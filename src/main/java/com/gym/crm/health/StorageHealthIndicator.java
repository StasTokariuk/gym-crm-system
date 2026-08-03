package com.gym.crm.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class StorageHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(StorageHealthIndicator.class);

    @Value("${storage.trainees.file}")
    private String traineesFile;

    @Value("${storage.trainers.file}")
    private String trainersFile;

    @Value("${storage.trainings.file}")
    private String trainingsFile;

    @Override
    public Health health() {
        boolean traineesExist = resourceExists(traineesFile);
        boolean trainersExist = resourceExists(trainersFile);
        boolean trainingsExist = resourceExists(trainingsFile);

        boolean allPresent = traineesExist && trainersExist && trainingsExist;

        Health.Builder builder = allPresent ? Health.up() : Health.down();

        builder.withDetail(traineesFile, traineesExist ? "available" : "missing")
                .withDetail(trainersFile, trainersExist ? "available" : "missing")
                .withDetail(trainingsFile, trainingsExist ? "available" : "missing");

        if (!allPresent) {
            log.warn("Storage health check failed. Some CSV data files are missing");
        } else {
            log.debug("Storage health check passed. All CSV data files are present");
        }

        return builder.build();
    }

    private boolean resourceExists(String fileName) {
        return new ClassPathResource(fileName).exists();
    }
}