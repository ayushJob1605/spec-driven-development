package com.supportticket.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TicketStateMachineTest {

    @ParameterizedTest
    @MethodSource("validTransitions")
    void allowsExactValidTransitions(TicketStatus from, TicketStatus to) {
        assertThat(TicketStateMachine.canTransition(from, to)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("invalidTransitions")
    void rejectsInvalidTransitions(TicketStatus from, TicketStatus to) {
        assertThat(TicketStateMachine.canTransition(from, to)).isFalse();
    }

    @Test
    void allowedTargetsForOpen() {
        assertThat(TicketStateMachine.allowedTargets(TicketStatus.OPEN))
                .containsExactlyInAnyOrder(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED);
    }

    @Test
    void terminalStatesHaveNoTargets() {
        assertThat(TicketStateMachine.allowedTargets(TicketStatus.CLOSED)).isEmpty();
        assertThat(TicketStateMachine.allowedTargets(TicketStatus.CANCELLED)).isEmpty();
    }

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.CLOSED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.OPEN, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.OPEN),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.OPEN, TicketStatus.OPEN),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.IN_PROGRESS));
    }
}
