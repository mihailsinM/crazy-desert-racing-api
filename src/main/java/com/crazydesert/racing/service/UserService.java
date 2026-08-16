package com.crazydesert.racing.service;

import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.UserAvatarResponse;
import com.crazydesert.racing.dto.UserCreateRequest;
import com.crazydesert.racing.dto.UserProfileUpdateRequest;
import com.crazydesert.racing.dto.UserResponse;
import com.crazydesert.racing.dto.UserUpdateRequest;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.exception.AvatarNotFoundException;
import com.crazydesert.racing.exception.AvatarStorageException;
import com.crazydesert.racing.exception.EmailAlreadyInUseException;
import com.crazydesert.racing.exception.InvalidAvatarException;
import com.crazydesert.racing.exception.UserNotFoundException;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class UserService {

    private static final long MAX_AVATAR_SIZE_BYTES = 2L * 1024 * 1024;
    private static final Set<String> ALLOWED_AVATAR_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final UserRepository userRepository;
    private final RaceCarRepository raceCarRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RaceCarRepository raceCarRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.raceCarRepository = raceCarRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserResponse toResponse(User user) {

        UserResponse response = new UserResponse();

        response.id = user.getId();
        response.name = user.getName();
        response.age = user.getAge();
        response.email = user.getEmail();
        response.licenseCategory = user.getLicenseCategory();
        response.licenseVerified = user.isLicenseVerified();
        response.role = user.getRole();
        response.avatarUrl = buildAvatarUrl(user);

        return response;
    }

    private String buildAvatarUrl(User user) {
        if (user.getAvatarContentType() == null) {
            return null;
        }

        return "/avatars/" + user.getId() + "?v=" + user.getAvatarVersion();
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse createUser(UserCreateRequest request) {

        User user = new User();
        String email = request.email.trim();

        validateEmailAvailable(email, null);

        user.setName(request.name.trim());
        user.setAge(request.age);
        user.setEmail(email);
        user.setLicenseCategory(trimToNull(request.licenseCategory));
        user.setLicenseVerified(false);

        user.setPassword(passwordEncoder.encode(request.password));

        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    public void deleteUserById(Long id) {

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(
                    "User with id " + id + " not found"
            );
        }
        userRepository.deleteById(id);
    }

    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"
                        ));

        return toResponse(user);
    }

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"
                        ));

        String email = request.email.trim();

        validateEmailAvailable(email, existingUser.getId());

        existingUser.setName(request.name.trim());
        existingUser.setAge(request.age);
        existingUser.setEmail(email);
        existingUser.setLicenseCategory(trimToNull(request.licenseCategory));
        existingUser.setLicenseVerified(false);

        User savedUser = userRepository.save(existingUser);

        return toResponse(savedUser);
    }

    public List<RaceCar> getUserCars(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + userId + " not found"
                        ));

        return raceCarRepository.findByOwnerId(userId);
    }

    public UserResponse verifyLicense(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"
                        ));

        user.setLicenseVerified(true);
        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    public UserResponse makeAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"
                        ));

        user.setRole(Role.ADMIN);
        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    public UserResponse getCurrentUser(String email) {
        User user = getUserByEmail(email);

        return toResponse(user);
    }

    public UserResponse updateCurrentUser(
            String currentEmail,
            UserProfileUpdateRequest request) {

        User user = getUserByEmail(currentEmail);
        String email = request.email.trim();
        String licenseCategory = request.licenseCategory.trim();

        validateEmailAvailable(email, user.getId());

        boolean licenseCategoryChanged = !Objects.equals(
                user.getLicenseCategory(),
                licenseCategory
        );

        user.setName(request.name.trim());
        user.setAge(request.age);
        user.setEmail(email);
        user.setLicenseCategory(licenseCategory);

        if (licenseCategoryChanged) {
            user.setLicenseVerified(false);
        }

        return toResponse(userRepository.save(user));
    }

    public UserResponse updateCurrentUserAvatar(
            String currentEmail,
            MultipartFile avatar) {

        byte[] avatarData = validateAndReadAvatar(avatar);

        User user = getUserByEmail(currentEmail);

        user.setAvatarData(avatarData);
        user.setAvatarContentType(avatar.getContentType());
        user.setAvatarVersion(System.currentTimeMillis());

        return toResponse(userRepository.save(user));
    }

    public UserResponse deleteCurrentUserAvatar(String currentEmail) {
        User user = getUserByEmail(currentEmail);

        user.setAvatarData(null);
        user.setAvatarContentType(null);
        user.setAvatarVersion(System.currentTimeMillis());

        return toResponse(userRepository.save(user));
    }

    public UserAvatarResponse getUserAvatar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + userId + " not found"
                        ));

        byte[] avatarData = user.getAvatarData();

        if (avatarData == null || avatarData.length == 0) {
            throw new AvatarNotFoundException(
                    "Avatar for user with id " + userId + " not found"
            );
        }

        return new UserAvatarResponse(
                avatarData,
                user.getAvatarContentType()
        );
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with email " + email + " not found"
                        ));
    }

    private void validateEmailAvailable(String email, Long currentUserId) {
        userRepository.findByEmail(email)
                .filter(user -> currentUserId == null
                        || !Objects.equals(user.getId(), currentUserId))
                .ifPresent(user -> {
                    throw new EmailAlreadyInUseException(
                            "Email is already in use"
                    );
                });
    }

    private byte[] validateAndReadAvatar(MultipartFile avatar) {
        if (avatar == null || avatar.isEmpty()) {
            throw new InvalidAvatarException("Avatar image must not be empty");
        }

        if (avatar.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new InvalidAvatarException(
                    "Avatar image must be 2 MB or smaller"
            );
        }

        if (!ALLOWED_AVATAR_TYPES.contains(avatar.getContentType())) {
            throw new InvalidAvatarException(
                    "Avatar must be a JPG, PNG, or WebP image"
            );
        }

        byte[] avatarData;

        try {
            avatarData = avatar.getBytes();
        } catch (IOException exception) {
            throw new AvatarStorageException(
                    "Failed to read avatar image",
                    exception
            );
        }

        if (!hasExpectedImageSignature(avatarData, avatar.getContentType())) {
            throw new InvalidAvatarException(
                    "Avatar file content does not match its image type"
            );
        }

        return avatarData;
    }

    private boolean hasExpectedImageSignature(
            byte[] data,
            String contentType) {

        return switch (contentType) {
            case "image/jpeg" -> isJpeg(data);
            case "image/png" -> isPng(data);
            case "image/webp" -> isWebp(data);
            default -> false;
        };
    }

    private boolean isJpeg(byte[] data) {
        return data.length >= 3
                && (data[0] & 0xff) == 0xff
                && (data[1] & 0xff) == 0xd8
                && (data[2] & 0xff) == 0xff;
    }

    private boolean isPng(byte[] data) {
        return data.length >= 8
                && (data[0] & 0xff) == 0x89
                && data[1] == 0x50
                && data[2] == 0x4e
                && data[3] == 0x47
                && data[4] == 0x0d
                && data[5] == 0x0a
                && data[6] == 0x1a
                && data[7] == 0x0a;
    }

    private boolean isWebp(byte[] data) {
        return data.length >= 12
                && data[0] == 'R'
                && data[1] == 'I'
                && data[2] == 'F'
                && data[3] == 'F'
                && data[8] == 'W'
                && data[9] == 'E'
                && data[10] == 'B'
                && data[11] == 'P';
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }


}
