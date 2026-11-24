package com.supportticket.domain;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED;

    public boolean isTerminal() {
        return this == CLOSED || this == CANCELLED;
    }
}
