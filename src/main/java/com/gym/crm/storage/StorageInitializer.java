package com.gym.crm.storage;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dao.TrainingDao;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.model.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StorageInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    @Value("classpath:${storage.trainees.file:trainees.csv}")
    private Resource traineesResource;

    @Value("classpath:${storage.trainers.file:trainers.csv}")
    private Resource trainersResource;

    @Value("classpath:${storage.trainings.file:trainings.csv}")
    private Resource trainingsResource;

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;
    private final TrainingTypeDao trainingTypeDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Database initialization and seeding started...");

        initializeTrainingTypes();

        if (traineeDao.findAll().isEmpty() && trainerDao.findAll().isEmpty()) {
            loadTrainees(traineesResource);
            loadTrainers(trainersResource);
            loadTrainings(trainingsResource);
            log.info("Database seeding from CSV completed successfully!");
        } else {
            log.info("Database already contains user data. Skipping CSV seeding.");
        }
    }

    private void initializeTrainingTypes() {
        for (TrainingTypeName typeName : TrainingTypeName.values()) {
            try {
                if (trainingTypeDao.findByName(typeName).isEmpty()) {
                    TrainingType trainingType = new TrainingType(typeName);
                    trainingTypeDao.save(trainingType);
                    log.info("Initialized training type: {}", typeName);
                }
            } catch (Exception e) {
                log.error("Failed to initialize training type {}: {}", typeName, e.getMessage());
                throw new RuntimeException("Critical database initialization error. Failed to save training type: " + typeName, e);
            }
        }
    }

    private void loadTrainees(Resource resource) {
        try {
            List<String> lines = readLines(resource);
            for (String line : lines) {
                String[] p = line.split(",");
                if (p.length < 8) {
                    throw new IllegalArgumentException("Malformed trainee CSV record (insufficient fields): " + line);
                }

                User user = new User(p[1], p[2]);
                user.setUsername(p[3]);
                user.setPassword(passwordEncoder.encode(p[4]));
                user.setActive(Boolean.parseBoolean(p[5]));

                Trainee trainee = new Trainee(user, LocalDate.parse(p[6]), p[7]);
                traineeDao.save(trainee);
                log.debug("Seeded trainee: {}", user.getUsername());
            }
        } catch (Exception e) {
            log.error("Failed to seed trainees: {}", e.getMessage());
            throw new RuntimeException("Rollback transaction: Failed to seed trainees from CSV file.", e);
        }
    }

    private void loadTrainers(Resource resource) {
        try {
            List<String> lines = readLines(resource);
            for (String line : lines) {
                String[] p = line.split(",");
                if (p.length < 7) {
                    throw new IllegalArgumentException("Malformed trainer CSV record (insufficient fields): " + line);
                }

                User user = new User(p[1], p[2]);
                user.setUsername(p[3]);
                user.setPassword(passwordEncoder.encode(p[4]));
                user.setActive(Boolean.parseBoolean(p[5]));

                TrainingTypeName typeName = TrainingTypeName.valueOf(p[6].toUpperCase());
                TrainingType specialization = trainingTypeDao.findByName(typeName)
                        .orElseThrow(() -> new IllegalStateException("Specialization not found: " + typeName));

                Trainer trainer = new Trainer(user, specialization);
                trainerDao.save(trainer);
                log.debug("Seeded trainer: {}", user.getUsername());
            }
        } catch (Exception e) {
            log.error("Failed to seed trainers: {}", e.getMessage());
            throw new RuntimeException("Rollback transaction: Failed to seed trainers from CSV file.", e);
        }
    }

    private void loadTrainings(Resource resource) {
        try {
            List<String> lines = readLines(resource);
            for (String line : lines) {
                String[] p = line.split(",");
                if (p.length < 7) {
                    throw new IllegalArgumentException("Malformed training CSV record (insufficient fields): " + line);
                }

                Long rawTraineeId = Long.parseLong(p[1]);
                Long rawTrainerId = Long.parseLong(p[2]);

                Trainee trainee = traineeDao.findById(rawTraineeId)
                        .orElseThrow(() -> new IllegalStateException("Trainee not found for ID: " + rawTraineeId));
                Trainer trainer = trainerDao.findById(rawTrainerId)
                        .orElseThrow(() -> new IllegalStateException("Trainer not found for ID: " + rawTrainerId));

                String trainingName = p[3];
                TrainingTypeName typeName = TrainingTypeName.valueOf(p[4].toUpperCase());
                TrainingType trainingType = trainingTypeDao.findByName(typeName)
                        .orElseThrow(() -> new IllegalStateException("Training type not found: " + typeName));

                Training training = new Training(
                        trainee,
                        trainer,
                        trainingName,
                        trainingType,
                        LocalDate.parse(p[5]),
                        Integer.parseInt(p[6])
                );
                trainingDao.save(training);
            }
        } catch (Exception e) {
            log.error("Failed to seed trainings: {}", e.getMessage(), e);
            throw new RuntimeException("Rollback transaction: Failed to seed trainings from CSV file.", e);
        }
    }

    private List<String> readLines(Resource resource) {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (!line.trim().isEmpty()) result.add(line.trim());
            }
        } catch (Exception e) {
            log.error("Failed to read resource: {}", resource.getFilename(), e);
            throw new RuntimeException("Critical failure reading CSV file: " + resource.getFilename(), e);
        }
        return result;
    }
}