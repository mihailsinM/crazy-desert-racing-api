package com.crazydesert.racing.controller;

import com.crazydesert.racing.dto.ChatReportResponse;
import com.crazydesert.racing.dto.ChatReportReviewRequest;
import com.crazydesert.racing.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat/admin/reports")
public class ChatAdminController {

    private final ChatService chatService;

    public ChatAdminController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ChatReportResponse> getOpenReports(
            Authentication authentication) {

        return chatService.getOpenReports(authentication.getName());
    }

    @PutMapping("/{reportId}")
    public ChatReportResponse reviewReport(
            Authentication authentication,
            @PathVariable Long reportId,
            @Valid @RequestBody ChatReportReviewRequest request) {

        return chatService.reviewReport(
                authentication.getName(),
                reportId,
                request
        );
    }
}
