package com.example.McpServices;




import com.example.Service.ServiceImpl.ImageService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class McpImageService {
    @Autowired
    private ImageService imageService;

    @Tool(name = "gat_imageurl",description = "根据用户描述生成图片")
    public String getImageUrl(String message) {
        String url = imageService.getImageUrl(message);
        if (url == null || url.isEmpty()) {
            return "{\"error\": \"图片生成失败\", \"message\": \"" + message + "\"}";
        }
        return "{\"imageUrl\": \"" + url + "\"}";
    }
}

