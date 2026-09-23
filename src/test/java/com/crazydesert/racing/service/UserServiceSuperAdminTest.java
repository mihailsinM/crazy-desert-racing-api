package com.crazydesert.racing.service;

import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.UserUpdateRequest;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.exception.ProtectedAccountException;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceSuperAdminTest {

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
                passwordEncoder,
                new ImageMetadataSanitizer(),
                new ImageFramingValidator(new ImageFocusValidator())
        );
    }

    @Test
    void preventsDeletingSuperAdmin() {
        User owner = superAdmin();
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThrows(
                ProtectedAccountException.class,
                () -> userService.deleteUserById(1L)
        );

        verify(userRepository, never()).delete(owner);
    }

    @Test
    void preventsAdminEditingSuperAdmin() {
        User owner = superAdmin();
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThrows(
                ProtectedAccountException.class,
                () -> userService.updateUser(1L, new UserUpdateRequest())
        );

        verify(userRepository, never()).save(owner);
    }

    @Test
    void promotesRegularUser() {
        User user = regularUser();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        assertEquals(Role.ADMIN, userService.makeAdmin(2L).role);
        verify(userRepository).save(user);
    }

    @Test
    void revokesAdministrator() {
        User user = regularUser();
        user.setRole(Role.ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        assertEquals(Role.USER, userService.removeAdmin(2L).role);
        verify(userRepository).save(user);
    }

    @Test
    void preventsChangingProtectedSuperAdministratorRole() {
        User owner = superAdmin();
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThrows(ProtectedAccountException.class, () -> userService.makeAdmin(1L));
        assertThrows(ProtectedAccountException.class, () -> userService.removeAdmin(1L));
        verify(userRepository, never()).save(owner);
    }

    private User regularUser() {
        User user = new User();
        user.setEmail("member@example.com");
        user.setRole(Role.USER);
        return user;
    }

    private User superAdmin() {
        User user = new User();
        user.setEmail("owner@example.com");
        user.setRole(Role.SUPER_ADMIN);
        return user;
    }
}
