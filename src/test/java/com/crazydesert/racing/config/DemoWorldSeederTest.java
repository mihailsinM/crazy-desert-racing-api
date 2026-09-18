package com.crazydesert.racing.config;

import com.crazydesert.racing.User;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.repository.DesertLiveItemRepository;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.RaceRegistrationRepository;
import com.crazydesert.racing.repository.RaceRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DemoWorldSeederTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RaceCarRepository raceCarRepository = mock(RaceCarRepository.class);
    private final RaceRepository raceRepository = mock(RaceRepository.class);
    private final RaceRegistrationRepository raceRegistrationRepository = mock(RaceRegistrationRepository.class);
    private final DesertLiveItemRepository desertLiveItemRepository = mock(DesertLiveItemRepository.class);
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Test
    void blocksResetWithoutExactConfirmation() {
        DemoWorldSeeder seeder = seeder("wrong-confirmation");

        assertThrows(IllegalStateException.class, () -> seeder.run(null));

        verifyNoInteractions(userRepository, jdbcTemplate);
    }

    @Test
    void blocksResetWhenProtectedOwnerIsMissing() {
        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.empty());

        DemoWorldSeeder seeder = seeder(DemoWorldSeeder.REQUIRED_CONFIRMATION);

        assertThrows(IllegalStateException.class, () -> seeder.run(null));
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void blocksResetWhenOwnerIsNotSuperAdmin() {
        User owner = new User();
        owner.setRole(Role.ADMIN);
        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));

        DemoWorldSeeder seeder = seeder(DemoWorldSeeder.REQUIRED_CONFIRMATION);

        assertThrows(IllegalStateException.class, () -> seeder.run(null));
        verifyNoInteractions(jdbcTemplate);
    }

    private DemoWorldSeeder seeder(String confirmation) {
        return new DemoWorldSeeder(
                userRepository,
                raceCarRepository,
                raceRepository,
                raceRegistrationRepository,
                desertLiveItemRepository,
                jdbcTemplate,
                passwordEncoder,
                "owner@example.com",
                "demo-password",
                confirmation
        );
    }
}
