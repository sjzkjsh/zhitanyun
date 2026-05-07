package com.example.Mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Entity.PdfDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DocumentMapper extends BaseMapper<PdfDocument> {
    @Select("SELECT * FROM pdf_document WHERE " +
            "(#{keyword} IS NULL OR title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{category} IS NULL OR category = #{category})")
    List<PdfDocument> search(@Param("keyword") String keyword,
                             @Param("category") String category);




}