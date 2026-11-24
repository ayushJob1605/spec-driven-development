package com.supportticket.api.dto;

import com.supportticket.domain.TicketStatus;

public record StatusChangeRequest(TicketStatus status) {
}
