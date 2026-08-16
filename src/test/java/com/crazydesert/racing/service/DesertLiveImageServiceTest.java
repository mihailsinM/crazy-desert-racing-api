package com.crazydesert.racing.service;

import com.crazydesert.racing.DesertLiveImage;
import com.crazydesert.racing.DesertLiveItem;
import com.crazydesert.racing.dto.DesertLiveImageResponse;
import com.crazydesert.racing.repository.DesertLiveImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DesertLiveImageServiceTest {

    @Mock
    private DesertLiveImageRepository imageRepository;

    @Mock
    private ImageUploadValidator imageUploadValidator;

    private DesertLiveImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new DesertLiveImageService(
                imageRepository,
                imageUploadValidator
        );
    }

    @Test
    void storesImageInSeparateEntityAndAddsOpaqueUrlKey() {
        DesertLiveItem item = new DesertLiveItem();
        ReflectionTestUtils.setField(item, "id", 10L);
        byte[] imageData = new byte[]{1, 2, 3};
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "desert.webp",
                "image/webp",
                imageData
        );

        when(imageUploadValidator.validateAndRead(image))
                .thenReturn(imageData);
        when(imageRepository.findByItemId(10L))
                .thenReturn(Optional.empty());

        imageService.storeImage(item, image);

        ArgumentCaptor<DesertLiveImage> imageCaptor =
                ArgumentCaptor.forClass(DesertLiveImage.class);
        verify(imageRepository).save(imageCaptor.capture());

        assertEquals(item, imageCaptor.getValue().getItem());
        assertArrayEquals(imageData, imageCaptor.getValue().getData());
        assertEquals("image/webp", imageCaptor.getValue().getContentType());
        assertNotNull(item.getImageKey());
    }

    @Test
    void returnsStoredImageByOpaqueKey() {
        DesertLiveImage image = new DesertLiveImage();
        image.setData(new byte[]{4, 5, 6});
        image.setContentType("image/png");

        when(imageRepository.findByItem_ImageKey("opaque-key"))
                .thenReturn(Optional.of(image));

        DesertLiveImageResponse response =
                imageService.getImage("opaque-key");

        assertArrayEquals(new byte[]{4, 5, 6}, response.data());
        assertEquals("image/png", response.contentType());
    }
}
