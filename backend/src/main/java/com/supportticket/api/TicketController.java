package com.supportticket.api;

import com.supportticket.api.dto.CommentResponse;
import com.supportticket.api.dto.CreateCommentRequest;
import com.supportticket.api.dto.CreateTicketRequest;
import com.supportticket.api.dto.PageResponse;
import com.supportticket.api.dto.StatusChangeRequest;
import com.supportticket.api.dto.TicketResponse;
import com.supportticket.api.dto.UpdateTicketRequest;
import com.supportticket.domain.TicketStatus;
import com.supportticket.service.TicketService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request));
    }

    @GetMapping
    public PageResponse<TicketResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) String q) {
        return ticketService.list(page, size, status, q);
    }

    @GetMapping("/{id}")
    public TicketResponse get(@PathVariable UUID id) {
        return ticketService.getById(id);
    }

    @PatchMapping("/{id}")
    public TicketResponse update(@PathVariable UUID id, @RequestBody UpdateTicketRequest request) {
        return ticketService.update(id, request);
    }

    @PostMapping("/{id}/status")
    public TicketResponse changeStatus(@PathVariable UUID id, @RequestBody StatusChangeRequest request) {
        return ticketService.changeStatus(id, request);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID id, @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.addComment(id, request));
    }
}
