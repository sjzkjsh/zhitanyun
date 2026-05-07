package com.atguigu.RagFlowService;

import com.aliyun.bailian20231229.Client;
import com.aliyun.bailian20231229.models.RetrieveRequest;
import com.aliyun.bailian20231229.models.RetrieveResponse;
import com.aliyun.bailian20231229.models.RetrieveResponseBody;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BailianKnowledgeService {

    @Autowired
    private Client bailianClient;

    @Value("${alibaba.cloud.bailian.workspace-id}")
    private String workspaceId;

    @Value("${alibaba.cloud.bailian.default-index-id:}")
    private String defaultIndexId;

    /**
     * 使用默认知识库ID检索
     */
    public String searchKnowledgeBase(String query) {
        if (defaultIndexId == null || defaultIndexId.isEmpty()) {
            throw new IllegalStateException("未配置默认知识库ID，请设置 alibaba.cloud.bailian.default-index-id 或调用带indexId参数的方法");
        }
        return searchKnowledgeBase(defaultIndexId, query);
    }

    /**
     * 指定知识库ID检索
     *
     * @param indexId 知识库ID（在阿里云百炼控制台获取）
     * @param query   用户查询语句
     * @return 格式化后的知识库内容，适合拼接到Prompt中
     */
    public String searchKnowledgeBase(String indexId, String query) {
        try {
            log.debug("开始检索知识库，indexId: {}, query: {}", indexId, query);

            RetrieveRequest request = new RetrieveRequest();
            request.setIndexId(indexId);
            request.setQuery(query);

            // 可选：设置检索参数
            // request.setTopK(5); // 返回Top5结果
            // request.setRerankTopK(3); // 重排序后返回Top3

            RuntimeOptions runtime = new RuntimeOptions();
            runtime.readTimeout = 5000; // 5秒超时
            runtime.connectTimeout = 3000; // 3秒连接超时

            RetrieveResponse response = bailianClient.retrieveWithOptions(workspaceId, request, null, runtime);

            return parseRetrieveResponse(response);

        } catch (Exception e) {
            log.error("阿里云百炼知识库检索失败", e);
            // 返回空字符串，让对话可以继续，只是没有RAG增强
            return "";
        }
    }

    /**
     * 解析检索响应，提取文本内容
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

        // 注意：阿里云返回的是 Nodes 而不是 Chunks
        List<RetrieveResponseBody.RetrieveResponseBodyDataNodes> nodes = data.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            return "";
        }

        // 提取所有node的文本内容
        return nodes.stream()
                .map(node -> {
                    String text = node.getText();
                    // Float score = node.getScore(); // 相关度分数
                    // Map<String, Object> metadata = node.getMetadata(); // 元数据

                    if (text == null || text.trim().isEmpty()) {
                        return null;
                    }

                    return text.trim();
                })
                .filter(Objects::nonNull)
                .distinct() // 去重
                .collect(Collectors.joining("\n\n---\n\n")); // 用分隔符连接多个片段
    }

    /**
     * 高级检索：返回原始nodes，供自定义处理
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