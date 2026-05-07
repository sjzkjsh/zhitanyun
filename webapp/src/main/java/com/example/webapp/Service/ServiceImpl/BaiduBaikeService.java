package com.example.webapp.Service.ServiceImpl;

import org.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 百度百科服务 - 完全按照官方示例封装
 */
@Slf4j
@Service
public class BaiduBaikeService {

    private static final String BASE_URL = "https://appbuilder.baidu.com/v2/baike/lemma/get_content";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .build();

    @Value("bce-v3/ALTAK-puLNjBcu5wCsWegUsbi4t/4919281ec2c6b937b80d2544f838570a5e51dcd1")
    private String apikey;

    /**
     * 百科词条查询结果
     */
    public record BaikeResult(
            String title,
            String summary,
            String content,
            String imageUrl,
            List<String> tags,
            String url,
            boolean found
    ) {}

    /**
     * 查询百科词条（完全按照官方示例实现）
     */
    public BaikeResult getContentByTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("百科查询关键词为空");
            return new BaikeResult(null, null, null, null, null, null, false);
        }

        String trimmedKeyword = keyword.trim();
        log.info("百度百科查询: {}", trimmedKeyword);

        try {
            // 注意：官方示例中 search_key 后面有空格，这里去掉空格
            String encodedKeyword = URLEncoder.encode(trimmedKeyword, StandardCharsets.UTF_8);
            String url = BASE_URL + "?search_type=lemmaTitle&search_key=" + encodedKeyword;

            // 生成随机请求ID（官方示例格式）
            String requestId = generateRequestId();

            // 按照官方示例：GET 请求但带有空的 RequestBody
            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, "");

            Request request = new Request.Builder()
                    .url(url)

                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Appbuilder-Request-Id", requestId)
                    .addHeader("Authorization", "Bearer " + apikey)
                    .build();

            log.info("请求URL: {}", url);
            log.info("请求ID: {}", requestId);

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                String responseBody = response.body().string();

                log.info("HTTP状态码: {}", response.code());
                log.debug("响应体: {}", responseBody);

                if (!response.isSuccessful()) {
                    log.error("请求失败: {} - {}", response.code(), response.message());
                    return new BaikeResult(null, null, null, null, null, null, false);
                }

                return parseResponse(responseBody, trimmedKeyword);
            }

        } catch (IOException e) {
            log.error("百科查询异常: {}", e.getMessage(), e);
            return new BaikeResult(null, null, null, null, null, null, false);
        }
    }

    /**
     * 简化查询 - 只返回摘要
     */
    public String getSimpleSummary(String keyword) {
        BaikeResult result = getContentByTitle(keyword);
        if (!result.found()) {
            return String.format("未找到【%s】的百科信息", keyword);
        }

        // 去除HTML标签
        String plainSummary = result.summary().replaceAll("<[^>]+>", "");

        return String.format("【%s】\n%s\n\n详情：%s",
                result.title(), plainSummary, result.url());
    }

    /**
     * 批量查询
     */
    public List<BaikeResult> getContentsBatch(List<String> keywords) {
        List<BaikeResult> results = new ArrayList<>();
        for (String keyword : keywords) {
            results.add(getContentByTitle(keyword));
            try {
                Thread.sleep(100); // 简单限流
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return results;
    }

    // ==================== 私有方法 ====================
    private String generateRequestId() {
        // 生成32位随机字符串
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 解析响应
     */
    /**
     * 解析响应
     */
    private BaikeResult parseResponse(String json, String originalKeyword) {
        try {
            JSONObject root = new JSONObject(json);

            // 检查请求ID（用于调试）
            String requestId = root.optString("request_id", "");
            log.debug("请求ID: {}", requestId);

            // 解析 result 字段（实际数据在这里）
            JSONObject result = root.optJSONObject("result");
            if (result == null) {
                log.warn("响应中result字段为空, raw={}", json);
                return new BaikeResult(null, null, null, null, null, null, false);
            }

            // 提取字段
            String title = result.optString("lemma_title", originalKeyword);
            String summary = result.optString("summary", "");
            String content = result.optString("abstract_plain", "");  // 纯文本摘要
            String imageUrl = result.optString("pic_url", "");        // 图片URL

            // 构建百科链接
            String url = result.optString("url", "");
            if (url.isEmpty()) {
                try {
                    String encoded = URLEncoder.encode(title, StandardCharsets.UTF_8);
                    url = "https://baike.baidu.com/item/" + encoded;
                } catch (Exception e) {
                    url = "https://baike.baidu.com";
                }
            }

            // 解析标签（如果有的话）
            List<String> tags = new ArrayList<>();
            // 可以从 card 或其他字段提取标签，这里简单处理

            log.info("百科查询成功: {}", title);
            return new BaikeResult(title, summary, content, imageUrl, tags, url, true);

        } catch (Exception e) {
            log.error("解析响应失败: {}, raw={}", e.getMessage(), json);
            return new BaikeResult(null, null, null, null, null, null, false);
        }
    }
}