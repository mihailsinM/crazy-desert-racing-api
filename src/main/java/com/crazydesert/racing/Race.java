package com.crazydesert.racing;

import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
@Table(name = "races")
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;
    public String location;
    public LocalDate startDate;
    public int maxParticipants;
}
