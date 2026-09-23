package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.ChatSupportTopic;
import jakarta.validation.constraints.NotNull;

public record ChatSupportTopicRequest(@NotNull ChatSupportTopic topic) {
}
