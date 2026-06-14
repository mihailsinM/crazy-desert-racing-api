package com.crazydesert.racing.service;

import com.crazydesert.racing.Race;
import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.RaceRegistration;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.RaceRegistrationCreateRequest;
import com.crazydesert.racing.dto.RaceRegistrationMyCreateRequest;
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

        if (!raceCar.getOwner().getId().equals(user.getId())) {
            throw new RaceCarOwnershipException(
                    "Race car does not belong to user"
            );
        }

        if (!user.isLicenseVerified()) {
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
                raceRegistrationRepository.countByRaceId(race.getId());

        if (registrationExists) {
            throw new DuplicateRaceRegistrationException(
                    "User is already registered to this race with this race car"
            );
        }

        if (registrationsCount >= race.getMaxParticipants()) {
            throw new RaceCapacityExceededException(
                    "Race has reached maximum number of participants"
            );
        }

        RaceRegistration registration = new RaceRegistration();

        registration.setUser(user);
        registration.setRaceCar(raceCar);
        registration.setRace(race);

        return raceRegistrationRepository.save(registration);
    }

    public RaceRegistration createMyRegistration(
            String email,
            RaceRegistrationMyCreateRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with email " + email + " not found"
                        ));

        RaceRegistrationCreateRequest fullRequest =
                new RaceRegistrationCreateRequest();

        fullRequest.userId = user.getId();
        fullRequest.raceCarId = request.raceCarId;
        fullRequest.raceId = request.raceId;

        return createRegistration(fullRequest);
    }
}