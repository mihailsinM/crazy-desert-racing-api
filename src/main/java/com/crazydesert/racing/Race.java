package com.crazydesert.racing;

import com.crazydesert.racing.enums.RaceStatus;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "races")
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private LocalDate startDate;
    private int maxParticipants;

    @Enumerated(EnumType.STRING)
    private RaceStatus status;

    @Column(length = 1000)
    private String adminMessage;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public RaceStatus getStatus() {
        return status;
    }

    public String getAdminMessage() {
        return adminMessage;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public void setStatus(RaceStatus status) {
        this.status = status;
    }

    public void setAdminMessage(String adminMessage) {
        this.adminMessage = adminMessage;
    }
}