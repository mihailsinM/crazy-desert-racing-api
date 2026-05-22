package com.crazydesert.racing;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;
    public int age;
    public String email;

    @OneToMany(mappedBy = "owner")
    @JsonManagedReference
    public List<RaceCar> raceCars;
}
