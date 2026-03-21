package com.flashlink.demoflashlink_url_service.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SnowflakeIdGenerator Tests")
class SnowflakeIdGeneratorTest {

    private SnowflakeIdGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new SnowflakeIdGenerator(1L);
    }

    @Test
    @DisplayName("Should generate unique IDs")
    void nextId_ShouldGenerateUniqueIds() {
        // Given
        long id1 = generator.nextId();
        long id2 = generator.nextId();

        // Then
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("Should generate IDs in chronological order")
    void nextId_ShouldGenerateIdsInChronologicalOrder() {
        // Given
        long id1 = generator.nextId();
        long id2 = generator.nextId();
        long id3 = generator.nextId();

        // Then
        assertThat(id2).isGreaterThan(id1);
        assertThat(id3).isGreaterThan(id2);
    }

    @Test
    @DisplayName("Should generate positive IDs")
    void nextId_ShouldGeneratePositiveIds() {
        // When
        long id = generator.nextId();

        // Then
        assertThat(id).isPositive();
    }

    @Test
    @DisplayName("Should generate IDs within expected range")
    void nextId_ShouldGenerateIdsWithinExpectedRange() {
        // When
        long id = generator.nextId();

        // Then
        assertThat(id).isLessThan(Long.MAX_VALUE);
        assertThat(id).isGreaterThan(0L);
    }

    @Test
    @DisplayName("Should handle rapid successive calls")
    void nextId_ShouldHandleRapidSuccessiveCalls() {
        // When
        long[] ids = new long[1000];
        for (int i = 0; i < 1000; i++) {
            ids[i] = generator.nextId();
        }

        // Then
        for (int i = 1; i < ids.length; i++) {
            assertThat(ids[i]).isGreaterThan(ids[i - 1]);
        }
    }

    @Test
    @DisplayName("Should incorporate node ID")
    void nextId_ShouldIncorporateNodeId() {
        // Given
        SnowflakeIdGenerator generator1 = new SnowflakeIdGenerator(1L);
        SnowflakeIdGenerator generator2 = new SnowflakeIdGenerator(2L);

        // When
        long id1 = generator1.nextId();
        long id2 = generator2.nextId();

        // Then
        // IDs should be different due to different node IDs
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("Should handle maximum node ID")
    void nextId_ShouldHandleMaximumNodeId() {
        // Given
        long maxNodeId = 1023L; // Maximum valid node ID for Snowflake
        SnowflakeIdGenerator maxNodeGenerator = new SnowflakeIdGenerator(maxNodeId);

        // When
        long id = maxNodeGenerator.nextId();

        // Then
        assertThat(id).isPositive();
    }

    @Test
    @DisplayName("Should throw exception for invalid node ID")
    void constructor_ShouldThrowExceptionForInvalidNodeId() {
        // Given
        long invalidNodeId = 1024L; // Beyond maximum valid node ID

        // When & Then
        assertThatThrownBy(() -> new SnowflakeIdGenerator(invalidNodeId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should handle negative node ID")
    void constructor_ShouldHandleNegativeNodeId() {
        // Given
        long negativeNodeId = -1L;

        // When & Then
        assertThatThrownBy(() -> new SnowflakeIdGenerator(negativeNodeId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should generate IDs with proper structure")
    void nextId_ShouldGenerateIdsWithProperStructure() {
        // When
        long id = generator.nextId();

        // Then
        // Snowflake ID should be 64 bits (less than 2^63)
        assertThat(id).isLessThan(1L << 63);
        
        // Should be non-zero
        assertThat(id).isNotZero();
    }

    @Test
    @DisplayName("Should generate IDs consistently over time")
    void nextId_ShouldGenerateConsistentlyOverTime() throws InterruptedException {
        // Given
        long id1 = generator.nextId();
        
        // When
        Thread.sleep(1); // Small delay to ensure timestamp increment
        long id2 = generator.nextId();
        
        // Then
        assertThat(id2).isGreaterThan(id1);
        
        // The difference should be at least the timestamp increment
        long difference = id2 - id1;
        assertThat(difference).isGreaterThan(0);
    }
}
