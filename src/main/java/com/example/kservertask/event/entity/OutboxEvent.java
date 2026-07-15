package com.example.kservertask.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "outbox_event",
        indexes = @Index(
                name = "idx_outbox_event_claim",
                columnList = "status, next_retry_at, claimed_until"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @Column(name = "event_id", length = 36)
    private String eventId;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "claimed_by", length = 100)
    private String claimedBy;

    @Column(name = "claimed_until")
    private Instant claimedUntil;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    public OutboxEvent(
            String eventId,
            Long orderId,
            String eventType,
            String payload,
            String status,
            int retryCount,
            Instant nextRetryAt,
            Instant createdAt
    ) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.nextRetryAt = nextRetryAt;
        this.createdAt = createdAt;
    }
}
