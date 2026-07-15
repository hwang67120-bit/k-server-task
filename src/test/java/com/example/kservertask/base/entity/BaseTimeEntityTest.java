package com.example.kservertask.base.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class BaseTimeEntityTest {

    @Test
    void recordsCreateAndUpdateTime() {
        TestEntity entity = new TestEntity();

        entity.markCreated();
        Instant createdAt = entity.getCreatedAt();

        entity.markUpdated();

        assertThat(createdAt).isNotNull();
        assertThat(entity.getUpdatedAt()).isAfterOrEqualTo(createdAt);
    }

    private static class TestEntity extends BaseTimeEntity {

        void markCreated() {
            createTime();
        }

        void markUpdated() {
            updateTime();
        }
    }
}
