package com.crazydesert.racing.service;

import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.UserCreateRequest;
import com.crazydesert.racing.exception.UserNotFoundException;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers(){
        return userRepository.findAll();
    }

    public User createUser(UserCreateRequest request){

        User user = new User();
        user.name = request.name;
        user.age = request.age;

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

    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " not found"
                        ));

        existingUser.name = updatedUser.name;
        existingUser.age = updatedUser.age;

        return userRepository.save(existingUser);
    }


}
