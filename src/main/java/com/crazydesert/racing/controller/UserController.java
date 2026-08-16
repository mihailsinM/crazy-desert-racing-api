package com.crazydesert.racing.controller;

import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.dto.UserAvatarResponse;
import com.crazydesert.racing.dto.UserCreateRequest;
import com.crazydesert.racing.dto.UserProfileUpdateRequest;
import com.crazydesert.racing.dto.UserResponse;
import com.crazydesert.racing.dto.UserUpdateRequest;
import com.crazydesert.racing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;


@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;

    }
    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/users")
    public UserResponse createUser(
            @Valid @RequestBody UserCreateRequest request) {

        return userService.createUser(request);
    }

    @DeleteMapping("/users/{id}")
    public String deleteUserById(@PathVariable Long id){
        userService.deleteUserById(id);
        return "User deleted with id: " + id;
    }

    @GetMapping("/users/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/users/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {

        return userService.updateUser(id, request);
    }

    @GetMapping("/{userId}/cars")
    public List<RaceCar> getUserCars(
            @PathVariable Long userId) {

        return userService.getUserCars(userId);
    }

    @PutMapping("/users/{id}/verify-license")
    public UserResponse verifyLicense(@PathVariable Long id) {
        return userService.verifyLicense(id);
    }

    @PutMapping("/users/{id}/make-admin")
    public UserResponse makeAdmin(@PathVariable Long id) {
        return userService.makeAdmin(id);
    }

    @GetMapping("/users/me")
    public UserResponse getCurrentUser(Authentication authentication) {

        String email = authentication.getName();

        return userService.getCurrentUser(email);
    }

    @PutMapping("/users/me")
    public UserResponse updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request) {

        return userService.updateCurrentUser(
                authentication.getName(),
                request
        );
    }

    @PutMapping(
            value = "/users/me/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public UserResponse updateCurrentUserAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile avatar) {

        return userService.updateCurrentUserAvatar(
                authentication.getName(),
                avatar
        );
    }

    @DeleteMapping("/users/me/avatar")
    public UserResponse deleteCurrentUserAvatar(
            Authentication authentication) {

        return userService.deleteCurrentUserAvatar(
                authentication.getName()
        );
    }

    @GetMapping("/avatars/{userId}")
    public ResponseEntity<byte[]> getUserAvatar(@PathVariable Long userId) {
        UserAvatarResponse avatar = userService.getUserAvatar(userId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.contentType()))
                .cacheControl(
                        CacheControl.maxAge(Duration.ofDays(30)).cachePublic()
                )
                .body(avatar.data());
    }

}
