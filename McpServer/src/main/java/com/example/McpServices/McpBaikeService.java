package com.example.McpServices;

import com.example.Service.baidu.BaiduBaikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 百度百科 MCP 工具
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpBaikeService {

    private final BaiduBaikeService baikeService;

    @Tool(name = "search_baike",
            description = """
          【百度百科查询】
          
          查询百度百科词条的详细信息，包括摘要、内容、图片等。
          适用于查询人物、地点、事件、概念等百科知识。
          
          参数：
          - keyword: 词条名称（支持中文，如：李荣浩、北京、人工智能）
          
          返回：词条标题、摘要、详细内容、相关图片、标签分类、百科链接
          """)
    public String searchBaike(@ToolParam String keyword) {
        log.info("MCP百科查询: {}", keyword);

        BaiduBaikeService.BaikeResult result = baikeService.getContentByTitle(keyword);

        // 【修改】返回自然语言，而非JSON
        if (!result.found()) {
            return String.format("百度百科中未找到'%s'的相关词条。建议：1.检查词条名称是否正确；2.尝试使用更通用的关键词。", keyword);
        }

        // 清理HTML并截断
        String cleanSummary = result.summary()
                .replaceAll("<[^>]+>", "")
                .trim();

        // 【修改】简化格式，确保不超过MCP长度限制
        return String.format("""
        【百度百科-%s】
        摘要：%s
        标签：%s
        链接：%s
        详细内容：%s
        """,
                result.title(),
                truncate(cleanSummary, 200),
                String.join("、", result.tags()),
                result.url(),
                truncate(result.content(), 1000)
        );
    }

    @Tool(name = "search_baike_batch",
            description = "批量查询多个百科词条，逗号分隔关键词")
    public String searchBaikeBatch(
            @ToolParam(description = "多个词条，逗号分隔", required = true)
            String keywords) {

        List<String> list = Arrays.asList(keywords.split("[,，]"));
        List<BaiduBaikeService.BaikeResult> results = baikeService.getContentsBatch(list);

        StringBuilder sb = new StringBuilder("【百科批量查询】\n\n");

        for (int i = 0; i < results.size(); i++) {
            BaiduBaikeService.BaikeResult r = results.get(i);
            String key = list.get(i).trim();

            sb.append(i + 1).append(". ");
            if (r.found()) {
                String s = r.summary().replaceAll("<[^>]+>", "");
                if (s.length() > 80) s = s.substring(0, 80) + "...";
                sb.append(String.format("%s：%s\n", r.title(), s));
            } else {
                sb.append(String.format("【%s】未找到\n", key));
            }
        }

        return sb.toString();
    }

    private String truncate(String str, int max) {
        if (str == null || str.length() <= max) return str;
        return str.substring(0, max) + "...";
    }
}