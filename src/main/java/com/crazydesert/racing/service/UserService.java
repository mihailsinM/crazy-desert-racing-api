package com.crazydesert.racing.service;

import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.UserCreateRequest;
import com.crazydesert.racing.dto.UserResponse;
import com.crazydesert.racing.dto.UserUpdateRequest;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.exception.UserNotFoundException;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

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

        response.id = user.id;
        response.name = user.name;
        response.age = user.age;
        response.email = user.email;
        response.licenseCategory = user.licenseCategory;
        response.licenseVerified = user.licenseVerified;
        response.role = user.role;

        return response;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse createUser(UserCreateRequest request) {

        User user = new User();

        user.name = request.name;
        user.age = request.age;
        user.email = request.email;
        user.licenseCategory = request.licenseCategory;
        user.licenseVerified = false;

        user.password = passwordEncoder.encode(request.password);

        user.role = Role.USER;

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

        existingUser.name = request.name;
        existingUser.age = request.age;
        existingUser.email = request.email;
        existingUser.licenseCategory = request.licenseCategory;
        existingUser.licenseVerified = false;

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

        user.licenseVerified = true;
        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    public UserResponse makeAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"
                        ));

        user.role = Role.ADMIN;
        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with email " + email + " not found"
                        ));

        return toResponse(user);
    }


}
