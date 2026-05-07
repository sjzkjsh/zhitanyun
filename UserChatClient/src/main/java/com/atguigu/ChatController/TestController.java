package com.atguigu.ChatController;

import com.atguigu.FeignInterface.PageFeign;
import com.atguigu.RagFlowService.BailianKnowledgeService;
import com.atguigu.Result.Result;
import com.atguigu.Util.LoginUserHolder;
import com.example.Entity.ChatEntity.ChatMessage;
import com.example.Entity.ChatEntity.ContextCreateRequest;
import com.example.Entity.ChatEntity.ContextListVO;
import com.example.Entity.ChatEntity.ContextUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Slf4j
@RestController
public class TestController {

    @Autowired
    ChatClient qwenChatClient;

    @Autowired
    BailianKnowledgeService bailianKnowledgeService;

    @Autowired
    PageFeign pageFeign;

    /**
     * 创建 SSE 事件，无需手动转义换行（ServerSentEvent.builder 会自动处理）
     */
    private ServerSentEvent<String> makeEvent(String eventName, String data) {
        return ServerSentEvent.builder(data).event(eventName).build();
    }

    @GetMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
    public Flux<ServerSentEvent<String>> chat(
            @RequestParam(required = false, defaultValue = "你好") String message,
            @RequestParam(required = false) List<String> files,
            @RequestParam(required = false) List<String> contexts
    ) {
        String id = LoginUserHolder.getLoginUser().getUsername();

        // 1. 异步获取文件内容
        Mono<String> filesMono = (files == null || files.isEmpty())
                ? Mono.just("")
                : Flux.fromIterable(files)
                .flatMap(fileId -> Mono.zip(
                        Mono.fromCallable(() -> pageFeign.getFileName(fileId))
                                .subscribeOn(Schedulers.boundedElastic())
                                .timeout(Duration.ofSeconds(2), Mono.just("未知文件"))
                                .onErrorReturn("未知文件"),
                        Mono.fromCallable(() -> pageFeign.getFileContext(fileId))
                                .subscribeOn(Schedulers.boundedElastic())
                                .timeout(Duration.ofSeconds(5), Mono.just(""))
                                .onErrorReturn("")
                ).map(tuple -> String.format("【附件：%s】\n%s\n\n", tuple.getT1(), tuple.getT2())))
                .collectList()
                .map(list -> String.join("", list))
                .doOnError(e -> log.error("获取文件内容失败", e))
                .onErrorReturn(""); // 确保任何异常都不中断流

        // 2. 构建 Prompt
        Mono<String> promptMono = filesMono.map(filesContext -> {
            StringBuilder finalPrompt = new StringBuilder();

            if (!filesContext.isBlank()) {
                finalPrompt.append("【附件内容】\n").append(filesContext).append("\n\n");
            }

            if (contexts != null && !contexts.isEmpty()) {
                finalPrompt.append("【用户添加的上下文】\n");
                for (String ctx : contexts) {
                    finalPrompt.append(ctx).append("\n\n");
                }
            }

            finalPrompt.append(message);
            return finalPrompt.toString();
        });

        // 3. 流式响应处理
        return promptMono.flatMapMany(prompt -> {
            log.info("开始流式调用，prompt长度: {}", prompt.length());

            // 状态管理变量
            AtomicBoolean inThinking = new AtomicBoolean(false);
            AtomicBoolean inAnswer = new AtomicBoolean(false);
            AtomicBoolean thinkingStarted = new AtomicBoolean(false);
            AtomicBoolean answerStarted = new AtomicBoolean(false);
            AtomicReference<StringBuilder> bufferRef = new AtomicReference<>(new StringBuilder());

            return qwenChatClient
                    .prompt()
                    .user(prompt)
                    .advisors(a -> a.param(CONVERSATION_ID, id))
                    .stream()
                    .content()
                    .doOnNext(chunk -> log.debug("收到chunk: '{}'", chunk))
                    .flatMap(chunk -> processChunk(
                            chunk,
                            inThinking, inAnswer,
                            thinkingStarted, answerStarted,
                            bufferRef
                    ))
                    .concatWith(flushRemainingBuffer(bufferRef, inThinking, inAnswer))
                    .doOnComplete(() -> log.info("流式传输完成"))
                    .doOnError(e -> log.error("流式传输错误", e))
                    .onErrorResume(e -> {
                        // 将异常转化为 SSE 事件发送给前端
                        log.error("捕获到异常，发送错误事件", e);
                        return Flux.just(
                                makeEvent("error", "模型服务异常: " + e.getMessage()),
                                makeEvent("done", "[DONE]")
                        );
                    });
        });
    }

    /**
     * 处理单个 chunk，解析标签并生成事件列表
     */
    private Flux<ServerSentEvent<String>> processChunk(
            String chunk,
            AtomicBoolean inThinking,
            AtomicBoolean inAnswer,
            AtomicBoolean thinkingStarted,
            AtomicBoolean answerStarted,
            AtomicReference<StringBuilder> bufferRef
    ) {
        List<ServerSentEvent<String>> events = new ArrayList<>();

        if (chunk == null || chunk.isEmpty()) {
            return Flux.empty();
        }

        // ========== 关键修复：纯文本兜底 ==========
        // 如果还没进入任何模式，且当前chunk不是以 '<' 开头，说明模型直接输出了文本，强制进入 answer 模式
        boolean firstChunk = !thinkingStarted.get() && !answerStarted.get();
        if (firstChunk && !chunk.trim().startsWith("<")) {
            log.info("检测到纯文本输出，自动进入answer模式");
            answerStarted.set(true);
            inAnswer.set(true);
            events.add(makeEvent("answer_start", ""));
        }

        StringBuilder buffer = bufferRef.get();
        buffer.append(chunk);
        String content = buffer.toString();

        // 循环处理完整标签
        int safetyCounter = 0;
        while (safetyCounter++ < 20 && !content.isEmpty()) {
            boolean processed = false;

            // 1. 处理 </thinking>
            if (inThinking.get()) {
                int thinkEnd = content.indexOf("</thinking>");
                if (thinkEnd != -1) {
                    if (thinkEnd > 0) {
                        String thinkContent = content.substring(0, thinkEnd);
                        events.add(makeEvent("thinking", thinkContent));
                    }
                    events.add(makeEvent("thinking_end", ""));
                    inThinking.set(false);
                    content = content.substring(thinkEnd + 11);
                    processed = true;
                    continue;
                }
            }

            // 2. 处理 </answer>
            if (inAnswer.get()) {
                int ansEnd = content.indexOf("</answer>");
                if (ansEnd != -1) {
                    if (ansEnd > 0) {
                        String ansContent = content.substring(0, ansEnd);
                        events.add(makeEvent("answer", ansContent));
                    }
                    events.add(makeEvent("done", "[DONE]"));
                    inAnswer.set(false);
                    content = content.substring(ansEnd + 9);
                    processed = true;
                    continue;
                }
            }

            // 3. 处理 <thinking>
            int thinkStart = content.indexOf("<thinking>");
            if (thinkStart != -1) {
                if (inAnswer.get() && thinkStart > 0) {
                    String before = content.substring(0, thinkStart);
                    events.add(makeEvent("answer", before));
                }
                if (!thinkingStarted.get()) {
                    events.add(makeEvent("thinking_start", ""));
                    thinkingStarted.set(true);
                }
                inThinking.set(true);
                inAnswer.set(false);
                content = content.substring(thinkStart + 10);
                processed = true;
                continue;
            }

            // 4. 处理 <answer>
            int ansStart = content.indexOf("<answer>");
            if (ansStart != -1) {
                if (!answerStarted.get()) {
                    events.add(makeEvent("answer_start", ""));
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
        buffer.setLength(0);
        buffer.append(content);

        // 防积累：如果没有未闭合标签，且处于某种模式，发送累积内容
        if (buffer.length() > 10) {
            String bufStr = buffer.toString();
            int lastOpen = bufStr.lastIndexOf('<');
            int lastClose = bufStr.lastIndexOf('>');

            if (lastOpen > lastClose) {
                // 有不完整标签，只发送标签前的内容
                String toSend = bufStr.substring(0, lastOpen);
                String toKeep = bufStr.substring(lastOpen);
                if (!toSend.isEmpty() && (inThinking.get() || inAnswer.get())) {
                    String eventType = inThinking.get() ? "thinking" : "answer";
                    events.add(makeEvent(eventType, toSend));
                }
                buffer.setLength(0);
                buffer.append(toKeep);
            } else {
                // 没有未闭合标签，直接发送全部
                if (inThinking.get()) {
                    events.add(makeEvent("thinking", bufStr));
                } else if (inAnswer.get()) {
                    events.add(makeEvent("answer", bufStr));
                }
                buffer.setLength(0);
            }
        }

        // 如果 events 为空但处于某种模式且 buffer 较小，暂时不发送，等待更多数据
        return Flux.fromIterable(events);
    }

    /**
     * 流结束时清空 buffer 中剩余的内容
     */
    private Mono<ServerSentEvent<String>> flushRemainingBuffer(
            AtomicReference<StringBuilder> bufferRef,
            AtomicBoolean inThinking,
            AtomicBoolean inAnswer
    ) {
        return Mono.fromCallable(() -> {
            StringBuilder buffer = bufferRef.get();
            if (buffer.length() > 0) {
                String remaining = buffer.toString()
                        .replace("<thinking>", "")
                        .replace("</thinking>", "")
                        .replace("<answer>", "")
                        .replace("</answer>", "");

                if (!remaining.isEmpty()) {
                    if (inAnswer.get()) {
                        return makeEvent("answer", remaining);
                    } else if (inThinking.get()) {
                        return makeEvent("thinking", remaining);
                    } else {
                        // 关键修复：如果流结束时仍未进入任何模式，作为 answer 发送
                        log.info("流结束，剩余内容作为answer发送");
                        return makeEvent("answer", remaining);
                    }
                }
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).filter(evt -> evt != null);
    }

    // ========== 上下文管理接口（保持不变）==========

    @PostMapping("/context")
    public Result<String> createContext(@RequestBody ContextCreateRequest request) {
        return pageFeign.createContext(request);
    }

    @GetMapping("/contexts")
    public Result<List<ContextListVO>> listContexts() {
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        return pageFeign.listContexts(userId.toString());
    }

    @GetMapping("/context/{contextId}/history")
    public Result<List<ChatMessage>> getHistory(@PathVariable String contextId) {
        return pageFeign.getHistory(contextId);
    }

    @PutMapping("/context/{contextId}")
    public void updateContext(
            @PathVariable String contextId,
            @RequestBody ContextUpdateRequest request) {
        pageFeign.updateContext(contextId, request);
    }

    @DeleteMapping("/context/{contextId}")
    public void deleteContext(@PathVariable String contextId) {
        pageFeign.deleteContext(contextId);
    }
}