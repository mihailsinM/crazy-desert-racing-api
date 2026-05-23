package com.crazydesert.racing;

import jakarta.persistence.*;
@Entity
@Table(name = "race_registrations")
public class RaceRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;

    @ManyToOne
    @JoinColumn(name = "race_car_id")
    public RaceCar raceCar;

    @ManyToOne
    @JoinColumn(name = "race_id")
    public Race race;
}