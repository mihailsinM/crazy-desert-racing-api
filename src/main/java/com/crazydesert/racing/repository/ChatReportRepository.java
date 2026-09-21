package com.crazydesert.racing.repository;

import com.crazydesert.racing.ChatReport;
import com.crazydesert.racing.enums.ChatReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatReportRepository extends JpaRepository<ChatReport, Long> {

    boolean existsByMessageIdAndReporterId(Long messageId, Long reporterId);

    boolean existsByMessageId(Long messageId);

    List<ChatReport> findByStatusOrderByCreatedAtAsc(ChatReportStatus status);
}
