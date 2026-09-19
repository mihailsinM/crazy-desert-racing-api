package com.crazydesert.racing.config;

import com.crazydesert.racing.User;
import com.crazydesert.racing.enums.MembershipTier;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;

@Component
@Order(10)
@ConditionalOnProperty(
        name = "crazy.super-admin.enabled",
        havingValue = "true"
)
public class SuperAdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String name;

    public SuperAdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${crazy.super-admin.email}") String email,
            @Value("${crazy.super-admin.password}") String password,
            @Value("${crazy.super-admin.name:Michael}") String name) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = requireText(email, "Super administrator email is required");
        this.password = requireText(password, "Super administrator password is required");
        this.name = requireText(name, "Super administrator name is required");
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(User::new);

        boolean newUser = user.getId() == null;

        user.setEmail(normalizedEmail);
        user.setName(name.trim());
        user.setRole(Role.SUPER_ADMIN);
        user.setLicenseVerified(true);
        user.setShowCars(true);
        user.setShowRaceHistory(true);
        user.setShowPhotos(true);
        user.setMembershipTier(MembershipTier.PLATINUM);
        user.setPassword(passwordEncoder.encode(password));

        if (newUser) {
            user.setAge(38);
            user.setLicenseCategory("B");
        }

        userRepository.save(user);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }

        return value;
    }
}
