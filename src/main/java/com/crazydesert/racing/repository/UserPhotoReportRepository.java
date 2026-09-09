package com.crazydesert.racing.repository;

import com.crazydesert.racing.UserPhotoReport;
import com.crazydesert.racing.enums.UserPhotoReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPhotoReportRepository
        extends JpaRepository<UserPhotoReport, Long> {

    boolean existsByPhotoIdAndReporterId(Long photoId, Long reporterId);

    List<UserPhotoReport> findByStatusOrderByCreatedAtAsc(
            UserPhotoReportStatus status
    );

    void deleteByPhotoId(Long photoId);
}
