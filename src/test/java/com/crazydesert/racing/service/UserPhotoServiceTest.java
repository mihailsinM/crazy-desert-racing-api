package com.crazydesert.racing.service;

import com.crazydesert.racing.User;
import com.crazydesert.racing.UserPhoto;
import com.crazydesert.racing.dto.ImageFramingRequest;
import com.crazydesert.racing.dto.MediaImageResponse;
import com.crazydesert.racing.dto.UserPhotoReportRequest;
import com.crazydesert.racing.dto.UserPhotoUpdateRequest;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.enums.UserPhotoReportReason;
import com.crazydesert.racing.enums.UserPhotoVisibility;
import com.crazydesert.racing.exception.DuplicateUserPhotoReportException;
import com.crazydesert.racing.exception.InvalidUserPhotoException;
import com.crazydesert.racing.exception.UserPhotoAccessDeniedException;
import com.crazydesert.racing.repository.UserPhotoReportRepository;
import com.crazydesert.racing.repository.UserPhotoRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPhotoServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPhotoRepository userPhotoRepository;

    @Mock
    private UserPhotoReportRepository userPhotoReportRepository;

    @Mock
    private MediaImageService mediaImageService;

    private UserPhotoService userPhotoService;

    @BeforeEach
    void setUp() {
        userPhotoService = new UserPhotoService(
                userRepository,
                userPhotoRepository,
                userPhotoReportRepository,
                mediaImageService,
                new ImageFramingValidator(new ImageFocusValidator())
        );
    }

    @Test
    void rejectsUploadWithoutRightsConfirmationBeforeStoringImage() {
        User owner = user(1L, "owner@example.com");
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "photo.png",
                "image/png",
                new byte[]{1}
        );
        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));

        assertThrows(
                InvalidUserPhotoException.class,
                () -> userPhotoService.uploadPhoto(
                        "owner@example.com",
                        image,
                        false,
                        null,
                        UserPhotoVisibility.MEMBERS_ONLY,
                        new ImageFramingRequest()
                )
        );

        verify(mediaImageService, never()).storeImage(
                any(),
                any(),
                any()
        );
    }

    @Test
    void preventsAnotherMemberFromViewingPrivatePhoto() {
        User owner = user(1L, "owner@example.com");
        User viewer = user(2L, "viewer@example.com");
        UserPhoto photo = photo(10L, owner, UserPhotoVisibility.PRIVATE);

        when(userRepository.findByEmail("viewer@example.com"))
                .thenReturn(Optional.of(viewer));
        when(userPhotoRepository.findById(10L))
                .thenReturn(Optional.of(photo));

        assertThrows(
                UserPhotoAccessDeniedException.class,
                () -> userPhotoService.getPhotoImage(
                        10L,
                        "viewer@example.com"
                )
        );

        verify(mediaImageService, never()).getAuthorizedImage(any());
    }

    @Test
    void allowsOwnerToViewPrivatePhoto() {
        User owner = user(1L, "owner@example.com");
        UserPhoto photo = photo(10L, owner, UserPhotoVisibility.PRIVATE);
        byte[] imageData = new byte[]{1, 2, 3};

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));
        when(userPhotoRepository.findById(10L))
                .thenReturn(Optional.of(photo));
        when(mediaImageService.getAuthorizedImage("image-key"))
                .thenReturn(new MediaImageResponse(imageData, "image/png"));

        MediaImageResponse response = userPhotoService.getPhotoImage(
                10L,
                "owner@example.com"
        );

        assertArrayEquals(imageData, response.data());
    }

    @Test
    void privatePhotoCannotBecomeProfilePhoto() {
        User owner = user(1L, "owner@example.com");
        UserPhoto photo = photo(10L, owner, UserPhotoVisibility.PRIVATE);

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));
        when(userPhotoRepository.findById(10L))
                .thenReturn(Optional.of(photo));

        assertThrows(
                InvalidUserPhotoException.class,
                () -> userPhotoService.setProfilePhoto(
                        "owner@example.com",
                        10L
                )
        );
        verify(userRepository, never()).save(owner);
    }

    @Test
    void selectingAvatarKeepsPreviousPhotoOnCard() {
        User owner = user(1L, "owner@example.com");
        UserPhoto oldAvatar = photo(10L, owner, UserPhotoVisibility.PUBLIC);
        UserPhoto newAvatar = photo(11L, owner, UserPhotoVisibility.PUBLIC);
        owner.setProfilePhoto(oldAvatar);

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));
        when(userPhotoRepository.findById(11L))
                .thenReturn(Optional.of(newAvatar));

        userPhotoService.setProfilePhoto("owner@example.com", 11L);

        assertEquals(newAvatar, owner.getProfilePhoto());
        assertEquals(oldAvatar, owner.getProfileCardPhoto());
        verify(userRepository).save(owner);
    }

    @Test
    void selectingCardDoesNotChangeAvatarAndRejectsPrivatePhoto() {
        User owner = user(1L, "owner@example.com");
        UserPhoto avatar = photo(10L, owner, UserPhotoVisibility.MEMBERS_ONLY);
        UserPhoto card = photo(11L, owner, UserPhotoVisibility.PUBLIC);
        UserPhoto privatePhoto = photo(12L, owner, UserPhotoVisibility.PRIVATE);
        owner.setProfilePhoto(avatar);

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));
        when(userPhotoRepository.findById(11L))
                .thenReturn(Optional.of(card));
        when(userPhotoRepository.findById(12L))
                .thenReturn(Optional.of(privatePhoto));

        userPhotoService.setProfileCardPhoto("owner@example.com", 11L);

        assertEquals(avatar, owner.getProfilePhoto());
        assertEquals(card, owner.getProfileCardPhoto());
        assertThrows(
                InvalidUserPhotoException.class,
                () -> userPhotoService.setProfileCardPhoto(
                        "owner@example.com",
                        12L
                )
        );
        assertEquals(card, owner.getProfileCardPhoto());
    }

    @Test
    void hidingSelectedCardClearsCardWithoutClearingAvatar() {
        User owner = user(1L, "owner@example.com");
        UserPhoto avatar = photo(10L, owner, UserPhotoVisibility.PUBLIC);
        UserPhoto card = photo(11L, owner, UserPhotoVisibility.PUBLIC);
        owner.setProfilePhoto(avatar);
        owner.setProfileCardPhoto(card);

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));
        when(userPhotoRepository.findById(11L))
                .thenReturn(Optional.of(card));
        when(userPhotoRepository.save(card)).thenReturn(card);

        UserPhotoUpdateRequest request = new UserPhotoUpdateRequest();
        request.visibility = UserPhotoVisibility.PRIVATE;

        userPhotoService.updatePhoto(
                "owner@example.com",
                11L,
                request
        );

        assertEquals(avatar, owner.getProfilePhoto());
        assertNull(owner.getProfileCardPhoto());
    }

    @Test
    void rejectsDuplicateReport() {
        User owner = user(1L, "owner@example.com");
        User reporter = user(2L, "reporter@example.com");
        UserPhoto photo = photo(
                10L,
                owner,
                UserPhotoVisibility.MEMBERS_ONLY
        );
        UserPhotoReportRequest request = new UserPhotoReportRequest();
        request.reason = UserPhotoReportReason.PRIVACY;

        when(userRepository.findByEmail("reporter@example.com"))
                .thenReturn(Optional.of(reporter));
        when(userPhotoRepository.findById(10L))
                .thenReturn(Optional.of(photo));
        when(userPhotoReportRepository.existsByPhotoIdAndReporterId(10L, 2L))
                .thenReturn(true);

        assertThrows(
                DuplicateUserPhotoReportException.class,
                () -> userPhotoService.reportPhoto(
                        10L,
                        "reporter@example.com",
                        request
                )
        );
        verify(userPhotoReportRepository, never()).save(any());
    }

    private User user(Long id, String email) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setName("User " + id);
        user.setAge(30);
        user.setEmail(email);
        user.setRole(Role.USER);
        return user;
    }

    private UserPhoto photo(
            Long id,
            User owner,
            UserPhotoVisibility visibility) {

        UserPhoto photo = new UserPhoto();
        ReflectionTestUtils.setField(photo, "id", id);
        photo.setOwner(owner);
        photo.setImageKey("image-key");
        photo.setImageVersion(1L);
        photo.setVisibility(visibility);
        photo.setRightsConfirmed(true);
        photo.setCreatedAt(LocalDateTime.now());
        return photo;
    }
}
