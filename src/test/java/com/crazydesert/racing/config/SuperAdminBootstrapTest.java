package com.crazydesert.racing.config;

import com.crazydesert.racing.User;
import com.crazydesert.racing.enums.MembershipTier;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuperAdminBootstrapTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createsProtectedOwnerWhenAccountDoesNotExist() throws Exception {
        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("unique-password"))
                .thenReturn("encoded-password");

        SuperAdminBootstrap bootstrap = new SuperAdminBootstrap(
                userRepository,
                passwordEncoder,
                " Owner@Example.com ",
                "unique-password",
                "Michael"
        );

        bootstrap.run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("owner@example.com", saved.getEmail());
        assertEquals("Michael", saved.getName());
        assertEquals("encoded-password", saved.getPassword());
        assertEquals(Role.SUPER_ADMIN, saved.getRole());
        assertEquals(MembershipTier.PLATINUM, saved.getMembershipTier());
        assertTrue(saved.isLicenseVerified());
    }

    @Test
    void restoresExistingAccountAsSuperAdmin() throws Exception {
        User existing = new User();
        ReflectionTestUtils.setField(existing, "id", 7L);
        existing.setEmail("owner@example.com");
        existing.setRole(Role.USER);

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("new-password"))
                .thenReturn("new-hash");

        SuperAdminBootstrap bootstrap = new SuperAdminBootstrap(
                userRepository,
                passwordEncoder,
                "owner@example.com",
                "new-password",
                "Michael"
        );

        bootstrap.run(null);

        assertEquals(Role.SUPER_ADMIN, existing.getRole());
        assertEquals("new-hash", existing.getPassword());
        verify(userRepository).save(existing);
    }

    @Test
    void rejectsBlankPassword() {
        assertThrows(
                IllegalStateException.class,
                () -> new SuperAdminBootstrap(
                        userRepository,
                        passwordEncoder,
                        "owner@example.com",
                        " ",
                        "Michael"
                )
        );
    }
}
