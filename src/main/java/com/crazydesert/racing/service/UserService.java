package com.crazydesert.racing.service;

import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.UserCreateRequest;
import com.crazydesert.racing.dto.UserUpdateRequest;
import com.crazydesert.racing.exception.UserNotFoundException;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RaceCarRepository raceCarRepository;

    public UserService(UserRepository userRepository, RaceCarRepository raceCarRepository) {
        this.userRepository = userRepository;
        this.raceCarRepository = raceCarRepository;
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User createUser(UserCreateRequest request){

        User user = new User();

        user.name = request.name;
        user.age = request.age;
        user.email = request.email;

        user.licenseCategory = request.licenseCategory;
        user.licenseVerified = false;

        return userRepository.save(user);
    }

    public void deleteUserById(Long id) {

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(
                    "User with id " + id + " not found"
            );
        }
        userRepository.deleteById(id);
    }

public User getUserById(Long id) {

    return userRepository.findById(id)
            .orElseThrow(() ->
                    new UserNotFoundException(
                            "User with id " + id + " not found"
                    ));
}

    public User updateUser(Long id, UserUpdateRequest request) {
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

        return userRepository.save(existingUser);
    }

    public List<RaceCar> getUserCars(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + userId + " not found"
                        ));

        return raceCarRepository.findByOwnerId(userId);
    }

    public User verifyLicense(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"
                        ));

        user.licenseVerified = true;

        return userRepository.save(user);
    }


}
