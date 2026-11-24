package com.supportticket.api.dto;

import com.supportticket.domain.Priority;

public record CreateTicketRequest(
        String title,
        String description,
        Priority priority,
        String assignee) {
}
