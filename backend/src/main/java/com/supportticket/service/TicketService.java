package com.supportticket.service;

import com.supportticket.api.FieldErrorItem;
import com.supportticket.api.TicketMapper;
import com.supportticket.api.dto.CommentResponse;
import com.supportticket.api.dto.CreateCommentRequest;
import com.supportticket.api.dto.CreateTicketRequest;
import com.supportticket.api.dto.PageResponse;
import com.supportticket.api.dto.StatusChangeRequest;
import com.supportticket.api.dto.TicketResponse;
import com.supportticket.api.dto.UpdateTicketRequest;
import com.supportticket.domain.Comment;
import com.supportticket.domain.Priority;
import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketStateMachine;
import com.supportticket.domain.TicketStatus;
import com.supportticket.persistence.TicketRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request) {
        String title = requireText(request.title(), "title", 200);
        String description = requireText(request.description(), "description", 5000);
        Priority priority = requirePriority(request.priority());
        String assignee = optionalAssignee(request.assignee());

        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setAssignee(assignee);

        Ticket saved = ticketRepository.save(ticket);
        return TicketMapper.toTicketResponse(saved, true);
    }

    @Transactional(readOnly = true)
    public TicketResponse getById(UUID id) {
        Ticket ticket = ticketRepository
                .findWithCommentsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
        return TicketMapper.toTicketResponse(ticket, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> list(int page, int size, TicketStatus status, String q) {
        if (page < 0) {
            throw new ValidationException(
                    "page must be >= 0", List.of(new FieldErrorItem("page", "must be >= 0")));
        }
        if (size < 1 || size > 100) {
            throw new ValidationException(
                    "size must be between 1 and 100",
                    List.of(new FieldErrorItem("size", "must be between 1 and 100")));
        }

        String query = (q == null || q.isBlank()) ? null : q.trim();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Ticket> result = ticketRepository.search(status, query, pageable);

        List<TicketResponse> content = result.getContent().stream()
                .map(ticket -> TicketMapper.toTicketResponse(ticket, false))
                .toList();

        return new PageResponse<>(
                content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public TicketResponse update(UUID id, UpdateTicketRequest request) {
        if (!request.hasAnyField()) {
            throw new ValidationException(
                    "At least one field must be provided",
                    List.of(new FieldErrorItem("body", "at least one field is required")));
        }

        Ticket ticket = ticketRepository
                .findWithCommentsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));

        assertEditable(ticket);

        if (request.isTitlePresent()) {
            ticket.setTitle(requireText(request.getTitle(), "title", 200));
        }
        if (request.isDescriptionPresent()) {
            ticket.setDescription(requireText(request.getDescription(), "description", 5000));
        }
        if (request.isPriorityPresent()) {
            ticket.setPriority(requirePriority(request.getPriority()));
        }
        if (request.isAssigneePresent()) {
            ticket.setAssignee(optionalAssignee(request.getAssignee()));
        }

        return TicketMapper.toTicketResponse(ticketRepository.save(ticket), true);
    }

    @Transactional
    public TicketResponse changeStatus(UUID id, StatusChangeRequest request) {
        if (request.status() == null) {
            throw new ValidationException(
                    "status is required", List.of(new FieldErrorItem("status", "must not be null")));
        }

        Ticket ticket = ticketRepository
                .findWithCommentsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));

        TicketStatus from = ticket.getStatus();
        TicketStatus to = request.status();
        if (!TicketStateMachine.canTransition(from, to)) {
            throw new InvalidTransitionException(
                    "Invalid status transition from " + from + " to " + to);
        }

        ticket.setStatus(to);
        return TicketMapper.toTicketResponse(ticketRepository.save(ticket), true);
    }

    @Transactional
    public CommentResponse addComment(UUID id, CreateCommentRequest request) {
        Ticket ticket = ticketRepository
                .findWithCommentsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));

        if (ticket.getStatus().isTerminal()) {
            throw new ValidationException(
                    "Comments cannot be added to CLOSED or CANCELLED tickets",
                    List.of(new FieldErrorItem("status", "ticket is terminal")));
        }

        String content = requireText(request.content(), "content", 2000);
        String author = optionalAuthor(request.author());

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        ticket.addComment(comment);
        ticketRepository.save(ticket);

        return TicketMapper.toCommentResponse(comment);
    }

    private static void assertEditable(Ticket ticket) {
        if (ticket.getStatus().isTerminal()) {
            throw new ValidationException(
                    "CLOSED and CANCELLED tickets cannot be edited",
                    List.of(new FieldErrorItem("status", "ticket is terminal")));
        }
    }

    private static Priority requirePriority(Priority priority) {
        if (priority == null) {
            throw new ValidationException(
                    "priority is required", List.of(new FieldErrorItem("priority", "must not be null")));
        }
        return priority;
    }

    private static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(
                    field + " must not be blank", List.of(new FieldErrorItem(field, "must not be blank")));
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException(
                    field + " must not be blank", List.of(new FieldErrorItem(field, "must not be blank")));
        }
        if (trimmed.length() > maxLength) {
            throw new ValidationException(
                    field + " must be at most " + maxLength + " characters",
                    List.of(new FieldErrorItem(field, "must be at most " + maxLength + " characters")));
        }
        return trimmed;
    }

    private static String optionalAssignee(String assignee) {
        if (assignee == null) {
            return null;
        }
        String trimmed = assignee.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException(
                    "assignee must not be blank when provided",
                    List.of(new FieldErrorItem("assignee", "must not be blank when provided")));
        }
        if (trimmed.length() > 100) {
            throw new ValidationException(
                    "assignee must be at most 100 characters",
                    List.of(new FieldErrorItem("assignee", "must be at most 100 characters")));
        }
        return trimmed;
    }

    private static String optionalAuthor(String author) {
        if (author == null) {
            return null;
        }
        String trimmed = author.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 100) {
            throw new ValidationException(
                    "author must be at most 100 characters",
                    List.of(new FieldErrorItem("author", "must be at most 100 characters")));
        }
        return trimmed;
    }
}
