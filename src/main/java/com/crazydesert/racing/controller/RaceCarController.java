package com.crazydesert.racing.controller;

import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.dto.RaceCarCreateRequest;
import com.crazydesert.racing.dto.RaceCarUpdateRequest;
import com.crazydesert.racing.service.RaceCarService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/race-cars")
public class RaceCarController {

    private final RaceCarService raceCarService;

    public RaceCarController(RaceCarService raceCarService){
        this.raceCarService = raceCarService;
    }

    @PostMapping
    public RaceCar createRaceCar(
            @Valid @RequestBody RaceCarCreateRequest request) {
        return raceCarService.createRaceCar(request);
    }

    @GetMapping
    public List<RaceCar> getAllRaceCars(){
        return raceCarService.getAllRaceCars();
    }

    @GetMapping("/{id}")
    public RaceCar getRaceCarById(@PathVariable Long id){
        return raceCarService.getRaceCarById(id);
    }

    @PutMapping("/{id}")
    public RaceCar updateRaceCar(
            @PathVariable Long id,
            @Valid @RequestBody RaceCarUpdateRequest request){

        return raceCarService.updateRaceCar(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteRaceCarById(@PathVariable Long id){
        raceCarService.deleteRaceCarById(id);
        return "Race car deleted with id: " + id;
    }

    @PostMapping("/{raceCarId}/owner/{userId}")
    public RaceCar assignCarToUser(
            @PathVariable Long raceCarId,
            @PathVariable Long userId) {

        return raceCarService.assignCarToUser(userId, raceCarId);
    }
}
