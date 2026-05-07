package com.example.Service.AnalysisService;

import com.example.Entity.AnalysisEntity.EnhancedAnomalyReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportExportService {

    @Autowired
    private EnhancedAnomalyService enhancedAnomalyService;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.base-url:http://localhost:8014}")
    private String baseUrl;

    // 读取 YAML 配置的报告输出目录，默认为 ./reports（容错）
    @Value("${report.output.dir.path:./reports}")
    private String reportOutputPath;

    /**
     * 生成 HTML 异常分析报告，保存到配置目录，并返回下载链接
     */
    public String generateHtmlReport(Integer buildingId, Integer deviceId, List<String> metrics) throws Exception {
        System.out.println("===== 开始生成报告 =====");
        System.out.println("buildingId: " + buildingId + ", deviceId: " + deviceId);

        // 1. 获取数据
        EnhancedAnomalyReport report = null;
        try {
            report = enhancedAnomalyService.analyzeWithRootCauseAuto(buildingId, deviceId, metrics);
            System.out.println("✅ 数据获取成功");
        } catch (Exception e) {
            System.err.println("❌ 数据获取失败:");
            e.printStackTrace();
            throw e;
        }

        // 2. 渲染模板
        String htmlContent = null;
        try {
            Context context = new Context();
            context.setVariable("report", report);
            context.setVariable("generatedAt", LocalDateTime.now());
            htmlContent = templateEngine.process("anomaly-report", context);
            System.out.println("✅ 模板渲染成功，内容长度: " + htmlContent.length());
        } catch (Exception e) {
            System.err.println("❌ 模板渲染失败:");
            e.printStackTrace();
            throw e;
        }

        // 3. 生成文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("anomaly_report_%d_%s.html", deviceId, timestamp);
        System.out.println("📄 文件名: " + fileName);

        // 4. 写入文件
        try {
            Path outputDir = Paths.get(reportOutputPath);
            System.out.println("📁 目标目录: " + outputDir.toAbsolutePath());
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
                System.out.println("📁 目录已创建");
            }
            Path filePath = outputDir.resolve(fileName);
            Files.write(filePath, htmlContent.getBytes(StandardCharsets.UTF_8));
            System.out.println("✅ 报告已保存至：" + filePath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ 文件写入失败:");
            e.printStackTrace();
            throw e;
        }

        // 5. 返回链接
        String downloadUrl = baseUrl + "/api/reports/download/" + fileName;
        System.out.println("🔗 返回链接: " + downloadUrl);
        return downloadUrl;
    }
}