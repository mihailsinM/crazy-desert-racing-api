package com.crazydesert.racing.controller;

import com.crazydesert.racing.Race;
import com.crazydesert.racing.dto.RaceCreateRequest;
import com.crazydesert.racing.dto.RaceUpdateRequest;
import com.crazydesert.racing.service.RaceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/races")
public class RaceController {

    private final RaceService raceService;

    public RaceController(RaceService raceService) {
        this.raceService = raceService;
    }

    @PostMapping
    public Race createRace(
            @Valid @RequestBody RaceCreateRequest request){
        return raceService.createRace(request);
    }

    @GetMapping
    public List<Race> getAllRaces() {return raceService.getAllRaces();}

    @GetMapping("/{id}")
    public Race getRaceById(@PathVariable Long id) {return raceService.getRaceById(id);}

    @PutMapping("/{id}")
    public Race updateRace(
            @PathVariable Long id,
            @Valid @RequestBody RaceUpdateRequest request){

        return raceService.updateRace(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteRaceById(@PathVariable Long id){
        raceService.deleteRaceById(id);
        return "Race deleted with id: " + id;
    }


}
