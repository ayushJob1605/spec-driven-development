package com.supportticket.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Centralized ticket status transition rules per spec/state-machine.md.
 */
public final class TicketStateMachine {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED = new EnumMap<>(TicketStatus.class);

    static {
        ALLOWED.put(TicketStatus.OPEN, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
        ALLOWED.put(TicketStatus.IN_PROGRESS, EnumSet.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED));
        ALLOWED.put(TicketStatus.RESOLVED, EnumSet.of(TicketStatus.CLOSED));
        ALLOWED.put(TicketStatus.CLOSED, EnumSet.noneOf(TicketStatus.class));
        ALLOWED.put(TicketStatus.CANCELLED, EnumSet.noneOf(TicketStatus.class));
    }

    private TicketStateMachine() {
    }

    public static boolean canTransition(TicketStatus from, TicketStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    public static Set<TicketStatus> allowedTargets(TicketStatus from) {
        if (from == null) {
            return Set.of();
        }
        return Set.copyOf(ALLOWED.getOrDefault(from, Set.of()));
    }
}
