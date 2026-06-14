package com.crazydesert.racing;

import jakarta.persistence.*;

@Entity
@Table(name = "race_registrations")
public class RaceRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "race_car_id")
    private RaceCar raceCar;

    @ManyToOne
    @JoinColumn(name = "race_id")
    private Race race;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public RaceCar getRaceCar() {
        return raceCar;
    }

    public Race getRace() {
        return race;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setRaceCar(RaceCar raceCar) {
        this.raceCar = raceCar;
    }

    public void setRace(Race race) {
        this.race = race;
    }
}