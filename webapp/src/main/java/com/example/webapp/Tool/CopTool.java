package com.example.webapp.Tool;

import com.example.webapp.Entity.InstantCopResult;
import com.example.webapp.Service.ServiceImpl.CopServiceImpl;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CopTool {


    @Autowired
    private CopServiceImpl copService;

    @Tool(name = "calculateInstantCop",
            description = """
            计算当前客户绑定设备的空调瞬时COP（能效比）。
            当用户询问以下问题时调用此工具：
            - 空调效率怎么样 / COP是多少
            - 空调运行正常吗 / 制冷效果好不好
            - 能效比 / 能耗效率
            返回COP值、制冷量、功耗、温差、水流量等信息。
            """)
    public InstantCopResult calculateInstantCop() {
        return copService.calculateInstantCop();
    }
}
