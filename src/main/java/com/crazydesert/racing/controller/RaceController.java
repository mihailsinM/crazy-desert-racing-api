package com.crazydesert.racing.controller;

import com.crazydesert.racing.dto.ImageFramingRequest;
import com.crazydesert.racing.dto.RaceCreateRequest;
import com.crazydesert.racing.dto.RacePublicationSyncResponse;
import com.crazydesert.racing.dto.RaceResponse;
import com.crazydesert.racing.dto.RaceUpdateRequest;
import com.crazydesert.racing.service.RaceService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/races")
public class RaceController {

    private final RaceService raceService;

    public RaceController(RaceService raceService) {
        this.raceService = raceService;
    }

    @PostMapping
    public RaceResponse createRace(
            Authentication authentication,
            @Valid @RequestBody RaceCreateRequest request){
        return raceService.createRace(
                authentication.getName(),
                request
        );
    }

    @GetMapping
    public List<RaceResponse> getAllRaces() {
        return raceService.getAllRaces();
    }

    @GetMapping("/{id}")
    public RaceResponse getRaceById(@PathVariable Long id) {
        return raceService.getRaceById(id);
    }

    @PutMapping("/{id}")
    public RaceResponse updateRace(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody RaceUpdateRequest request){

        return raceService.updateRace(
                authentication.getName(),
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    public String deleteRaceById(@PathVariable Long id){
        raceService.deleteRaceById(id);
        return "Race deleted with id: " + id;
    }

    @PostMapping("/publications/synchronize")
    public RacePublicationSyncResponse synchronizeRacePublications(
            Authentication authentication) {

        return new RacePublicationSyncResponse(
                raceService.synchronizeRacePublications(
                        authentication.getName()
                )
        );
    }

    @PutMapping(
            value = "/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public RaceResponse updateRaceImage(
            @PathVariable Long id,
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

        return raceService.updateRaceImage(
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
    public RaceResponse updateRaceImageFraming(
            @PathVariable Long id,
            @RequestBody ImageFramingRequest request) {

        return raceService.updateRaceImageFraming(id, request);
    }

    @DeleteMapping("/{id}/image")
    public RaceResponse deleteRaceImage(@PathVariable Long id) {
        return raceService.deleteRaceImage(id);
    }

}
