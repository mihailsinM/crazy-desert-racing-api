package com.crazydesert.racing;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "race_cars")
public class RaceCar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    public Long id;

    public String name;
    public String brand;
    public int horsePower;
    public String imageUrl;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "user_id")
    public User owner;
}
