package com.crazydesert.racing.service;

import com.crazydesert.racing.MediaImage;
import com.crazydesert.racing.dto.MediaImageResponse;
import com.crazydesert.racing.enums.MediaImageVisibility;
import com.crazydesert.racing.exception.MediaImageNotFoundException;
import com.crazydesert.racing.repository.MediaImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Transactional
public class MediaImageService {

    private final MediaImageRepository mediaImageRepository;
    private final ImageUploadValidator imageUploadValidator;

    public MediaImageService(
            MediaImageRepository mediaImageRepository,
            ImageUploadValidator imageUploadValidator) {

        this.mediaImageRepository = mediaImageRepository;
        this.imageUploadValidator = imageUploadValidator;
    }

    public MediaImage storeImage(
            String existingImageKey,
            MultipartFile uploadedImage) {

        return storeImage(
                existingImageKey,
                uploadedImage,
                MediaImageVisibility.PRIVATE
        );
    }

    public MediaImage storeImage(
            String existingImageKey,
            MultipartFile uploadedImage,
            MediaImageVisibility visibility) {

        byte[] imageData = imageUploadValidator.validateAndRead(uploadedImage);
        MediaImage mediaImage = findExistingImage(existingImageKey);

        if (mediaImage.getImageKey() == null) {
            mediaImage.setImageKey(UUID.randomUUID().toString());
        }

        mediaImage.setData(imageData);
        mediaImage.setContentType(uploadedImage.getContentType());
        mediaImage.setVisibility(
                visibility == null
                        ? MediaImageVisibility.PRIVATE
                        : visibility
        );
        mediaImage.setImageVersion(
                Math.max(
                        System.currentTimeMillis(),
                        mediaImage.getImageVersion() + 1
                )
        );

        return mediaImageRepository.save(mediaImage);
    }

    public void deleteImage(String imageKey) {
        if (imageKey == null) {
            return;
        }

        mediaImageRepository.findByImageKey(imageKey)
                .ifPresent(mediaImageRepository::delete);
    }

    @Transactional(readOnly = true)
    public MediaImageResponse getAuthorizedImage(String imageKey) {
        MediaImage mediaImage = mediaImageRepository.findByImageKey(imageKey)
                .orElseThrow(() ->
                        new MediaImageNotFoundException(
                                "Media image not found"
                        ));

        return new MediaImageResponse(
                mediaImage.getData(),
                mediaImage.getContentType()
        );
    }

    @Transactional(readOnly = true)
    public MediaImageResponse getPublicImage(String imageKey) {
        MediaImage mediaImage = mediaImageRepository
                .findByImageKeyAndVisibility(
                        imageKey,
                        MediaImageVisibility.PUBLIC
                )
                .orElseThrow(() ->
                        new MediaImageNotFoundException(
                                "Media image not found"
                        ));

        return new MediaImageResponse(
                mediaImage.getData(),
                mediaImage.getContentType()
        );
    }

    private MediaImage findExistingImage(String imageKey) {
        if (imageKey == null) {
            return new MediaImage();
        }

        return mediaImageRepository.findByImageKey(imageKey)
                .orElseGet(MediaImage::new);
    }
}
