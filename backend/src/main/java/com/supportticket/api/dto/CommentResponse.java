package com.supportticket.api.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID ticketId,
        String content,
        String author,
        Instant createdAt,
        Instant updatedAt) {
}
