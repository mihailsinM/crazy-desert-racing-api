package com.crazydesert.racing.service;

import com.crazydesert.racing.Race;
import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.RaceRegistration;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.RaceRegistrationCreateRequest;
import com.crazydesert.racing.exception.*;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.RaceRegistrationRepository;
import com.crazydesert.racing.repository.RaceRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class RaceRegistrationService {

    private final RaceRegistrationRepository raceRegistrationRepository;
    private final UserRepository userRepository;
    private final RaceCarRepository raceCarRepository;
    private final RaceRepository raceRepository;

    public RaceRegistrationService(
            RaceRegistrationRepository raceRegistrationRepository,
            UserRepository userRepository,
            RaceCarRepository raceCarRepository,
            RaceRepository raceRepository) {

        this.raceRegistrationRepository = raceRegistrationRepository;
        this.userRepository = userRepository;
        this.raceCarRepository = raceCarRepository;
        this.raceRepository = raceRepository;
    }

    public RaceRegistration createRegistration(
            RaceRegistrationCreateRequest request) {

        User user = userRepository.findById(request.userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + request.userId + " not found"
                        ));

        RaceCar raceCar = raceCarRepository.findById(request.raceCarId)
                .orElseThrow(() ->
                        new RaceCarNotFoundException(
                                "Race car with id " + request.raceCarId + " not found"
                        ));

        Race race = raceRepository.findById(request.raceId)
                .orElseThrow(() ->
                        new RaceNotFoundException(
                                "Race with id " + request.raceId + " not found"
                        ));

        if (!raceCar.owner.id.equals(user.id)) {
            throw new RaceCarOwnershipException(
                    "Race car does not belong to user"
            );
        }

        if (!user.licenseVerified) {
            throw new LicenseNotVerifiedException(
                    "User license is not verified"
            );
        }

        boolean registrationExists = raceRegistrationRepository
                .existsByUserIdAndRaceCarIdAndRaceId(
                        request.userId,
                        request.raceCarId,
                        request.raceId
                );

        long registrationsCount =
                raceRegistrationRepository.countByRaceId(race.id);

        if (registrationExists) {
            throw new DuplicateRaceRegistrationException(
                    "User is already registered to this race with this race car"
            );
        }

        if (registrationsCount >= race.maxParticipants) {
            throw new RaceCapacityExceededException(
                    "Race has reached maximum number of participants"
            );
        }

        RaceRegistration registration = new RaceRegistration();

        registration.user = user;
        registration.raceCar = raceCar;
        registration.race = race;

        return raceRegistrationRepository.save(registration);
    }
}