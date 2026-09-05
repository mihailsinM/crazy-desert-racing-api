package com.crazydesert.racing.service;

import com.crazydesert.racing.ImageFraming;
import com.crazydesert.racing.MediaImage;
import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.ImageFramingProfileRequest;
import com.crazydesert.racing.dto.ImageFramingRequest;
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
        assertFraming(savedRaceCar, 50, 50, 0, 50, 50, 0);
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
        assertFraming(savedRaceCar, 25, 75, 10, 25, 75, 10);
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
        ReflectionTestUtils.setField(raceCar, "cardImageFraming", null);
        ReflectionTestUtils.setField(raceCar, "avatarImageFraming", null);

        assertEquals(50, raceCar.getImageFocusX());
        assertEquals(50, raceCar.getImageFocusY());
        assertEquals(0, raceCar.getImageCropPercent());
        assertFraming(raceCar, 50, 50, 0, 50, 50, 0);
    }

    @Test
    void usesLegacyCardFramingAsAvatarFallback() {
        RaceCar raceCar = new RaceCar();
        raceCar.applyCardImageFraming(20, 80, 15);
        ReflectionTestUtils.setField(raceCar, "avatarImageFraming", null);

        assertFraming(raceCar, 20, 80, 15, 20, 80, 15);
    }

    @Test
    void usesCardFallbackWhenLegacyAvatarColumnsAreAllNull() {
        RaceCar raceCar = new RaceCar();
        raceCar.applyCardImageFraming(30, 70, 10);
        ImageFraming emptyAvatarFraming = new ImageFraming();
        ReflectionTestUtils.setField(emptyAvatarFraming, "focusX", null);
        ReflectionTestUtils.setField(emptyAvatarFraming, "focusY", null);
        ReflectionTestUtils.setField(emptyAvatarFraming, "cropPercent", null);
        ReflectionTestUtils.setField(
                raceCar,
                "avatarImageFraming",
                emptyAvatarFraming
        );

        assertFraming(raceCar, 30, 70, 10, 30, 70, 10);
    }

    @Test
    void storesUploadedImageAndAppliesDifferentProfiles() {
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
                explicitFraming(
                        25,
                        35,
                        20,
                        70,
                        80,
                        5
                )
        );

        assertEquals(
                "/media/images/opaque-key?v=123",
                updatedRaceCar.getImageUrl()
        );
        assertFraming(updatedRaceCar, 25, 35, 20, 70, 80, 5);
    }

    @Test
    void replacesUploadedImageUsingTheExistingMediaImageKey() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 50, 50);
        raceCar.setImageKey("existing-key");
        MockMultipartFile uploadedImage = new MockMultipartFile(
                "file",
                "replacement.webp",
                "image/webp",
                new byte[]{4, 5, 6}
        );
        MediaImage mediaImage = new MediaImage();
        mediaImage.setImageKey("existing-key");
        mediaImage.setImageVersion(456L);

        stubOwnedRaceCar(owner, raceCar);
        when(mediaImageService.storeImage(
                "existing-key",
                uploadedImage,
                MediaImageVisibility.PUBLIC
        )).thenReturn(mediaImage);

        RaceCar updatedRaceCar = raceCarService.updateRaceCarImage(
                owner.getEmail(),
                10L,
                uploadedImage,
                explicitFraming(25, 35, 20, 70, 80, 5)
        );

        assertEquals(
                "/media/images/existing-key?v=456",
                updatedRaceCar.getImageUrl()
        );
        verify(mediaImageService).storeImage(
                "existing-key",
                uploadedImage,
                MediaImageVisibility.PUBLIC
        );
    }

    @Test
    void rejectsInvalidUploadFramingBeforeStoringImage() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 50, 50);
        MockMultipartFile uploadedImage = new MockMultipartFile(
                "file",
                "car.webp",
                "image/webp",
                new byte[]{1, 2, 3}
        );

        stubRaceCarAccess(owner, raceCar);

        assertThrows(
                InvalidImageFramingException.class,
                () -> raceCarService.updateRaceCarImage(
                        owner.getEmail(),
                        10L,
                        uploadedImage,
                        explicitFraming(25, 35, 12, 70, 80, 5)
                )
        );
        verifyNoInteractions(mediaImageService);
    }

    @Test
    void updatesDifferentProfilesWithoutReplacingImage() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 50, 50);

        stubOwnedRaceCar(owner, raceCar);

        RaceCar updatedRaceCar =
                raceCarService.updateRaceCarImageFraming(
                        owner.getEmail(),
                        10L,
                        explicitFraming(
                                15,
                                35,
                                20,
                                80,
                                65,
                                5
                        )
                );

        assertFraming(updatedRaceCar, 15, 35, 20, 80, 65, 5);
        verifyNoInteractions(mediaImageService);
    }

    @Test
    void keepsLegacyFramingUpdateCompatible() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 50, 50);
        ImageFramingRequest legacyRequest = new ImageFramingRequest();
        legacyRequest.focusX = 15;
        legacyRequest.focusY = 85;
        legacyRequest.cropPercent = 20;

        stubOwnedRaceCar(owner, raceCar);

        RaceCar updatedRaceCar =
                raceCarService.updateRaceCarImageFraming(
                        owner.getEmail(),
                        10L,
                        legacyRequest
                );

        assertFraming(updatedRaceCar, 15, 85, 20, 15, 85, 20);
    }

    @Test
    void rejectsInvalidAvatarProfile() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 50, 50);
        ImageFramingRequest request = explicitFraming(
                -1,
                35,
                20,
                80,
                65,
                5
        );

        stubRaceCarAccess(owner, raceCar);

        assertThrows(
                InvalidImageFocusException.class,
                () -> raceCarService.updateRaceCarImageFraming(
                        owner.getEmail(),
                        10L,
                        request
                )
        );
    }

    @Test
    void rejectsInvalidCardProfileWithoutChangingAvatar() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 50, 50);
        ImageFramingRequest request = explicitFraming(
                15,
                35,
                20,
                80,
                65,
                12
        );

        stubRaceCarAccess(owner, raceCar);

        assertThrows(
                InvalidImageFramingException.class,
                () -> raceCarService.updateRaceCarImageFraming(
                        owner.getEmail(),
                        10L,
                        request
                )
        );
        assertFraming(raceCar, 50, 50, 0, 50, 50, 0);
    }

    @Test
    void rejectsIncompleteExplicitProfiles() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 50, 50);
        ImageFramingRequest request = new ImageFramingRequest();
        request.avatar = new ImageFramingProfileRequest(25, 35, 10);

        stubRaceCarAccess(owner, raceCar);

        assertThrows(
                InvalidImageFramingException.class,
                () -> raceCarService.updateRaceCarImageFraming(
                        owner.getEmail(),
                        10L,
                        request
                )
        );
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
                        explicitFraming(
                                50,
                                50,
                                0,
                                50,
                                50,
                                0
                        )
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
                null
        );

        assertEquals(
                "/media/images/admin-upload-key?v=456",
                updatedRaceCar.getImageUrl()
        );
        assertFraming(updatedRaceCar, 50, 50, 0, 50, 50, 0);
    }

    @Test
    void allowsAdminToUpdateBothFramingProfiles() {
        User owner = createUser(1L, "owner@example.com");
        User admin = createUser(2L, "admin@example.com");
        admin.setRole(Role.ADMIN);
        RaceCar raceCar = createRaceCar(owner, 50, 50);

        when(userRepository.findByEmail(admin.getEmail()))
                .thenReturn(Optional.of(admin));
        when(raceCarRepository.findById(10L))
                .thenReturn(Optional.of(raceCar));
        when(raceCarRepository.save(raceCar)).thenReturn(raceCar);

        RaceCar updatedRaceCar =
                raceCarService.updateRaceCarImageFraming(
                        admin.getEmail(),
                        10L,
                        explicitFraming(
                                25,
                                40,
                                15,
                                75,
                                60,
                                5
                        )
                );

        assertFraming(updatedRaceCar, 25, 40, 15, 75, 60, 5);
    }

    @Test
    void rejectsFramingUpdateFromAnotherUser() {
        User owner = createUser(1L, "owner@example.com");
        User otherUser = createUser(2L, "other@example.com");
        RaceCar raceCar = createRaceCar(owner, 50, 50);

        when(userRepository.findByEmail(otherUser.getEmail()))
                .thenReturn(Optional.of(otherUser));
        when(raceCarRepository.findById(10L))
                .thenReturn(Optional.of(raceCar));

        assertThrows(
                RaceCarOwnershipException.class,
                () -> raceCarService.updateRaceCarImageFraming(
                        otherUser.getEmail(),
                        10L,
                        explicitFraming(
                                25,
                                40,
                                15,
                                75,
                                60,
                                5
                        )
                )
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
        assertFraming(updatedRaceCar, 50, 50, 0, 50, 50, 0);
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

    private ImageFramingRequest explicitFraming(
            int avatarFocusX,
            int avatarFocusY,
            int avatarCropPercent,
            int cardFocusX,
            int cardFocusY,
            int cardCropPercent) {

        ImageFramingRequest request = new ImageFramingRequest();
        request.avatar = new ImageFramingProfileRequest(
                avatarFocusX,
                avatarFocusY,
                avatarCropPercent
        );
        request.card = new ImageFramingProfileRequest(
                cardFocusX,
                cardFocusY,
                cardCropPercent
        );

        return request;
    }

    private void assertFraming(
            RaceCar raceCar,
            int avatarFocusX,
            int avatarFocusY,
            int avatarCropPercent,
            int cardFocusX,
            int cardFocusY,
            int cardCropPercent) {

        assertEquals(
                avatarFocusX,
                raceCar.getImageFraming().avatar().focusX()
        );
        assertEquals(
                avatarFocusY,
                raceCar.getImageFraming().avatar().focusY()
        );
        assertEquals(
                avatarCropPercent,
                raceCar.getImageFraming().avatar().cropPercent()
        );
        assertEquals(
                cardFocusX,
                raceCar.getImageFraming().card().focusX()
        );
        assertEquals(
                cardFocusY,
                raceCar.getImageFraming().card().focusY()
        );
        assertEquals(
                cardCropPercent,
                raceCar.getImageFraming().card().cropPercent()
        );
    }

    private void stubRaceCarSave() {
        when(raceCarRepository.save(any(RaceCar.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stubOwnedRaceCar(User owner, RaceCar raceCar) {
        stubRaceCarAccess(owner, raceCar);
        when(raceCarRepository.save(raceCar)).thenReturn(raceCar);
    }

    private void stubRaceCarAccess(User owner, RaceCar raceCar) {
        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(raceCarRepository.findById(10L))
                .thenReturn(Optional.of(raceCar));
    }
}
