package com.supportticket.persistence;

import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @EntityGraph(attributePaths = "comments")
    @Query("SELECT t FROM Ticket t WHERE t.id = :id")
    Optional<Ticket> findWithCommentsById(@Param("id") UUID id);

    @Query("""
            SELECT t FROM Ticket t
            WHERE (:status IS NULL OR t.status = :status)
              AND (
                   :q IS NULL
                   OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
                   OR LOWER(t.description) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
              )
            """)
    Page<Ticket> search(
            @Param("status") TicketStatus status, @Param("q") String q, Pageable pageable);
}
