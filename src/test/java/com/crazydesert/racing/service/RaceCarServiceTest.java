package com.crazydesert.racing.service;

import com.crazydesert.racing.MediaImage;
import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.RaceCarCreateRequest;
import com.crazydesert.racing.dto.RaceCarUpdateRequest;
import com.crazydesert.racing.enums.MediaImageVisibility;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.exception.InvalidImageFramingException;
import com.crazydesert.racing.exception.InvalidImageFocusException;
import com.crazydesert.racing.exception.RaceCarOwnershipException;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaceCarServiceTest {

    @Mock
    private RaceCarRepository raceCarRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MediaImageService mediaImageService;

    private RaceCarService raceCarService;

    @BeforeEach
    void setUp() {
        ImageFocusValidator imageFocusValidator =
                new ImageFocusValidator();

        raceCarService = new RaceCarService(
                raceCarRepository,
                userRepository,
                new ImageFramingValidator(imageFocusValidator),
                mediaImageService
        );
    }

    @Test
    void createsRaceCarWithCenteredFocusByDefault() {
        RaceCarCreateRequest request = createRequest();
        stubRaceCarSave();

        RaceCar savedRaceCar = raceCarService.createRaceCar(request);

        assertEquals(50, savedRaceCar.getImageFocusX());
        assertEquals(50, savedRaceCar.getImageFocusY());
        assertEquals(0, savedRaceCar.getImageCropPercent());
    }

    @Test
    void createsRaceCarWithCustomFocus() {
        RaceCarCreateRequest request = createRequest();
        request.imageFocusX = 25;
        request.imageFocusY = 75;
        request.imageCropPercent = 10;
        stubRaceCarSave();

        RaceCar savedRaceCar = raceCarService.createRaceCar(request);

        assertEquals(25, savedRaceCar.getImageFocusX());
        assertEquals(75, savedRaceCar.getImageFocusY());
        assertEquals(10, savedRaceCar.getImageCropPercent());
    }

    @Test
    void rejectsIncompleteOrOutOfRangeFocus() {
        RaceCarCreateRequest incompleteRequest = createRequest();
        incompleteRequest.imageFocusX = 25;

        assertThrows(
                InvalidImageFocusException.class,
                () -> raceCarService.createRaceCar(incompleteRequest)
        );

        RaceCarCreateRequest outOfRangeRequest = createRequest();
        outOfRangeRequest.imageFocusX = -1;
        outOfRangeRequest.imageFocusY = 101;

        assertThrows(
                InvalidImageFocusException.class,
                () -> raceCarService.createRaceCar(outOfRangeRequest)
        );
        verifyNoInteractions(raceCarRepository);
    }

    @Test
    void rejectsUnsupportedCropPercent() {
        RaceCarCreateRequest request = createRequest();
        request.imageCropPercent = 12;

        assertThrows(
                InvalidImageFramingException.class,
                () -> raceCarService.createRaceCar(request)
        );
        verifyNoInteractions(raceCarRepository);
    }

    @Test
    void preservesExistingFocusWhenUpdateOmitsCoordinates() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 25, 75);
        RaceCarUpdateRequest request = updateRequest();

        stubOwnedRaceCar(owner, raceCar);

        RaceCar updatedRaceCar = raceCarService.updateRaceCar(
                owner.getEmail(),
                10L,
                request
        );

        assertEquals(25, updatedRaceCar.getImageFocusX());
        assertEquals(75, updatedRaceCar.getImageFocusY());
        assertEquals(0, updatedRaceCar.getImageCropPercent());
    }

    @Test
    void updatesFocusWhenBothCoordinatesAreProvided() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 50, 50);
        RaceCarUpdateRequest request = updateRequest();
        request.imageFocusX = 20;
        request.imageFocusY = 80;

        stubOwnedRaceCar(owner, raceCar);

        RaceCar updatedRaceCar = raceCarService.updateRaceCar(
                owner.getEmail(),
                10L,
                request
        );

        assertEquals(20, updatedRaceCar.getImageFocusX());
        assertEquals(80, updatedRaceCar.getImageFocusY());
    }

    @Test
    void returnsDefaultFramingForLegacyCarWithoutMetadata() {
        RaceCar raceCar = new RaceCar();
        ReflectionTestUtils.setField(raceCar, "imageFraming", null);

        assertEquals(50, raceCar.getImageFocusX());
        assertEquals(50, raceCar.getImageFocusY());
        assertEquals(0, raceCar.getImageCropPercent());
    }

    @Test
    void storesUploadedImageAndAppliesFraming() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 50, 50);
        MockMultipartFile uploadedImage = new MockMultipartFile(
                "file",
                "car.webp",
                "image/webp",
                new byte[]{1, 2, 3}
        );
        MediaImage mediaImage = new MediaImage();
        mediaImage.setImageKey("opaque-key");
        mediaImage.setImageVersion(123L);

        stubOwnedRaceCar(owner, raceCar);
        when(mediaImageService.storeImage(
                null,
                uploadedImage,
                MediaImageVisibility.PUBLIC
        ))
                .thenReturn(mediaImage);

        RaceCar updatedRaceCar = raceCarService.updateRaceCarImage(
                owner.getEmail(),
                10L,
                uploadedImage,
                20,
                80,
                15
        );

        assertEquals(
                "/media/images/opaque-key?v=123",
                updatedRaceCar.getImageUrl()
        );
        assertEquals(20, updatedRaceCar.getImageFocusX());
        assertEquals(80, updatedRaceCar.getImageFocusY());
        assertEquals(15, updatedRaceCar.getImageCropPercent());
    }

    @Test
    void updatesFramingWithoutReplacingImage() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 50, 50);

        stubOwnedRaceCar(owner, raceCar);

        RaceCar updatedRaceCar =
                raceCarService.updateRaceCarImageFraming(
                        owner.getEmail(),
                        10L,
                        15,
                        85,
                        20
                );

        assertEquals(15, updatedRaceCar.getImageFocusX());
        assertEquals(85, updatedRaceCar.getImageFocusY());
        assertEquals(20, updatedRaceCar.getImageCropPercent());
    }

    @Test
    void rejectsImageUploadFromAnotherUser() {
        User owner = createUser(1L, "owner@example.com");
        User otherUser = createUser(2L, "other@example.com");
        RaceCar raceCar = createRaceCar(owner, 50, 50);
        MockMultipartFile uploadedImage = new MockMultipartFile(
                "file",
                "car.webp",
                "image/webp",
                new byte[]{1, 2, 3}
        );

        when(userRepository.findByEmail(otherUser.getEmail()))
                .thenReturn(Optional.of(otherUser));
        when(raceCarRepository.findById(10L))
                .thenReturn(Optional.of(raceCar));

        assertThrows(
                RaceCarOwnershipException.class,
                () -> raceCarService.updateRaceCarImage(
                        otherUser.getEmail(),
                        10L,
                        uploadedImage,
                        50,
                        50,
                        0
                )
        );
        verifyNoInteractions(mediaImageService);
    }

    @Test
    void allowsAdminToUploadImage() {
        User owner = createUser(1L, "owner@example.com");
        User admin = createUser(2L, "admin@example.com");
        admin.setRole(Role.ADMIN);
        RaceCar raceCar = createRaceCar(owner, 50, 50);
        MockMultipartFile uploadedImage = new MockMultipartFile(
                "file",
                "car.png",
                "image/png",
                new byte[]{1, 2, 3}
        );
        MediaImage mediaImage = new MediaImage();
        mediaImage.setImageKey("admin-upload-key");
        mediaImage.setImageVersion(456L);

        when(userRepository.findByEmail(admin.getEmail()))
                .thenReturn(Optional.of(admin));
        when(raceCarRepository.findById(10L))
                .thenReturn(Optional.of(raceCar));
        when(mediaImageService.storeImage(
                null,
                uploadedImage,
                MediaImageVisibility.PUBLIC
        ))
                .thenReturn(mediaImage);
        when(raceCarRepository.save(raceCar)).thenReturn(raceCar);

        RaceCar updatedRaceCar = raceCarService.updateRaceCarImage(
                admin.getEmail(),
                10L,
                uploadedImage,
                50,
                50,
                0
        );

        assertEquals(
                "/media/images/admin-upload-key?v=456",
                updatedRaceCar.getImageUrl()
        );
    }

    @Test
    void deletesStoredImageAndResetsFraming() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 15, 85);
        raceCar.setImageCropPercent(20);
        raceCar.setImageKey("stored-image-key");
        raceCar.setImageVersion(123L);

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(raceCarRepository.findById(10L))
                .thenReturn(Optional.of(raceCar));
        when(raceCarRepository.saveAndFlush(raceCar))
                .thenReturn(raceCar);

        RaceCar updatedRaceCar = raceCarService.deleteRaceCarImage(
                owner.getEmail(),
                10L
        );

        assertNull(updatedRaceCar.getImageUrl());
        assertEquals(50, updatedRaceCar.getImageFocusX());
        assertEquals(50, updatedRaceCar.getImageFocusY());
        assertEquals(0, updatedRaceCar.getImageCropPercent());
        verify(mediaImageService).deleteImage("stored-image-key");
    }

    private RaceCarCreateRequest createRequest() {
        RaceCarCreateRequest request = new RaceCarCreateRequest();
        request.name = "Desert Storm";
        request.brand = "BMW";
        request.horsePower = 500;
        request.imageUrl = "https://example.com/car.jpg";
        request.imagePosition = "CENTER";

        return request;
    }

    private RaceCarUpdateRequest updateRequest() {
        RaceCarUpdateRequest request = new RaceCarUpdateRequest();
        request.name = "Desert Storm Updated";
        request.brand = "BMW";
        request.horsePower = 520;
        request.imageUrl = "https://example.com/car-updated.jpg";
        request.imagePosition = "CENTER";

        return request;
    }

    private User createUser(Long id, String email) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setName("Racer");
        user.setAge(38);
        user.setEmail(email);
        user.setLicenseCategory("B");
        user.setRole(Role.USER);

        return user;
    }

    private RaceCar createRaceCar(
            User owner,
            int focusX,
            int focusY) {

        RaceCar raceCar = new RaceCar();
        ReflectionTestUtils.setField(raceCar, "id", 10L);
        raceCar.setName("Desert Storm");
        raceCar.setBrand("BMW");
        raceCar.setHorsePower(500);
        raceCar.setImageUrl("https://example.com/car.jpg");
        raceCar.setImagePosition("CENTER");
        raceCar.setImageFocusX(focusX);
        raceCar.setImageFocusY(focusY);
        raceCar.setOwner(owner);

        return raceCar;
    }

    private void stubRaceCarSave() {
        when(raceCarRepository.save(any(RaceCar.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stubOwnedRaceCar(User owner, RaceCar raceCar) {
        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(raceCarRepository.findById(10L))
                .thenReturn(Optional.of(raceCar));
        when(raceCarRepository.save(raceCar)).thenReturn(raceCar);
    }
}
