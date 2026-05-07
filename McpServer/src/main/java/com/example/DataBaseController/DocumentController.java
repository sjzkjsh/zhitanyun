package com.example.DataBaseController;


import com.example.Entity.DocumentDTO;
import com.example.Entity.PdfDocument;
import com.example.Mapper.DocumentMapper;
import com.example.Service.PdfServiceImpl.PdfStreamService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DocumentController {

    @Autowired
    private DocumentMapper documentRepository;

    @Autowired
    private PdfStreamService pdfStreamService;

    @Value("${pdf.storage.path}")
    private String pdfStoragePath;

    // 1. 查询地方标准文档列表
    @GetMapping("/documents")
    public List<DocumentDTO> listDocuments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        List<PdfDocument> docs = documentRepository.search(keyword, category);
        return docs.stream().map(DocumentDTO::new).collect(Collectors.toList());
    }
//    //查询使用说明书列表
//    @GetMapping("/documentUse")
//    public List<DocumentDTO> listDocumentsUse(
//            @RequestParam(required = false) String keyword,
//            @RequestParam(required = false) String category) {
//        List<PdfDocument> docs = documentRepository.searchUse(keyword, category);
//        return docs.stream().map(DocumentDTO::new).collect(Collectors.toList());
//    }

    // 2. 获取 PDF 文件流（在线预览）
    @GetMapping("/pdf/{id}")
    public ResponseEntity<InputStreamResource> getPdf(@PathVariable Long id, HttpServletRequest request) throws IOException {
        PdfDocument doc = documentRepository.selectById(id);
        if (doc == null) {
            throw new RuntimeException("文档不存在");
        }
        Path filePath = Paths.get(pdfStoragePath).resolve(doc.getFileName());
        if (!filePath.toFile().exists()) {
            return ResponseEntity.notFound().build();
        }
        String rangeHeader = request.getHeader("Range");
        return pdfStreamService.getPdfStream(filePath, rangeHeader);
    }

    // 3. 下载 PDF 文件
    @GetMapping("/pdf/download/{id}")
    public ResponseEntity<InputStreamResource> downloadPdf(@PathVariable Long id) throws IOException {
        PdfDocument doc = documentRepository.selectById(id);
        if (doc == null) {
            throw new RuntimeException("文档不存在");
        }
        Path filePath = Paths.get(pdfStoragePath).resolve(doc.getFileName());
        if (!filePath.toFile().exists()) {
            return ResponseEntity.notFound().build();
        }

        org.springframework.core.io.Resource resource =
                new org.springframework.core.io.FileSystemResource(filePath.toFile());

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=\"" + java.net.URLEncoder.encode(doc.getFileName(), "UTF-8") + "\"")
                .header("Content-Type", "application/pdf")
                .header("Content-Length", String.valueOf(filePath.toFile().length()))
                .body(new InputStreamResource(resource.getInputStream()));
    }
}