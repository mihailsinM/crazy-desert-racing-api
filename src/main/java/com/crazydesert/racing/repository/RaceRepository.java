package com.crazydesert.racing.repository;

import com.crazydesert.racing.Race;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RaceRepository extends JpaRepository<Race, Long> {
}
