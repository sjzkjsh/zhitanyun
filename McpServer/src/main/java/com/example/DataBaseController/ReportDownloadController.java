package com.example.DataBaseController;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/reports")
public class ReportDownloadController {

    // 读取与 ReportExportService 相同的输出目录配置
    @Value("${report.output.dir.path:./reports}")
    private String reportOutputPath;

    /**
     * 下载或预览生成的 HTML 报告
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(reportOutputPath).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // 若想浏览器直接预览，可将 attachment 改为 inline
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + fileName + "\"")
                        .contentType(MediaType.TEXT_HTML)
                        .body(resource);
            } else {
                System.err.println("❌ 文件不存在: " + filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}