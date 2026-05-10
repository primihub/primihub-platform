package com.primihub.biz.util.snowflake;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SnowflakeIdTest {

    @Test
    void nextId_shouldReturnPositiveNumber() {
        SnowflakeId id = new SnowflakeId();

        long result = id.nextId();

        assertThat(result).isPositive();
    }

    @Test
    void nextId_shouldBeMonotonicallyIncreasing() {
        SnowflakeId id = new SnowflakeId();

        long id1 = id.nextId();
        long id2 = id.nextId();
        long id3 = id.nextId();

        assertThat(id1).isLessThan(id2);
        assertThat(id2).isLessThan(id3);
    }

    @Test
    void nextId_shouldBeUniqueAcrossMultipleCalls() {
        SnowflakeId id = new SnowflakeId();
        Set<Long> ids = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            ids.add(id.nextId());
        }

        assertThat(ids).hasSize(1000);
    }

    @Test
    void getInstance_shouldReturnSingleton() {
        SnowflakeId instance1 = SnowflakeId.getInstance();
        SnowflakeId instance2 = SnowflakeId.getInstance();

        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    void id_shouldContainTimestampComponent() {
        SnowflakeId id = new SnowflakeId();

        long id1 = id.nextId();
        long id2 = id.nextId();

        long diff = id2 - id1;
        assertThat(diff).isBetween(0L, 1000000L);
    }

    @Test
    void workerId_shouldBeWithinRange() {
        SnowflakeId id = new SnowflakeId();

        long nextId = id.nextId();
        long workerId = (nextId >> 12) & 0x1F;

        assertThat(workerId).isBetween(0L, 31L);
    }

    @Test
    void datacenterId_shouldBeWithinRange() {
        SnowflakeId id = new SnowflakeId();

        long nextId = id.nextId();
        long datacenterId = (nextId >> 17) & 0x1F;

        assertThat(datacenterId).isBetween(0L, 31L);
    }

    @Test
    void tilNextMillis_shouldReturnLaterTimestamp() {
        SnowflakeId id = new SnowflakeId();
        long current = System.currentTimeMillis();

        long result = id.tilNextMillis(current);

        assertThat(result).isGreaterThan(current);
    }

    @Test
    void timeGen_shouldReturnCurrentTime() {
        SnowflakeId id = new SnowflakeId();
        long before = System.currentTimeMillis();

        long result = id.timeGen();

        long after = System.currentTimeMillis();
        assertThat(result).isBetween(before, after);
    }
}
