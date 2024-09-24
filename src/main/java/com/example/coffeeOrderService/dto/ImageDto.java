package com.example.coffeeOrderService.dto;

import com.example.coffeeOrderService.model.image.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ImageDto {

    private Long id;
    private String fileName;
    private String downloadUrl;

    public static ImageDto fromImage(Image image) {
        return ImageDto.builder()
                .id(image.getId())
                .fileName(image.getFileName())
                .downloadUrl(image.getDownloadUrl())
                .build();
    }
}
