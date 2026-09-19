package com.crazydesert.racing.repository;

import com.crazydesert.racing.RaceCar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaceCarRepository extends JpaRepository <RaceCar, Long>{

    List<RaceCar> findByOwnerId(Long ownerId);

    List<RaceCar> findByGalleryPhotoId(Long photoId);

    List<RaceCar> findDistinctByGalleryPhotosId(Long photoId);

    long countByOwnerId(Long ownerId);
}
