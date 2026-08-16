package com.crazydesert.racing.repository;

import com.crazydesert.racing.DesertLiveItem;
import com.crazydesert.racing.enums.DesertLiveCategory;
import com.crazydesert.racing.enums.DesertLiveModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface DesertLiveItemRepository
        extends JpaRepository<DesertLiveItem, Long> {

    @Query("""
            select item
            from DesertLiveItem item
            where item.moderationStatus = :status
              and (:category is null or item.category = :category)
              and (item.activeFrom is null or item.activeFrom <= :now)
              and (item.activeUntil is null or item.activeUntil > :now)
            order by item.displayPriority desc, function('random')
            """)
    List<DesertLiveItem> findRandomActive(
            @Param("status") DesertLiveModerationStatus status,
            @Param("category") DesertLiveCategory category,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Query("""
            select item
            from DesertLiveItem item
            where item.moderationStatus = :status
              and (:category is null or item.category = :category)
              and (item.activeFrom is null or item.activeFrom <= :now)
              and (item.activeUntil is null or item.activeUntil > :now)
              and (
                    :search = ''
                    or lower(item.title) like concat('%', :search, '%')
                    or lower(item.description) like concat('%', :search, '%')
                    or lower(item.createdBy.name) like concat('%', :search, '%')
              )
            """)
    Page<DesertLiveItem> findActive(
            @Param("status") DesertLiveModerationStatus status,
            @Param("category") DesertLiveCategory category,
            @Param("search") String search,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Query("""
            select item
            from DesertLiveItem item
            where item.createdBy.id = :authorId
              and (:status is null or item.moderationStatus = :status)
              and (:category is null or item.category = :category)
              and (
                    :search = ''
                    or lower(item.title) like concat('%', :search, '%')
                    or lower(item.description) like concat('%', :search, '%')
              )
            """)
    Page<DesertLiveItem> findByAuthor(
            @Param("authorId") Long authorId,
            @Param("status") DesertLiveModerationStatus status,
            @Param("category") DesertLiveCategory category,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
            select item
            from DesertLiveItem item
            where (:status is null or item.moderationStatus = :status)
              and (:category is null or item.category = :category)
              and (
                    :search = ''
                    or lower(item.title) like concat('%', :search, '%')
                    or lower(item.description) like concat('%', :search, '%')
                    or lower(item.createdBy.name) like concat('%', :search, '%')
              )
            """)
    Page<DesertLiveItem> findForAdmin(
            @Param("status") DesertLiveModerationStatus status,
            @Param("category") DesertLiveCategory category,
            @Param("search") String search,
            Pageable pageable
    );
}
