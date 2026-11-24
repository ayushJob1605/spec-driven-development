-- Compatible with PostgreSQL (dev/demo) and H2 in PostgreSQL mode (tests).
-- Enum values stored as VARCHAR; application enforces allowed values.

CREATE TABLE ticket (
    id              UUID                        NOT NULL,
    title           VARCHAR(200)                NOT NULL,
    description     VARCHAR(5000)               NOT NULL,
    priority        VARCHAR(20)                 NOT NULL,
    status          VARCHAR(20)                 NOT NULL,
    assignee        VARCHAR(100),
    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL,
    CONSTRAINT pk_ticket PRIMARY KEY (id)
);

CREATE INDEX idx_ticket_created_at ON ticket (created_at DESC);
CREATE INDEX idx_ticket_status ON ticket (status);

CREATE TABLE comment (
    id              UUID                        NOT NULL,
    ticket_id       UUID                        NOT NULL,
    content         VARCHAR(2000)               NOT NULL,
    author          VARCHAR(100),
    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL,
    CONSTRAINT pk_comment PRIMARY KEY (id),
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id) ON DELETE CASCADE
);

CREATE INDEX idx_comment_ticket_id ON comment (ticket_id);
