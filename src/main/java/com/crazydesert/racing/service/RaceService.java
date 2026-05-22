package com.crazydesert.racing.service;


import com.crazydesert.racing.Race;
import com.crazydesert.racing.dto.RaceCreateRequest;
import com.crazydesert.racing.dto.RaceUpdateRequest;
import com.crazydesert.racing.exception.RaceNotFoundException;
import com.crazydesert.racing.repository.RaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RaceService {

    private final RaceRepository raceRepository;

    public RaceService(RaceRepository raceRepository){
        this.raceRepository = raceRepository;
    }

    public Race createRace(RaceCreateRequest request) {
        Race race = new Race();

        race.name = request.name;
        race.location = request.location;
        race.startDate = request.startDate;
        race.maxParticipants = request.maxParticipants;

        return raceRepository.save(race);
    }

    public List<Race> getAllRaces(){return raceRepository.findAll();}

    public Race getRaceById(Long id){
        return raceRepository.findById(id)
                .orElseThrow(()->
                        new RaceNotFoundException(
                                "Race with id " + id + " not found"));
    }

    public Race updateRace(Long id, RaceUpdateRequest request) {
        Race existingRace = raceRepository.findById(id)
                .orElseThrow(()->
                        new RaceNotFoundException(
                                "Race with id " + id + " not found"
                        ));
        existingRace.name = request.name;
        existingRace.location = request.location;
        existingRace.startDate = request.startDate;
        existingRace.maxParticipants = request.maxParticipants;

        return raceRepository.save(existingRace);
    }

    public void deleteRaceById(Long id) {
        if (!raceRepository.existsById(id)) {
            throw new RaceNotFoundException(
                    "Race with id " + id + " not found"
            );
        }
        raceRepository.deleteById(id);
    }


}
