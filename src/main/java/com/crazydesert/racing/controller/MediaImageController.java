package com.crazydesert.racing.controller;

import com.crazydesert.racing.dto.MediaImageResponse;
import com.crazydesert.racing.service.MediaImageService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/media/images")
public class MediaImageController {

    private final MediaImageService mediaImageService;

    public MediaImageController(MediaImageService mediaImageService) {
        this.mediaImageService = mediaImageService;
    }

    @GetMapping("/{imageKey}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageKey) {
        MediaImageResponse image = mediaImageService.getPublicImage(imageKey);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(
                        CacheControl.maxAge(Duration.ofDays(30)).cachePublic()
                )
                .body(image.data());
    }
}
