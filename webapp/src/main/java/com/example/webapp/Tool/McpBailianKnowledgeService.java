package com.example.webapp.Tool;

import com.aliyun.bailian20231229.Client;
import com.aliyun.bailian20231229.models.RetrieveRequest;
import com.aliyun.bailian20231229.models.RetrieveResponse;
import com.aliyun.bailian20231229.models.RetrieveResponseBody;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class McpBailianKnowledgeService {

    @Autowired
    private Client bailianClient;

    @Value("${alibaba.cloud.bailian.workspace-id}")
    private String workspaceId;

    @Value("${alibaba.cloud.bailian.default-index-id:}")
    private String defaultIndexId;

    /**
     * 使用默认知识库ID检索（MCP Tool 版本）
     */
    @Tool(name = "knowledge",
            description = """
        查询专业知识库（包含建筑电气规范、设备运维指南、故障案例等）。
        当用户询问专业术语、规范要求、故障原因时调用此工具。
        例如：风盘机是什么 / 防雷接地要求 / 空调不制冷原因
        """)
    public String searchKnowledgeBase(
            @ToolParam(description = "查询关键词或问题描述")
            String query
    ) {
        log.info("🎯 MCP Tool 'knowledge' 被调用，查询词：{}", query);

        if (defaultIndexId == null || defaultIndexId.isEmpty()) {
            log.error("未配置默认知识库ID");
            return "错误：未配置知识库ID，请检查配置 alibaba.cloud.bailian.default-index-id";
        }

        try {
            String result = searchKnowledgeBaseInternal(defaultIndexId, query);

            // 【关键】确保返回非空且格式正确
            if (result == null || result.isBlank()) {
                return "知识库中未找到相关内容。建议尝试其他关键词如'电源'、'防雷'、'建筑电气'。";
            }

            // 【关键】截断超长文本（MCP 限制，建议不超过 8000 字符）
            if (result.length() > 8000) {
                result = result.substring(0, 8000) +
                        "\n\n...（内容过长已截断，如需更详细请缩小查询范围）";
            }

            // 【关键】清理可能导致 MCP 序列化失败的字符
            result = cleanForMcp(result);

            log.info("✅ 知识库查询成功，返回长度：{}", result.length());
            return result;

        } catch (Exception e) {
            log.error("❌ 知识库查询失败：{}", e.getMessage(), e);
            // 【关键】返回错误信息而不是抛出异常或返回 null
            return "知识库查询失败：" + e.getMessage() + "。请稍后重试。";
        }
    }

    /**
     * 内部查询方法（不暴露为 Tool）
     */
    private String searchKnowledgeBaseInternal(String indexId, String query) throws Exception {
        RetrieveRequest request = new RetrieveRequest();
        request.setIndexId(indexId);
        request.setQuery(query);

        RuntimeOptions runtime = new RuntimeOptions();
        runtime.readTimeout = 5000;
        runtime.connectTimeout = 3000;

        RetrieveResponse response = bailianClient.retrieveWithOptions(workspaceId, request, null, runtime);
        return parseRetrieveResponse(response);
    }

    /**
     * 解析检索响应
     */
    private String parseRetrieveResponse(RetrieveResponse response) {
        if (response == null || response.getBody() == null) {
            return "";
        }

        RetrieveResponseBody body = response.getBody();

        // 检查是否成功
        if (body.getSuccess() == null || !body.getSuccess()) {
            log.warn("知识库检索未成功: {}", body.getMessage());
            return "";
        }

        RetrieveResponseBody.RetrieveResponseBodyData data = body.getData();
        if (data == null) {
            return "";
        }

        List<RetrieveResponseBody.RetrieveResponseBodyDataNodes> nodes = data.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            return "";
        }

        // 提取所有 node 的文本内容
        return nodes.stream()
                .map(node -> node.getText())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    /**
     * 【关键】清理文本，确保符合 MCP 规范
     */
    private String cleanForMcp(String text) {
        if (text == null) {
            return "";
        }

        // 1. 统一换行符
        text = text.replace("\r\n", "\n").replace("\r", "\n");

        // 2. 移除控制字符（保留 \n \t 等常见字符）
        text = text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");

        // 3. 移除零宽字符
        text = text.replaceAll("[\\u200B-\\u200D\\uFEFF]", "");

        // 4. 确保不以特殊字符开头（避免 MCP 解析问题）
        text = text.trim();

        // 5. 如果结果为空，返回提示
        if (text.isEmpty()) {
            return "知识库返回内容为空。";
        }

        return text;
    }

    /**
     * 高级检索：返回原始 nodes（非 Tool 方法）
     */
    public List<RetrieveResponseBody.RetrieveResponseBodyDataNodes> retrieveRaw(String indexId, String query) {
        try {
            RetrieveRequest request = new RetrieveRequest();
            request.setIndexId(indexId);
            request.setQuery(query);

            RuntimeOptions runtime = new RuntimeOptions();
            runtime.readTimeout = 5000;

            RetrieveResponse response = bailianClient.retrieveWithOptions(workspaceId, request, null, runtime);

            if (response.getBody() != null
                    && Boolean.TRUE.equals(response.getBody().getSuccess())
                    && response.getBody().getData() != null) {
                return response.getBody().getData().getNodes();
            }
            return List.of();
        } catch (Exception e) {
            log.error("知识库检索失败", e);
            return List.of();
        }
    }
}