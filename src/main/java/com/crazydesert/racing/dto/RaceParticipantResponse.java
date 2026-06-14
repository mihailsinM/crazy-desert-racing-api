package com.crazydesert.racing.dto;

public class RaceParticipantResponse {

    private Long registrationId;
    private Long userId;
    private String userName;
    private Long raceCarId;
    private String carName;
    private String carBrand;

    public RaceParticipantResponse(
            Long registrationId,
            Long userId,
            String userName,
            Long raceCarId,
            String carName,
            String carBrand
    ) {
        this.registrationId = registrationId;
        this.userId = userId;
        this.userName = userName;
        this.raceCarId = raceCarId;
        this.carName = carName;
        this.carBrand = carBrand;
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public Long getRaceCarId() {
        return raceCarId;
    }

    public String getCarName() {
        return carName;
    }

    public String getCarBrand() {
        return carBrand;
    }
}