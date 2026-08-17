package com.crazydesert.racing.controller;

import com.crazydesert.racing.dto.DesertLiveCreateRequest;
import com.crazydesert.racing.dto.DesertLiveItemResponse;
import com.crazydesert.racing.dto.DesertLivePageResponse;
import com.crazydesert.racing.dto.DesertLiveRejectRequest;
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
@RequestMapping("/desert-live/admin")
public class DesertLiveAdminController {

    private final DesertLiveQueryService queryService;
    private final DesertLiveCommandService commandService;

    public DesertLiveAdminController(
            DesertLiveQueryService queryService,
            DesertLiveCommandService commandService) {

        this.queryService = queryService;
        this.commandService = commandService;
    }

    @PostMapping
    public DesertLiveItemResponse createAdminItem(
            Authentication authentication,
            @Valid @RequestBody DesertLiveCreateRequest request) {

        return commandService.createAdminItem(
                authentication.getName(),
                request
        );
    }

    @GetMapping
    public DesertLivePageResponse getAdminItems(
            @RequestParam(required = false)
            DesertLiveModerationStatus status,
            @RequestParam(required = false) DesertLiveCategory category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return queryService.getAdminItems(
                status,
                category,
                search,
                page,
                size
        );
    }

    @GetMapping("/{id}")
    public DesertLiveItemResponse getAdminItem(@PathVariable Long id) {
        return queryService.getAdminItem(id);
    }

    @PutMapping("/{id}")
    public DesertLiveItemResponse updateAdminItem(
            @PathVariable Long id,
            @Valid @RequestBody DesertLiveUpdateRequest request) {

        return commandService.updateAdminItem(id, request);
    }

    @PutMapping("/{id}/approve")
    public DesertLiveItemResponse approveItem(
            Authentication authentication,
            @PathVariable Long id) {

        return commandService.approveItem(
                authentication.getName(),
                id
        );
    }

    @PutMapping("/{id}/reject")
    public DesertLiveItemResponse rejectItem(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DesertLiveRejectRequest request) {

        return commandService.rejectItem(
                authentication.getName(),
                id,
                request.reason
        );
    }

    @PutMapping(
            value = "/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DesertLiveItemResponse updateAdminItemImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile image,
            @RequestParam(defaultValue = "50") int focusX,
            @RequestParam(defaultValue = "50") int focusY) {

        return commandService.updateAdminItemImage(
                id,
                image,
                focusX,
                focusY
        );
    }

    @PutMapping("/{id}/image/focus")
    public DesertLiveItemResponse updateAdminItemImageFocus(
            @PathVariable Long id,
            @Valid @RequestBody ImageFocusRequest request) {

        return commandService.updateAdminItemImageFocus(
                id,
                request.focusX,
                request.focusY
        );
    }

    @DeleteMapping("/{id}/image")
    public DesertLiveItemResponse deleteAdminItemImage(
            @PathVariable Long id) {

        return commandService.deleteAdminItemImage(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdminItem(@PathVariable Long id) {
        commandService.deleteAdminItem(id);
        return ResponseEntity.noContent().build();
    }
}
