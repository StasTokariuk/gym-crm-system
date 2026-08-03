package com.gym.crm.health;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class StorageHealthIndicatorTest {

    @InjectMocks
    private StorageHealthIndicator storageHealthIndicator;

    @Test
    @DisplayName("Health is UP when all CSV files exist on classpath")
    void health_AllFilesPresent_ShouldBeUp() {
        ReflectionTestUtils.setField(storageHealthIndicator, "traineesFile", "trainees.csv");
        ReflectionTestUtils.setField(storageHealthIndicator, "trainersFile", "trainers.csv");
        ReflectionTestUtils.setField(storageHealthIndicator, "trainingsFile", "trainings.csv");

        Health health = storageHealthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    @DisplayName("Health is DOWN when a CSV file is missing")
    void health_MissingFile_ShouldBeDown() {
        ReflectionTestUtils.setField(storageHealthIndicator, "traineesFile", "does-not-exist.csv");
        ReflectionTestUtils.setField(storageHealthIndicator, "trainersFile", "trainers.csv");
        ReflectionTestUtils.setField(storageHealthIndicator, "trainingsFile", "trainings.csv");

        Health health = storageHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}