package com.crazydesert.racing.service;

import com.crazydesert.racing.MediaImage;
import com.crazydesert.racing.dto.MediaImageResponse;
import com.crazydesert.racing.enums.MediaImageVisibility;
import com.crazydesert.racing.exception.MediaImageNotFoundException;
import com.crazydesert.racing.repository.MediaImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaImageServiceTest {

    @Mock
    private MediaImageRepository mediaImageRepository;

    @Mock
    private ImageUploadValidator imageUploadValidator;

    private MediaImageService mediaImageService;

    @BeforeEach
    void setUp() {
        mediaImageService = new MediaImageService(
                mediaImageRepository,
                imageUploadValidator
        );
    }

    @Test
    void storesNewImageWithOpaqueKey() {
        byte[] imageData = new byte[]{1, 2, 3};
        MockMultipartFile uploadedImage = new MockMultipartFile(
                "file",
                "car.webp",
                "image/webp",
                imageData
        );

        when(imageUploadValidator.validateAndRead(uploadedImage))
                .thenReturn(imageData);
        when(mediaImageRepository.save(any(MediaImage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MediaImage storedImage =
                mediaImageService.storeImage(null, uploadedImage);

        ArgumentCaptor<MediaImage> imageCaptor =
                ArgumentCaptor.forClass(MediaImage.class);
        verify(mediaImageRepository).save(imageCaptor.capture());

        assertSame(storedImage, imageCaptor.getValue());
        assertNotNull(storedImage.getImageKey());
        assertArrayEquals(imageData, storedImage.getData());
        assertEquals("image/webp", storedImage.getContentType());
        assertEquals(MediaImageVisibility.PRIVATE, storedImage.getVisibility());
    }

    @Test
    void storesPublicImageWhenExplicitlyRequested() {
        byte[] imageData = new byte[]{1, 2, 3};
        MockMultipartFile uploadedImage = new MockMultipartFile(
                "file",
                "car.webp",
                "image/webp",
                imageData
        );

        when(imageUploadValidator.validateAndRead(uploadedImage))
                .thenReturn(imageData);
        when(mediaImageRepository.save(any(MediaImage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MediaImage storedImage = mediaImageService.storeImage(
                null,
                uploadedImage,
                MediaImageVisibility.PUBLIC
        );

        assertEquals(MediaImageVisibility.PUBLIC, storedImage.getVisibility());
    }

    @Test
    void replacesImageWithoutChangingOpaqueKey() {
        MediaImage existingImage = new MediaImage();
        existingImage.setImageKey("existing-key");
        byte[] imageData = new byte[]{4, 5, 6};
        MockMultipartFile uploadedImage = new MockMultipartFile(
                "file",
                "replacement.png",
                "image/png",
                imageData
        );

        when(imageUploadValidator.validateAndRead(uploadedImage))
                .thenReturn(imageData);
        when(mediaImageRepository.save(existingImage))
                .thenReturn(existingImage);

        when(mediaImageRepository.findByImageKey("existing-key"))
                .thenReturn(Optional.of(existingImage));

        MediaImage storedImage = mediaImageService.storeImage(
                "existing-key",
                uploadedImage
        );

        assertSame(existingImage, storedImage);
        assertEquals("existing-key", storedImage.getImageKey());
        assertArrayEquals(imageData, storedImage.getData());
        assertEquals("image/png", storedImage.getContentType());
    }

    @Test
    void returnsStoredImageByOpaqueKey() {
        MediaImage storedImage = new MediaImage();
        storedImage.setData(new byte[]{7, 8, 9});
        storedImage.setContentType("image/jpeg");

        when(mediaImageRepository.findByImageKey("opaque-key"))
                .thenReturn(Optional.of(storedImage));

        MediaImageResponse response =
                mediaImageService.getAuthorizedImage("opaque-key");

        assertArrayEquals(new byte[]{7, 8, 9}, response.data());
        assertEquals("image/jpeg", response.contentType());
    }

    @Test
    void rejectsUnknownOpaqueKey() {
        when(mediaImageRepository.findByImageKey("missing-key"))
                .thenReturn(Optional.empty());

        assertThrows(
                MediaImageNotFoundException.class,
                () -> mediaImageService.getAuthorizedImage("missing-key")
        );
    }

    @Test
    void returnsPublicImageByOpaqueKey() {
        MediaImage storedImage = new MediaImage();
        storedImage.setData(new byte[]{7, 8, 9});
        storedImage.setContentType("image/jpeg");
        storedImage.setVisibility(MediaImageVisibility.PUBLIC);

        when(mediaImageRepository.findByImageKeyAndVisibility(
                "public-key",
                MediaImageVisibility.PUBLIC
        )).thenReturn(Optional.of(storedImage));

        MediaImageResponse response =
                mediaImageService.getPublicImage("public-key");

        assertArrayEquals(new byte[]{7, 8, 9}, response.data());
        assertEquals("image/jpeg", response.contentType());
    }

    @Test
    void doesNotExposePrivateImageThroughPublicLookup() {
        when(mediaImageRepository.findByImageKeyAndVisibility(
                "private-key",
                MediaImageVisibility.PUBLIC
        )).thenReturn(Optional.empty());

        assertThrows(
                MediaImageNotFoundException.class,
                () -> mediaImageService.getPublicImage("private-key")
        );
    }

    @Test
    void deletesStoredImageByOpaqueKey() {
        MediaImage storedImage = new MediaImage();
        storedImage.setImageKey("opaque-key");

        when(mediaImageRepository.findByImageKey("opaque-key"))
                .thenReturn(Optional.of(storedImage));

        mediaImageService.deleteImage("opaque-key");

        verify(mediaImageRepository).delete(storedImage);
    }

    @Test
    void ignoresDeleteWhenImageKeyIsMissing() {
        mediaImageService.deleteImage(null);

        verifyNoInteractions(mediaImageRepository);
    }
}
