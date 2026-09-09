package com.crazydesert.racing.repository;

import com.crazydesert.racing.UserPhoto;
import com.crazydesert.racing.enums.UserPhotoVisibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserPhotoRepository extends JpaRepository<UserPhoto, Long> {

    List<UserPhoto> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<UserPhoto> findByOwnerIdAndVisibilityInOrderByCreatedAtDesc(
            Long ownerId,
            Collection<UserPhotoVisibility> visibilities
    );

    long countByOwnerId(Long ownerId);

    long countByOwnerIdAndVisibilityIn(
            Long ownerId,
            Collection<UserPhotoVisibility> visibilities
    );
}
