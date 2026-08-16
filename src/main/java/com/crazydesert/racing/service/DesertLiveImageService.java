package com.crazydesert.racing.service;

import com.crazydesert.racing.DesertLiveImage;
import com.crazydesert.racing.DesertLiveItem;
import com.crazydesert.racing.dto.DesertLiveImageResponse;
import com.crazydesert.racing.exception.DesertLiveImageNotFoundException;
import com.crazydesert.racing.repository.DesertLiveImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Transactional
public class DesertLiveImageService {

    private final DesertLiveImageRepository imageRepository;
    private final ImageUploadValidator imageUploadValidator;

    public DesertLiveImageService(
            DesertLiveImageRepository imageRepository,
            ImageUploadValidator imageUploadValidator) {

        this.imageRepository = imageRepository;
        this.imageUploadValidator = imageUploadValidator;
    }

    public void storeImage(
            DesertLiveItem item,
            MultipartFile uploadedImage) {

        byte[] imageData = imageUploadValidator.validateAndRead(uploadedImage);
        DesertLiveImage image = imageRepository.findByItemId(item.getId())
                .orElseGet(DesertLiveImage::new);

        image.setItem(item);
        image.setData(imageData);
        image.setContentType(uploadedImage.getContentType());

        if (item.getImageKey() == null) {
            item.setImageKey(UUID.randomUUID().toString());
        }

        item.setImageVersion(System.currentTimeMillis());
        imageRepository.save(image);
    }

    public void deleteImage(DesertLiveItem item) {
        if (item.getImageKey() == null) {
            throw new DesertLiveImageNotFoundException(
                    "Desert Live item does not have an image"
            );
        }

        imageRepository.deleteByItemId(item.getId());
        item.setImageKey(null);
        item.setImageVersion(System.currentTimeMillis());
    }

    public void deleteImageIfPresent(Long itemId) {
        imageRepository.deleteByItemId(itemId);
    }

    @Transactional(readOnly = true)
    public DesertLiveImageResponse getImage(String imageKey) {
        DesertLiveImage image = imageRepository.findByItem_ImageKey(imageKey)
                .orElseThrow(() ->
                        new DesertLiveImageNotFoundException(
                                "Desert Live image not found"
                        ));

        return new DesertLiveImageResponse(
                image.getData(),
                image.getContentType()
        );
    }
}
