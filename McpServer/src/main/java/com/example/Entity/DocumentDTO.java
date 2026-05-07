package com.example.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String publishDate;
    private String pdfUrl;   // 前端预览时请求的地址

    public DocumentDTO(PdfDocument doc) {
        this.id = doc.getId();
        this.title = doc.getTitle();
        this.description = doc.getDescription();
        this.category = doc.getCategory();
        this.publishDate = doc.getPublishDate().toString();
        this.pdfUrl = "/api/pdf/" + doc.getId();   // 关键：指向后端PDF流接口
    }
    // getter/setter
}