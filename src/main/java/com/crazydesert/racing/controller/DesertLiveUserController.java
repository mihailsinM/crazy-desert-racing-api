package com.crazydesert.racing.controller;

import com.crazydesert.racing.dto.DesertLiveCreateRequest;
import com.crazydesert.racing.dto.DesertLiveItemResponse;
import com.crazydesert.racing.dto.DesertLivePageResponse;
import com.crazydesert.racing.dto.DesertLiveUpdateRequest;
import com.crazydesert.racing.dto.ImageFocusRequest;
import com.crazydesert.racing.enums.DesertLiveCategory;
import com.crazydesert.racing.enums.DesertLiveModerationStatus;
import com.crazydesert.racing.service.DesertLiveCommandService;
import com.crazydesert.racing.service.DesertLiveQueryService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/desert-live/my")
public class DesertLiveUserController {

    private final DesertLiveQueryService queryService;
    private final DesertLiveCommandService commandService;

    public DesertLiveUserController(
            DesertLiveQueryService queryService,
            DesertLiveCommandService commandService) {

        this.queryService = queryService;
        this.commandService = commandService;
    }

    @PostMapping
    public DesertLiveItemResponse createMyItem(
            Authentication authentication,
            @Valid @RequestBody DesertLiveCreateRequest request) {

        return commandService.createMyItem(
                authentication.getName(),
                request
        );
    }

    @GetMapping
    public DesertLivePageResponse getMyItems(
            Authentication authentication,
            @RequestParam(required = false)
            DesertLiveModerationStatus status,
            @RequestParam(required = false) DesertLiveCategory category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return queryService.getMyItems(
                authentication.getName(),
                status,
                category,
                search,
                page,
                size
        );
    }

    @GetMapping("/{id}")
    public DesertLiveItemResponse getMyItem(
            Authentication authentication,
            @PathVariable Long id) {

        return queryService.getMyItem(
                authentication.getName(),
                id
        );
    }

    @PutMapping("/{id}")
    public DesertLiveItemResponse updateMyItem(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DesertLiveUpdateRequest request) {

        return commandService.updateMyItem(
                authentication.getName(),
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMyItem(
            Authentication authentication,
            @PathVariable Long id) {

        commandService.deleteMyItem(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(
            value = "/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DesertLiveItemResponse updateMyItemImage(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile image,
            @RequestParam(defaultValue = "50") int focusX,
            @RequestParam(defaultValue = "50") int focusY) {

        return commandService.updateMyItemImage(
                authentication.getName(),
                id,
                image,
                focusX,
                focusY
        );
    }

    @PutMapping("/{id}/image/focus")
    public DesertLiveItemResponse updateMyItemImageFocus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ImageFocusRequest request) {

        return commandService.updateMyItemImageFocus(
                authentication.getName(),
                id,
                request.focusX,
                request.focusY
        );
    }

    @DeleteMapping("/{id}/image")
    public DesertLiveItemResponse deleteMyItemImage(
            Authentication authentication,
            @PathVariable Long id) {

        return commandService.deleteMyItemImage(
                authentication.getName(),
                id
        );
    }
}
