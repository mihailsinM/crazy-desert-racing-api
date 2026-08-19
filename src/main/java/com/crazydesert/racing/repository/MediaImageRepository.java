package com.crazydesert.racing.repository;

import com.crazydesert.racing.MediaImage;
import com.crazydesert.racing.enums.MediaImageVisibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaImageRepository extends JpaRepository<MediaImage, Long> {

    Optional<MediaImage> findByImageKey(String imageKey);

    Optional<MediaImage> findByImageKeyAndVisibility(
            String imageKey,
            MediaImageVisibility visibility);
}
