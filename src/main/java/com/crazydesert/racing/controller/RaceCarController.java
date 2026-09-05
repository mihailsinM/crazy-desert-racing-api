package com.crazydesert.racing.controller;

import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.dto.ImageFramingRequest;
import com.crazydesert.racing.dto.RaceCarCreateRequest;
import com.crazydesert.racing.dto.RaceCarUpdateRequest;
import com.crazydesert.racing.service.RaceCarService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/my")
    public List<RaceCar> getMyRaceCars(Authentication authentication) {
        String email = authentication.getName();

        return raceCarService.getRaceCarsByOwnerEmail(email);
    }

    @PostMapping("/my")
    public RaceCar createMyRaceCar(
            Authentication authentication,
            @Valid @RequestBody RaceCarCreateRequest request) {

        String email = authentication.getName();

        return raceCarService.createMyRaceCar(email, request);
    }

    @GetMapping("/{id}")
    public RaceCar getRaceCarById(@PathVariable Long id){
        return raceCarService.getRaceCarById(id);
    }

    @PutMapping("/{id}")
    public RaceCar updateRaceCar(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody RaceCarUpdateRequest request){

        return raceCarService.updateRaceCar(authentication.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteRaceCarById(
            @PathVariable Long id,
            Authentication authentication){

        raceCarService.deleteRaceCarById(authentication.getName(), id);
        return "Race car deleted with id: " + id;
    }

    @PutMapping(
            value = "/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public RaceCar updateRaceCarImage(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam("file") MultipartFile image,
            @RequestParam(required = false) Integer focusX,
            @RequestParam(required = false) Integer focusY,
            @RequestParam(required = false) Integer cropPercent,
            @RequestParam(required = false) Integer avatarFocusX,
            @RequestParam(required = false) Integer avatarFocusY,
            @RequestParam(required = false) Integer avatarCropPercent,
            @RequestParam(required = false) Integer cardFocusX,
            @RequestParam(required = false) Integer cardFocusY,
            @RequestParam(required = false) Integer cardCropPercent) {

        return raceCarService.updateRaceCarImage(
                authentication.getName(),
                id,
                image,
                ImageFramingRequest.fromParameters(
                        focusX,
                        focusY,
                        cropPercent,
                        avatarFocusX,
                        avatarFocusY,
                        avatarCropPercent,
                        cardFocusX,
                        cardFocusY,
                        cardCropPercent
                )
        );
    }

    @PutMapping("/{id}/image/framing")
    public RaceCar updateRaceCarImageFraming(
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody ImageFramingRequest request) {

        return raceCarService.updateRaceCarImageFraming(
                authentication.getName(),
                id,
                request
        );
    }

    @DeleteMapping("/{id}/image")
    public RaceCar deleteRaceCarImage(
            @PathVariable Long id,
            Authentication authentication) {

        return raceCarService.deleteRaceCarImage(
                authentication.getName(),
                id
        );
    }

    @PostMapping("/{raceCarId}/owner/{userId}")
    public RaceCar assignCarToUser(
            @PathVariable Long raceCarId,
            @PathVariable Long userId) {

        return raceCarService.assignCarToUser(userId, raceCarId);
    }

}
