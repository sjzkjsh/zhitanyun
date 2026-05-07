package com.example.Service.ServiceImpl;

import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.stereotype.Service;

@Service
public class ImageService {


    private final ImageModel imageModel;
    public ImageService(ImageModel imageModel) {
        this.imageModel = imageModel;
    }
    public String getImageUrl(String message){
        ImagePrompt imagePrompt = new ImagePrompt(message);
        ImageResponse call = imageModel.call(imagePrompt);
        return call.getResult().getOutput().getUrl();
    }

}
