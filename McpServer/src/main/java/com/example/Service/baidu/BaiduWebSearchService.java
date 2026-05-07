package com.example.Service.baidu;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class BaiduWebSearchService {

    //
    @Value("bce-v3/ALTAK-puLNjBcu5wCsWegUsbi4t/4919281ec2c6b937b80d2544f838570a5e51dcd1")
    private String apiKey;

    // 创建 RestClient 实例
    private final RestClient restClient;

    public BaiduWebSearchService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://qianfan.baidubce.com")
                .build();
    }
    // 搜索
    public SearchResult webSearch(String query, String recency, String edition) {
        Map<String, Object> body = Map.of(
                "messages", List.of(
                        Map.of("role", "user", "content", query)
                ),
                "edition", edition,
                "search_source", "baidu_search_v2",
                "search_recency_filter", recency
        );

        JsonNode response = restClient.post()
                .uri("/v2/ai_search/web_search")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        return parseResult(response, query);
    }

    // 快捷搜索（默认参数）
    public SearchResult quickSearch(String query) {
        return webSearch(query, "week", "standard");
    }

    private SearchResult parseResult(JsonNode response, String query) {
        List<SearchItem> items = new java.util.ArrayList<>();

        JsonNode results = response.path("references"); // 修正：千帆接口返回字段是 references 不是 result
        if (results.isArray()) {
            results.forEach(r -> items.add(new SearchItem(
                    r.path("title").asText(""),
                    r.path("url").asText(""),
                    r.path("content").asText(""), // 修正：接口字段是 content 不是 snippet
                    r.path("source").asText(""),
                    r.path("date").asText("")
            )));
        }

        return new SearchResult(
                response.path("search_query").asText(query),
                items,
                items.size()
        );
    }

    // 内部实体类（不动）
    public record SearchResult(String query, List<SearchItem> items, int total) {}
    public record SearchItem(String title, String url, String snippet, String source, String date) {}
}