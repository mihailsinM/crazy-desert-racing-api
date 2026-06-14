package com.crazydesert.racing.repository;

import com.crazydesert.racing.RaceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaceRegistrationRepository extends JpaRepository<RaceRegistration, Long> {

    boolean existsByUserIdAndRaceCarIdAndRaceId(
            Long userId,
            Long raceCarId,
            Long raceId);

    long countByRaceId(Long raceId);

    List<RaceRegistration> findByRaceId(Long raceId);

}
