package com.crazydesert.racing.controller;

import com.crazydesert.racing.dto.ImageFramingRequest;
import com.crazydesert.racing.dto.MediaImageResponse;
import com.crazydesert.racing.dto.PhotoReportReviewRequest;
import com.crazydesert.racing.dto.UserPhotoReportRequest;
import com.crazydesert.racing.dto.UserPhotoReportResponse;
import com.crazydesert.racing.dto.UserPhotoResponse;
import com.crazydesert.racing.dto.UserPhotoUpdateRequest;
import com.crazydesert.racing.enums.UserPhotoVisibility;
import com.crazydesert.racing.service.UserPhotoService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

@RestController
public class UserPhotoController {

    private final UserPhotoService userPhotoService;

    public UserPhotoController(UserPhotoService userPhotoService) {
        this.userPhotoService = userPhotoService;
    }

    @GetMapping("/users/me/photos")
    public List<UserPhotoResponse> getCurrentUserPhotos(
            Authentication authentication) {

        return userPhotoService.getCurrentUserPhotos(
                authentication.getName()
        );
    }

    @PostMapping(
            value = "/users/me/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public UserPhotoResponse uploadPhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile image,
            @RequestParam boolean rightsConfirmed,
            @RequestParam(required = false) String caption,
            @RequestParam(required = false) UserPhotoVisibility visibility,
            @RequestParam(required = false) Integer focusX,
            @RequestParam(required = false) Integer focusY,
            @RequestParam(required = false) Integer cropPercent,
            @RequestParam(required = false) Integer avatarFocusX,
            @RequestParam(required = false) Integer avatarFocusY,
            @RequestParam(required = false) Integer avatarCropPercent,
            @RequestParam(required = false) Integer cardFocusX,
            @RequestParam(required = false) Integer cardFocusY,
            @RequestParam(required = false) Integer cardCropPercent) {

        return userPhotoService.uploadPhoto(
                authentication.getName(),
                image,
                rightsConfirmed,
                caption,
                visibility,
                ImageFramingRequest.fromParameters(
                        focusX,
                        focusY,
                        cropPercent,
                        avatarFocusX,
                        avatarFocusY,
                        avatarCropPercent,
                        cardFocusX,
                        cardFocusY,
                        cardCropPercent
                )
        );
    }

    @PutMapping("/users/me/photos/{photoId}")
    public UserPhotoResponse updatePhoto(
            Authentication authentication,
            @PathVariable Long photoId,
            @Valid @RequestBody UserPhotoUpdateRequest request) {

        return userPhotoService.updatePhoto(
                authentication.getName(),
                photoId,
                request
        );
    }

    @PutMapping("/users/me/photos/{photoId}/framing")
    public UserPhotoResponse updatePhotoFraming(
            Authentication authentication,
            @PathVariable Long photoId,
            @RequestBody ImageFramingRequest request) {

        return userPhotoService.updatePhotoFraming(
                authentication.getName(),
                photoId,
                request
        );
    }

    @PutMapping("/users/me/photos/{photoId}/profile")
    public UserPhotoResponse setProfilePhoto(
            Authentication authentication,
            @PathVariable Long photoId) {

        return userPhotoService.setProfilePhoto(
                authentication.getName(),
                photoId
        );
    }

    @PutMapping("/users/me/photos/{photoId}/card-profile")
    public UserPhotoResponse setProfileCardPhoto(
            Authentication authentication,
            @PathVariable Long photoId) {

        return userPhotoService.setProfileCardPhoto(
                authentication.getName(),
                photoId
        );
    }

    @DeleteMapping("/users/me/photos/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            Authentication authentication,
            @PathVariable Long photoId) {

        userPhotoService.deletePhoto(authentication.getName(), photoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/driver-photos/{photoId}/image")
    public ResponseEntity<byte[]> getPhotoImage(
            Authentication authentication,
            @PathVariable Long photoId) {

        MediaImageResponse image = userPhotoService.getPhotoImage(
                photoId,
                authentication.getName()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(
                        CacheControl.maxAge(Duration.ofDays(30))
                                .cachePrivate()
                )
                .body(image.data());
    }

    @PostMapping("/driver-photos/{photoId}/reports")
    public UserPhotoReportResponse reportPhoto(
            Authentication authentication,
            @PathVariable Long photoId,
            @Valid @RequestBody UserPhotoReportRequest request) {

        return userPhotoService.reportPhoto(
                photoId,
                authentication.getName(),
                request
        );
    }

    @GetMapping("/admin/photo-reports")
    public List<UserPhotoReportResponse> getOpenReports(
            Authentication authentication) {

        return userPhotoService.getOpenReports(authentication.getName());
    }

    @PutMapping("/admin/photo-reports/{reportId}")
    public UserPhotoReportResponse reviewReport(
            Authentication authentication,
            @PathVariable Long reportId,
            @Valid @RequestBody PhotoReportReviewRequest request) {

        return userPhotoService.reviewReport(
                reportId,
                authentication.getName(),
                request
        );
    }

    @PutMapping("/admin/user-photos/{photoId}/hide")
    public UserPhotoResponse hidePhotoAsAdmin(
            Authentication authentication,
            @PathVariable Long photoId) {

        return userPhotoService.hidePhotoAsAdmin(
                authentication.getName(),
                photoId
        );
    }

    @DeleteMapping("/admin/user-photos/{photoId}")
    public ResponseEntity<Void> deletePhotoAsAdmin(
            Authentication authentication,
            @PathVariable Long photoId) {

        userPhotoService.deletePhotoAsAdmin(
                authentication.getName(),
                photoId
        );
        return ResponseEntity.noContent().build();
    }
}
