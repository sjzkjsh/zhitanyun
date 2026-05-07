package com.atguigu.ChatController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
//@RequestMapping("/api")
public class HomeChat {

    @Autowired
    ChatClient qwenChatClient;

    @RequestMapping(value = "/HomeChat", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> chat(
            @RequestParam(required = false) Integer buildingId,
            @RequestParam(required = false) Integer deviceId,
            @RequestParam(defaultValue = "1") String id) {

        String userMessage = String.format(
                "使用building_optimization_strategy这个工具，参数：buildingId=%s, deviceId=%s",
                buildingId != null ? buildingId : "全局",
                deviceId != null ? deviceId : "全局"
        );

        return qwenChatClient.prompt()
                .user(userMessage)
                .stream()
                .content()
                .transform(this::processStream); // 复用同一个转换逻辑
    }

    /**
     * 核心：标签解析与流式输出转换
     * 输出格式：
     *   think:xxx    -> 思考内容
     *   answer:xxx   -> 回答内容
     *   [THINK_END]  -> 思考结束标记
     *   [DONE]       -> 全部结束
     */
    private Flux<String> processStream(Flux<String> source) {
        AtomicBoolean inThinking = new AtomicBoolean(false);
        AtomicBoolean inAnswer = new AtomicBoolean(false);
        AtomicBoolean thinkingStarted = new AtomicBoolean(false);
        AtomicBoolean answerStarted = new AtomicBoolean(false);
        AtomicReference<StringBuilder> buffer = new AtomicReference<>(new StringBuilder());

        return source
                .concatWith(Flux.just("</stream>")) // 结束标记，触发 flush
                .flatMap(chunk -> {
                    if ("</stream>".equals(chunk)) {
                        return flushBuffer(buffer, inThinking, inAnswer, thinkingStarted, answerStarted);
                    }
                    return processChunk(chunk, inThinking, inAnswer, thinkingStarted, answerStarted, buffer);
                })
                .onErrorResume(e -> Flux.just("error:" + e.getMessage(), "[DONE]"));
    }

    private Flux<String> processChunk(String chunk,
                                      AtomicBoolean inThinking, AtomicBoolean inAnswer,
                                      AtomicBoolean thinkingStarted, AtomicBoolean answerStarted,
                                      AtomicReference<StringBuilder> bufferRef) {

        if (chunk == null || chunk.isEmpty()) return Flux.empty();

        StringBuilder buf = bufferRef.get();
        buf.append(chunk);
        String content = buf.toString();
        java.util.List<String> outputs = new java.util.ArrayList<>();

        // 纯文本兜底：首 chunk 不以 < 开头，直接进 answer
        if (!thinkingStarted.get() && !answerStarted.get() && !content.trim().startsWith("<")) {
            answerStarted.set(true);
            inAnswer.set(true);
            outputs.add("answer_start:");
        }

        // 循环处理完整标签
        while (!content.isEmpty()) {
            boolean processed = false;

            // 1. 闭合标签处理
            if (inThinking.get()) {
                int end = content.indexOf("</thinking>");
                if (end != -1) {
                    if (end > 0) outputs.add("think:" + content.substring(0, end));
                    outputs.add("[THINK_END]");
                    inThinking.set(false);
                    content = content.substring(end + 11);
                    processed = true;
                    continue;
                }
            }
            if (inAnswer.get()) {
                int end = content.indexOf("</answer>");
                if (end != -1) {
                    if (end > 0) outputs.add("answer:" + content.substring(0, end));
                    outputs.add("[DONE]");
                    inAnswer.set(false);
                    content = content.substring(end + 9);
                    processed = true;
                    continue;
                }
            }

            // 2. 开始标签处理
            int thinkStart = content.indexOf("<thinking>");
            if (thinkStart != -1) {
                if (inAnswer.get() && thinkStart > 0) {
                    outputs.add("answer:" + content.substring(0, thinkStart));
                }
                if (!thinkingStarted.get()) {
                    outputs.add("think_start:");
                    thinkingStarted.set(true);
                }
                inThinking.set(true);
                inAnswer.set(false);
                content = content.substring(thinkStart + 10);
                processed = true;
                continue;
            }

            int ansStart = content.indexOf("<answer>");
            if (ansStart != -1) {
                if (!answerStarted.get()) {
                    outputs.add("answer_start:");
                    answerStarted.set(true);
                }
                inAnswer.set(true);
                inThinking.set(false);
                content = content.substring(ansStart + 8);
                processed = true;
                continue;
            }

            if (!processed) break;
        }

        // 更新 buffer
        buf.setLength(0);
        buf.append(content);

        // 防积累：长 buffer 且不含未闭合标签时发送
        if (buf.length() > 20) {
            String str = buf.toString();
            int lastOpen = str.lastIndexOf('<');
            int lastClose = str.lastIndexOf('>');

            if (lastOpen == -1 || lastClose > lastOpen) {
                // 没有未闭合标签，或标签已完整
                String toSend = lastOpen > lastClose ? str.substring(0, lastOpen) : str;
                String toKeep = lastOpen > lastClose ? str.substring(lastOpen) : "";

                if (!toSend.isEmpty()) {
                    if (inThinking.get()) outputs.add("think:" + toSend);
                    else if (inAnswer.get()) outputs.add("answer:" + toSend);
                }
                buf.setLength(0);
                buf.append(toKeep);
            }
        }

        return Flux.fromIterable(outputs);
    }

    private Flux<String> flushBuffer(AtomicReference<StringBuilder> bufferRef,
                                     AtomicBoolean inThinking, AtomicBoolean inAnswer,
                                     AtomicBoolean thinkingStarted, AtomicBoolean answerStarted) {

        StringBuilder buf = bufferRef.get();
        if (buf.length() == 0) {
            // 确保有结束标记
            if (answerStarted.get() || thinkingStarted.get()) {
                return Flux.just("[DONE]");
            }
            return Flux.empty();
        }

        String remaining = buf.toString()
                .replace("<thinking>", "").replace("</thinking>", "")
                .replace("<answer>", "").replace("</answer>", "");

        java.util.List<String> outputs = new java.util.ArrayList<>();

        if (!remaining.isEmpty()) {
            if (inAnswer.get()) outputs.add("answer:" + remaining);
            else if (inThinking.get()) outputs.add("think:" + remaining);
            else outputs.add("answer:" + remaining); // 兜底
        }

        outputs.add("[DONE]");
        return Flux.fromIterable(outputs);
    }
}