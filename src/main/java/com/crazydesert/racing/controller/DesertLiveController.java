package com.crazydesert.racing.controller;

import com.crazydesert.racing.dto.DesertLiveImageResponse;
import com.crazydesert.racing.dto.DesertLiveItemResponse;
import com.crazydesert.racing.dto.DesertLivePageResponse;
import com.crazydesert.racing.enums.DesertLiveCategory;
import com.crazydesert.racing.service.DesertLiveImageService;
import com.crazydesert.racing.service.DesertLiveQueryService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/desert-live")
public class DesertLiveController {

    private final DesertLiveQueryService queryService;
    private final DesertLiveImageService imageService;

    public DesertLiveController(
            DesertLiveQueryService queryService,
            DesertLiveImageService imageService) {

        this.queryService = queryService;
        this.imageService = imageService;
    }

    @GetMapping("/random")
    public List<DesertLiveItemResponse> getRandomItems(
            @RequestParam(required = false) DesertLiveCategory category,
            @RequestParam(defaultValue = "3") int limit) {

        return queryService.getRandomItems(category, limit);
    }

    @GetMapping
    public DesertLivePageResponse getPublicItems(
            @RequestParam(required = false) DesertLiveCategory category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return queryService.getPublicItems(
                category,
                search,
                page,
                size
        );
    }

    @GetMapping("/{id}")
    public DesertLiveItemResponse getPublicItem(@PathVariable Long id) {
        return queryService.getPublicItem(id);
    }

    @GetMapping("/images/{imageKey}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageKey) {
        DesertLiveImageResponse image = imageService.getImage(imageKey);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(
                        CacheControl.maxAge(Duration.ofDays(30)).cachePublic()
                )
                .body(image.data());
    }
}
