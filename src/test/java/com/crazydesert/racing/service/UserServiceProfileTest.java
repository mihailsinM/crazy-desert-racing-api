package com.crazydesert.racing.service;

import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.UserAvatarResponse;
import com.crazydesert.racing.dto.UserProfileUpdateRequest;
import com.crazydesert.racing.dto.UserResponse;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.exception.EmailAlreadyInUseException;
import com.crazydesert.racing.exception.InvalidAvatarException;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceProfileTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RaceCarRepository raceCarRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                raceCarRepository,
                passwordEncoder
        );
    }

    @Test
    void updatesOnlyCurrentUsersEditableProfileFields() {
        User user = createUser(1L, "racer@example.com", "B", true);
        UserProfileUpdateRequest request = profileRequest(
                "Updated Racer",
                39,
                "racer@example.com",
                "B"
        );

        when(userRepository.findByEmail("racer@example.com"))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateCurrentUser(
                "racer@example.com",
                request
        );

        assertEquals("Updated Racer", response.name);
        assertEquals(39, response.age);
        assertEquals(Role.USER, response.role);
        assertTrue(response.licenseVerified);
        verify(userRepository).save(user);
    }

    @Test
    void resetsLicenseVerificationWhenCategoryChanges() {
        User user = createUser(1L, "racer@example.com", "B", true);
        UserProfileUpdateRequest request = profileRequest(
                "Racer",
                39,
                "racer@example.com",
                "C"
        );

        when(userRepository.findByEmail("racer@example.com"))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateCurrentUser(
                "racer@example.com",
                request
        );

        assertEquals("C", response.licenseCategory);
        assertFalse(response.licenseVerified);
    }

    @Test
    void rejectsEmailThatBelongsToAnotherUser() {
        User currentUser = createUser(1L, "current@example.com", "B", true);
        User otherUser = createUser(2L, "used@example.com", "B", true);
        UserProfileUpdateRequest request = profileRequest(
                "Racer",
                39,
                "used@example.com",
                "B"
        );

        when(userRepository.findByEmail("current@example.com"))
                .thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail("used@example.com"))
                .thenReturn(Optional.of(otherUser));

        assertThrows(
                EmailAlreadyInUseException.class,
                () -> userService.updateCurrentUser(
                        "current@example.com",
                        request
                )
        );

        verify(userRepository, never()).save(currentUser);
    }

    @Test
    void storesValidAvatarAndReturnsVersionedUrl() {
        User user = createUser(1L, "racer@example.com", "B", true);
        byte[] imageData = new byte[]{
                (byte) 0x89,
                0x50,
                0x4e,
                0x47,
                0x0d,
                0x0a,
                0x1a,
                0x0a
        };
        MockMultipartFile avatar = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                imageData
        );

        when(userRepository.findByEmail("racer@example.com"))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateCurrentUserAvatar(
                "racer@example.com",
                avatar
        );

        assertArrayEquals(imageData, user.getAvatarData());
        assertEquals("image/png", user.getAvatarContentType());
        assertTrue(response.avatarUrl.startsWith("/avatars/1?v="));
    }

    @Test
    void rejectsUnsupportedAvatarType() {
        MockMultipartFile avatar = new MockMultipartFile(
                "file",
                "avatar.svg",
                "image/svg+xml",
                new byte[]{1, 2, 3}
        );

        assertThrows(
                InvalidAvatarException.class,
                () -> userService.updateCurrentUserAvatar(
                        "racer@example.com",
                        avatar
                )
        );

        verify(userRepository, never()).save(
                org.mockito.ArgumentMatchers.any(User.class)
        );
    }

    @Test
    void rejectsFileWhoseContentDoesNotMatchImageType() {
        MockMultipartFile avatar = new MockMultipartFile(
                "file",
                "fake.png",
                "image/png",
                "not-an-image".getBytes()
        );

        assertThrows(
                InvalidAvatarException.class,
                () -> userService.updateCurrentUserAvatar(
                        "racer@example.com",
                        avatar
                )
        );

        verify(userRepository, never()).save(
                org.mockito.ArgumentMatchers.any(User.class)
        );
    }

    @Test
    void returnsStoredAvatar() {
        User user = createUser(1L, "racer@example.com", "B", true);
        byte[] imageData = new byte[]{4, 5, 6};
        user.setAvatarData(imageData);
        user.setAvatarContentType("image/webp");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserAvatarResponse avatar = userService.getUserAvatar(1L);

        assertArrayEquals(imageData, avatar.data());
        assertEquals("image/webp", avatar.contentType());
    }

    private User createUser(
            Long id,
            String email,
            String licenseCategory,
            boolean licenseVerified) {

        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setName("Racer");
        user.setAge(38);
        user.setEmail(email);
        user.setLicenseCategory(licenseCategory);
        user.setLicenseVerified(licenseVerified);
        user.setRole(Role.USER);

        return user;
    }

    private UserProfileUpdateRequest profileRequest(
            String name,
            int age,
            String email,
            String licenseCategory) {

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.name = name;
        request.age = age;
        request.email = email;
        request.licenseCategory = licenseCategory;

        return request;
    }
}
