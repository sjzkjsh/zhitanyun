package com.example.McpServices;

import com.example.Service.baidu.BaiduWebSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 大模型专用 百度联网搜索MCP工具
 * 支持：快捷搜索 + 高级自定义搜索（完整调用webSearch）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaiduWebSearchMcpService {

    private final BaiduWebSearchService baiduWebSearchService;
    private final McpSystemService systemService;  // 注入你的时间服务

    private static final DateTimeFormatter CHINESE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy年M月d日");

    // ====================== 1. 快捷搜索（自动处理时间词）======================
    @Tool(
            name = "quickNetworkSearch",
            description = """
        【实时联网搜索】
        
        ⚠️ 重要：当用户问题包含"今天/昨天/明天/最新"等时间词时，
        必须先调用 get_current_datetime 或 parse_relative_time 工具确认具体日期，
        然后再进行搜索。
        
        功能：查询百度实时搜索结果。
        本工具会自动将"今天"转换为当前日期（2026年4月1日）。
        
        参数：
        - query: 搜索关键词（字符串）
        
        使用流程：
        1. 用户问"今天新闻" → 2. 调用get_current_date()获取日期 → 3. 用具体日期搜索
        """
    )
    public String quickNetworkSearch(
            @ToolParam(description = "搜索关键词", required = true)
            String query) {

        if (query == null || query.isBlank()) {
            return "搜索失败：请输入有效关键词";
        }

        try {
            // 获取当前日期并处理时间词
            String currentDate = systemService.getCurrentDate();
            String processedQuery = processTimeKeywords(query, currentDate);

            log.info("搜索：{} → {}", query, processedQuery);

            BaiduWebSearchService.SearchResult result =
                    baiduWebSearchService.quickSearch(processedQuery);

            return formatResult(result, query, processedQuery);

        } catch (Exception e) {
            log.error("搜索异常", e);
            return "搜索服务异常：" + e.getMessage();
        }
    }

    // ====================== 2. 高级搜索 =======================
    @Tool(
            name = "webSearch",
            description = """
        【高级联网搜索】
        
        可自定义时间范围（day/week/month/year）和搜索版本。
        
        ⚠️ 注意：当query包含"今天/昨天"等词时，建议先调用parse_relative_time转换。
        
        参数：
        - query: 搜索关键词
        - recency: 时间范围（day/week/month/year）
        - edition: 搜索版本（standard/premium）
        """
    )
    public String webSearch(
            @ToolParam(description = "搜索关键词", required = true)
            String query,
            @ToolParam(description = "时间范围：day/week/month/year")
            String recency,
            @ToolParam(description = "搜索版本：standard/premium")
            String edition) {

        if (query == null || query.isBlank()) {
            return "搜索失败：请输入有效关键词";
        }

        try {
            String currentDate = systemService.getCurrentDate();
            String processedQuery = processTimeKeywords(query, currentDate);

            log.info("高级搜索：{} → {}，范围：{}，版本：{}",
                    query, processedQuery, recency, edition);

            BaiduWebSearchService.SearchResult result =
                    baiduWebSearchService.webSearch(processedQuery, recency, edition);

            return formatResult(result, query, processedQuery);

        } catch (Exception e) {
            log.error("高级搜索异常", e);
            return "高级搜索服务异常：" + e.getMessage();
        }
    }

    // ====================== 私有方法 ======================

    /**
     * 从JSON中提取日期并处理时间词
     */
    private String processTimeKeywords(String query, String currentDateJson) {
        // 解析 {"date": "2026-04-01", "chinese": "2026年4月1日", ...}
        String chineseDate = extractValue(currentDateJson, "chinese");
        String isoDate = extractValue(currentDateJson, "date");

        // 计算昨天明天
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        String yesterday = now.minusDays(1).format(CHINESE_FORMATTER);
        String tomorrow = now.plusDays(1).format(CHINESE_FORMATTER);

        return query
                .replaceAll("今天|今日|现在|当前", chineseDate)
                .replaceAll("昨天|昨日", yesterday)
                .replaceAll("明天|明日", tomorrow)
                .replaceAll("最新", chineseDate + "最新");
    }

    private String extractValue(String json, String key) {
        // 简单JSON解析： "key": "value"
        String pattern = "\"" + key + "\": \"([^\"]+)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private String formatResult(BaiduWebSearchService.SearchResult result,
                                String originalQuery, String processedQuery) {
        if (result.items().isEmpty()) {
            return String.format(
                    "未搜索到【%s】相关内容\n（查询词：%s → %s）",
                    originalQuery, originalQuery, processedQuery
            );
        }

        StringBuilder sb = new StringBuilder();
        sb.append("【联网搜索结果】").append(processedQuery).append("\n\n");

        int max = Math.min(result.items().size(), 5);
        for (int i = 0; i < max; i++) {
            var item = result.items().get(i);
            sb.append(i + 1).append("、").append(item.title()).append("\n");
            sb.append("摘要：").append(item.snippet()).append("\n");
            sb.append("来源：").append(item.source()).append("\n");
            sb.append("链接：").append(item.url()).append("\n\n");
        }

        // 附加查询说明
        if (!originalQuery.equals(processedQuery)) {
            sb.append("【查询说明】原始词：").append(originalQuery)
                    .append(" → 实际查询：").append(processedQuery).append("\n");
        }

        return sb.toString();
    }
}