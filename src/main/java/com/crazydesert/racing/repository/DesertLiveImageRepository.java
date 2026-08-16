package com.crazydesert.racing.repository;

import com.crazydesert.racing.DesertLiveImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DesertLiveImageRepository
        extends JpaRepository<DesertLiveImage, Long> {

    Optional<DesertLiveImage> findByItemId(Long itemId);

    Optional<DesertLiveImage> findByItem_ImageKey(String imageKey);

    void deleteByItemId(Long itemId);
}
