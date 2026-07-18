package com.example.kservertask.event.repository;

import java.time.Instant;

import com.example.kservertask.event.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO processed_event (event_id, processed_at, created_at, updated_at)
            VALUES (:eventId, :processedAt, :processedAt, :processedAt)
            """, nativeQuery = true)
    int claimEvent(
            @Param("eventId") String eventId,
            @Param("processedAt") Instant processedAt
    );
}
