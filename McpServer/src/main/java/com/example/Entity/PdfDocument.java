package com.example.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity                     // JPA 实体注解
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pdf_document")   // JPA 表名注解（注意是 @Table，不是 @TableName）
public class PdfDocument {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 200)
        private String title;

        @Column(length = 500)
        private String description;

        @Column(length = 50)
        private String category;

        @Column(name = "publish_date")
        private LocalDate publishDate;

        @Column(name = "file_name", nullable = false, length = 200)
        private String fileName;
}