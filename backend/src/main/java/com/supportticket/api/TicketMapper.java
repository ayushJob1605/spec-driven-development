package com.supportticket.api;

import com.supportticket.api.dto.CommentResponse;
import com.supportticket.api.dto.TicketResponse;
import com.supportticket.domain.Comment;
import com.supportticket.domain.Ticket;
import java.util.List;

public final class TicketMapper {

    private TicketMapper() {
    }

    public static TicketResponse toTicketResponse(Ticket ticket, boolean includeComments) {
        List<CommentResponse> comments = includeComments
                ? ticket.getComments().stream().map(TicketMapper::toCommentResponse).toList()
                : List.of();
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getAssignee(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                comments);
    }

    public static CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                comment.getContent(),
                comment.getAuthor(),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }
}
