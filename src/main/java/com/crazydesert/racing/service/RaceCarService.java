package com.crazydesert.racing.service;


import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.RaceCarCreateRequest;
import com.crazydesert.racing.dto.RaceCarUpdateRequest;
import com.crazydesert.racing.exception.RaceCarNotFoundException;
import com.crazydesert.racing.exception.UserNotFoundException;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RaceCarService {

    private final RaceCarRepository raceCarRepository;
    private final UserRepository userRepository;

    public RaceCarService(RaceCarRepository raceCarRepository, UserRepository userRepository) {
        this.raceCarRepository = raceCarRepository;
        this.userRepository = userRepository;
    }

    public RaceCar assignCarToUser(Long userId, Long raceCarId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + userId + " not found"
                        ));

        RaceCar raceCar = raceCarRepository.findById(raceCarId)
                .orElseThrow(() ->
                        new RaceCarNotFoundException(
                                "Race car with id " + raceCarId + " not found"
                        ));

        raceCar.owner = user;

        return raceCarRepository.save(raceCar);
    }

    public List<RaceCar> getAllRaceCars() {
        return raceCarRepository.findAll();
    }

    public List<RaceCar> getRaceCarsByOwnerEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with email " + email + " not found"
                        ));

        return raceCarRepository.findByOwnerId(user.id);
    }

    public RaceCar createRaceCar(RaceCarCreateRequest request) {

        RaceCar raceCar = new RaceCar();

        raceCar.name = request.name;
        raceCar.brand = request.brand;
        raceCar.horsePower = request.horsePower;
        raceCar.imageUrl = request.imageUrl;

        return raceCarRepository.save(raceCar);
    }

    public RaceCar getRaceCarById(Long id) {
        return raceCarRepository.findById(id)
                .orElseThrow(()->
                        new RaceCarNotFoundException(
                                "Race car with id " + id + " not found"));
    }

    public RaceCar updateRaceCar(Long id, RaceCarUpdateRequest request) {
        RaceCar existingRaceCar = raceCarRepository.findById(id)
                .orElseThrow(()->
                        new RaceCarNotFoundException(
                                "Race car with id " + id + " not found"
                        ));
        existingRaceCar.name = request.name;
        existingRaceCar.brand = request.brand;
        existingRaceCar.horsePower = request.horsePower;
        existingRaceCar.imageUrl = request.imageUrl;

        return raceCarRepository.save(existingRaceCar);
    }

    public RaceCar createMyRaceCar(String email, RaceCarCreateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with email " + email + " not found"
                        ));

        RaceCar raceCar = new RaceCar();

        raceCar.name = request.name;
        raceCar.brand = request.brand;
        raceCar.horsePower = request.horsePower;
        raceCar.imageUrl = request.imageUrl;
        raceCar.owner = user;

        return raceCarRepository.save(raceCar);
    }

    public void deleteRaceCarById(Long id) {
        if (!raceCarRepository.existsById(id)) {
            throw new RaceCarNotFoundException(
                    "Race car with id " + id + " not found"
            );
        }

        raceCarRepository.deleteById(id);
    }
}
