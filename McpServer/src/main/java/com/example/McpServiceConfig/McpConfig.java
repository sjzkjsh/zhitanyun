package com.example.McpServiceConfig;


import com.example.McpServices.*;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;




@Configuration
public class McpConfig {
    @Autowired
    public TestTool testTool;


    @Autowired
    private McpAnomalyAnalysisService mcpAnomalyAnalysisService;
    @Autowired
    private McpDevicesService mcpDevicesService;
    @Autowired
    private McpEnergyService mcpEnergyService;
    @Autowired
    private McpBuildingService mcpBuildingService;
    @Autowired
    private McpImageService mcpImageService;

    @Autowired
    private McpBailianKnowledgeService mcpBailianKnowledgeService;

    @Autowired
    private McpCopService mcpCopService;

    @Autowired
    private BaiduWebSearchMcpService baiduWebSearchMcpService;

    @Autowired
    private McpCopCompute copCompute;
    @Autowired
    private McpBaikeService baikeService;
    @Autowired
    private McpSystemService mcpSystemService;

    @Autowired
    private BuildingOptimizationMcpTool buildingOptimizationMcpTool;

    @Autowired
    private McpEnergyScanTool mcpEnergyScanTool;
    @Autowired
    private WorkOrderMcpService workOrderMcpService;
    @Autowired
    private McpTransactionService mcpTransactionService;


//    @Bean
//    public ToolCallbackProvider allToolsProvider() {
//        return MethodToolCallbackProvider.builder()
//                .toolObjects(
////                        baiduWebSearchMcpService,
//                        mcpAnomalyAnalysisService,
//                        mcpDevicesService,
//                        mcpBuildingService,
//                        mcpEnergyService,
//                        mcpImageService,
//                        mcpCopService,
//                        // 如果 McpCopCompute 也需要注册，注意这里直接传对象，而不是 bean 名
//                        copCompute
//                )
//                .build();
//    }

    @Bean
    public ToolCallbackProvider testtool(){
        System.out.println("testtool注册工具");
        return MethodToolCallbackProvider.builder()
                .toolObjects(testTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider WorkOrder() {
        System.out.println("WorkOrder注册工具");
        return MethodToolCallbackProvider.builder()
                .toolObjects(workOrderMcpService)
                .build();
    }

    @Bean
    public ToolCallbackProvider TransactionTools() {
        System.out.println("TransactionTools注册工具（事务操作）");
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpTransactionService)
                .build();
    }

    @Bean
    public ToolCallbackProvider Konwledge(){
        System.out.println("Konwledge注册工具");
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpBailianKnowledgeService)
                .build();
    }

    @Bean
    public ToolCallbackProvider EnergyScan(){
        System.out.println("EnergyScan注册工具");
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpEnergyScanTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider Optimization(){
        System.out.println("Optimization注册工具");
        return MethodToolCallbackProvider
                .builder().
                toolObjects(buildingOptimizationMcpTool).build();
    }

    @Bean
    public  ToolCallbackProvider System(){
        System.out.println("系统时间注册工具");
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpSystemService).build();
    }

    @Bean
    public ToolCallbackProvider baiduBaikeSearch(){
        System.out.println("baidubaikeTool注册");
        return MethodToolCallbackProvider.builder()
                .toolObjects(baikeService).build();

    }



//    @Bean
//    public ToolCallbackProvider baiduSearch(){
//        System.out.println("baiduSearch工具注册");
//        return MethodToolCallbackProvider.builder().
//                toolObjects(baiduWebSearchMcpService).
//                build();
//    }


    @Bean//异常分析工具
    public ToolCallbackProvider anomalyAnalysisToolsProvider(){
        System.out.println("mcpAnomalyAnalysisService工具注册");
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpAnomalyAnalysisService)
                .build();
    }
    // 在你的配置类或启动类里
    @Bean
    public ToolCallbackProvider copTools() {
        System.out.println("copCompute工具注册");
        return MethodToolCallbackProvider.builder()
                .toolObjects(copCompute)
                .build();
    }
    //cop诊断工具
    @Bean
    public ToolCallbackProvider copServiceTools(){
        System.out.println("mcpCopService工具注册");
        return MethodToolCallbackProvider.builder().toolObjects(mcpCopService).build();
    }
//    设备查询工具
    @Bean
    public ToolCallbackProvider devicestoolsProvider( ){
        System.out.println("mcpDevicesService工具注册");
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpDevicesService)
                .build();
    }
    //建筑查询工具
    @Bean
    public ToolCallbackProvider buildingToolsProvider( ){
        System.out.println("mcpBuildingService工具注册");
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpBuildingService)
                .build();
    }
    //能耗，环境因素查询工具
    @Bean
    public ToolCallbackProvider energyToolsProvider( ){
        System.out.println("mcpEnergyService工具注册");
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpEnergyService)
                .build();
    }

    //智能图片生成工具
    @Bean
    public ToolCallbackProvider imageToolsProvider(){
        System.out.println("mcpImageService工具注册");
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpImageService)
                .build();
    }

}
