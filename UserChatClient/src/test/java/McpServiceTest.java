
import org.junit.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;


@SpringBootTest
public class McpServiceTest {

    @Autowired
    private ToolCallbackProvider toolsProvider;

    @Test
    public void testMcpToolsLoad() {
        var toolCallbacks = toolsProvider.getToolCallbacks();
        // 打印加载的MCP工具数量和名称
        System.out.println("加载的MCP工具数量：" + Arrays.stream(toolCallbacks).toList());
        for (ToolCallback toolCallback : toolCallbacks) {
            System.out.println("名称：" + toolCallback.toString());
        }
        
        // 如果输出包含 "baidu-map" 和 "web-search-mcp-server"，说明配置生效
    }
}