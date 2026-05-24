package com.crazydesert.racing.controller;

import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.UserCreateRequest;
import com.crazydesert.racing.dto.UserUpdateRequest;
import com.crazydesert.racing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;

    }
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/users")
    public User createUser(@Valid @RequestBody UserCreateRequest request){
        return userService.createUser(request);
    }

    @DeleteMapping("/users/{id}")
    public String deleteUserById(@PathVariable Long id){
        userService.deleteUserById(id);
        return "User deleted with id: " + id;
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Long id){
        return userService.getUserById(id);
    }

    @PutMapping("/users/{id}")
    public User updateUser(
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
    public User verifyLicense(@PathVariable Long id) {
        return userService.verifyLicense(id);
    }

    @PutMapping("/users/{id}/make-admin")
    public User makeAdmin(@PathVariable Long id) {
        return userService.makeAdmin(id);
    }

}

