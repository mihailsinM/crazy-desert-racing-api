package com.crazydesert.racing.service;

import com.crazydesert.racing.Race;
import com.crazydesert.racing.dto.RaceResponse;
import org.springframework.stereotype.Component;

@Component
public class RaceMapper {

    public RaceResponse toResponse(Race race) {
        return new RaceResponse(
                race.getId(),
                race.getName(),
                race.getLocation(),
                race.getStartDate(),
                race.getMaxParticipants(),
                race.getStatus(),
                race.getAdminMessage(),
                race.getImageUrl(),
                race.getImageFocusX(),
                race.getImageFocusY(),
                race.getImageCropPercent(),
                race.getImageFraming()
        );
    }
}
