package com.example.webapp.Database;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.webapp.Entity.DocumentDTO;
import com.example.webapp.Entity.PdfDocument;
import com.example.webapp.Mapper.DocumentMapper;
import com.example.webapp.Service.ServiceImpl.PdfStreamService;
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

    // 1. 查询文档列表
    @GetMapping("/documents")
    public List<DocumentDTO> listDocuments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        List<PdfDocument> docs = documentRepository.search(keyword, category);
        return docs.stream().map(DocumentDTO::new).collect(Collectors.toList());
    }

    // 2. 获取 PDF 文件流（支持 Range）
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
}