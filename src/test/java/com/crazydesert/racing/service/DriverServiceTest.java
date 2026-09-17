package com.crazydesert.racing.service;

import com.crazydesert.racing.User;
import com.crazydesert.racing.UserPhoto;
import com.crazydesert.racing.dto.DriverProfileResponse;
import com.crazydesert.racing.dto.PublicProfileUpdateRequest;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.enums.UserPhotoVisibility;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.RaceRegistrationRepository;
import com.crazydesert.racing.repository.UserPhotoRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RaceCarRepository raceCarRepository;

    @Mock
    private RaceRegistrationRepository raceRegistrationRepository;

    @Mock
    private UserPhotoRepository userPhotoRepository;

    private DriverService driverService;

    @BeforeEach
    void setUp() {
        driverService = new DriverService(
                userRepository,
                raceCarRepository,
                raceRegistrationRepository,
                userPhotoRepository
        );
    }

    @Test
    void hidesCarsRacesAndPhotosWhenDriverDisablesThem() {
        User viewer = user(1L, "viewer@example.com");
        User driver = user(2L, "driver@example.com");
        driver.setShowCars(false);
        driver.setShowRaceHistory(false);
        driver.setShowPhotos(false);

        when(userRepository.findByEmail("viewer@example.com"))
                .thenReturn(Optional.of(viewer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(driver));

        DriverProfileResponse response = driverService.getDriver(
                2L,
                "viewer@example.com"
        );

        assertFalse(response.carsVisible());
        assertFalse(response.raceHistoryVisible());
        assertFalse(response.photosVisible());
        assertTrue(response.cars().isEmpty());
        assertTrue(response.races().isEmpty());
        assertTrue(response.photos().isEmpty());
        verify(raceCarRepository, never()).findByOwnerId(2L);
        verify(raceRegistrationRepository, never()).findByUserId(2L);
        verify(userPhotoRepository, never())
                .findByOwnerIdOrderByCreatedAtDesc(2L);
    }

    @Test
    void ownerCanSeePrivatePhotosAndHiddenCollections() {
        User driver = user(2L, "driver@example.com");
        driver.setShowPhotos(false);
        UserPhoto privatePhoto = photo(10L, driver);
        privatePhoto.setVisibility(UserPhotoVisibility.PRIVATE);

        when(userRepository.findByEmail("driver@example.com"))
                .thenReturn(Optional.of(driver));
        when(userRepository.findById(2L)).thenReturn(Optional.of(driver));
        when(raceCarRepository.findByOwnerId(2L)).thenReturn(List.of());
        when(raceRegistrationRepository.findByUserId(2L))
                .thenReturn(List.of());
        when(userPhotoRepository.findByOwnerIdOrderByCreatedAtDesc(2L))
                .thenReturn(List.of(privatePhoto));

        DriverProfileResponse response = driverService.getDriver(
                2L,
                "driver@example.com"
        );

        assertFalse(response.photosVisible());
        assertEquals(1, response.photos().size());
        assertEquals(UserPhotoVisibility.PRIVATE,
                response.photos().get(0).visibility());
    }

    @Test
    void cardAndAvatarCanUseDifferentVisiblePhotos() {
        User viewer = user(1L, "viewer@example.com");
        User driver = user(2L, "driver@example.com");
        driver.setProfilePhoto(photo(10L, driver));
        driver.setProfileCardPhoto(photo(11L, driver));

        when(userRepository.findByEmail("viewer@example.com"))
                .thenReturn(Optional.of(viewer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(driver));
        when(userPhotoRepository.findByOwnerIdAndVisibilityInOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(List.of());

        DriverProfileResponse response = driverService.getDriver(
                2L,
                "viewer@example.com"
        );

        assertEquals("/driver-photos/10/image?v=1", response.avatarUrl());
        assertEquals("/driver-photos/11/image?v=1", response.cardImageUrl());
    }

    @Test
    void updatesOnlyPublicProfilePreferences() {
        User driver = user(2L, "driver@example.com");
        PublicProfileUpdateRequest request = new PublicProfileUpdateRequest();
        request.bio = "  Desert racer  ";
        request.location = "  Negev  ";
        request.showCars = true;
        request.showRaceHistory = false;
        request.showPhotos = true;

        when(userRepository.findByEmail("driver@example.com"))
                .thenReturn(Optional.of(driver));
        when(userRepository.save(driver)).thenReturn(driver);
        when(raceCarRepository.findByOwnerId(2L)).thenReturn(List.of());
        when(userPhotoRepository.findByOwnerIdOrderByCreatedAtDesc(2L))
                .thenReturn(List.of());

        DriverProfileResponse response = driverService.updateCurrentProfile(
                "driver@example.com",
                request
        );

        assertEquals("Desert racer", response.bio());
        assertEquals("Negev", response.location());
        assertTrue(response.carsVisible());
        assertFalse(response.raceHistoryVisible());
        assertTrue(response.photosVisible());
    }

    private User user(Long id, String email) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setName("Driver " + id);
        user.setAge(30);
        user.setEmail(email);
        user.setRole(Role.USER);
        return user;
    }

    private UserPhoto photo(Long id, User owner) {
        UserPhoto photo = new UserPhoto();
        ReflectionTestUtils.setField(photo, "id", id);
        photo.setOwner(owner);
        photo.setImageKey("photo-" + id);
        photo.setImageVersion(1L);
        photo.setRightsConfirmed(true);
        photo.setCreatedAt(LocalDateTime.now());
        return photo;
    }
}
