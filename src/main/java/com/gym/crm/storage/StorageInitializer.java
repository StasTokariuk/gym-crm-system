package com.gym.crm.storage;

import com.gym.crm.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Component
public class StorageInitializer implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    @Value("${storage.trainees.file}")
    private String traineesFile;

    @Value("${storage.trainers.file}")
    private String trainersFile;

    @Value("${storage.trainings.file}")
    private String trainingsFile;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof InMemoryStorage) {
            InMemoryStorage storage = (InMemoryStorage) bean;
            log.info("Post-processing InMemoryStorage bean: loading initial data from files...");
            loadTrainees(storage, traineesFile);
            loadTrainers(storage, trainersFile);
            loadTrainings(storage, trainingsFile);
            log.info("Initial data loaded. Trainees={}, Trainers={}, Trainings={}",
                    storage.getTraineeStorage().size(),
                    storage.getTrainerStorage().size(),
                    storage.getTrainingStorage().size());
        }
        return bean;
    }

    // CSV: id,firstName,lastName,username,password,isActive,dateOfBirth,address
    private void loadTrainees(InMemoryStorage storage, String file) {
        readLines(file).forEach(line -> {
            String[] p = line.split(",");
            Trainee t = new Trainee();
            t.setTraineeId(Long.parseLong(p[0]));
            t.setUserId(Long.parseLong(p[0]));
            t.setFirstName(p[1]);
            t.setLastName(p[2]);
            t.setUsername(p[3]);
            t.setPassword(p[4]);
            t.setActive(Boolean.parseBoolean(p[5]));
            t.setDateOfBirth(LocalDate.parse(p[6]));
            t.setAddress(p[7]);
            storage.getTraineeStorage().put(t.getTraineeId(), t);
        });
    }

    // CSV: id,firstName,lastName,username,password,isActive,specialization
    private void loadTrainers(InMemoryStorage storage, String file) {
        readLines(file).forEach(line -> {
            String[] p = line.split(",");
            Trainer t = new Trainer();
            t.setTrainerId(Long.parseLong(p[0]));
            t.setUserId(Long.parseLong(p[0]));
            t.setFirstName(p[1]);
            t.setLastName(p[2]);
            t.setUsername(p[3]);
            t.setPassword(p[4]);
            t.setActive(Boolean.parseBoolean(p[5]));
            t.setSpecialization(TrainingTypeName.valueOf(p[6]));
            storage.getTrainerStorage().put(t.getTrainerId(), t);
        });
    }

    // CSV: id,traineeId,trainerId,trainingName,trainingType,trainingDate,duration
    private void loadTrainings(InMemoryStorage storage, String file) {
        readLines(file).forEach(line -> {
            String[] p = line.split(",");
            Training t = new Training();
            t.setTrainingId(Long.parseLong(p[0]));
            t.setTraineeId(Long.parseLong(p[1]));
            t.setTrainerId(Long.parseLong(p[2]));
            t.setTrainingName(p[3]);
            t.setTrainingType(TrainingTypeName.valueOf(p[4]));
            t.setTrainingDate(LocalDate.parse(p[5]));
            t.setTrainingDuration(Integer.parseInt(p[6]));
            storage.getTrainingStorage().put(t.getTrainingId(), t);
        });
    }

    private java.util.List<String> readLines(String file) {
        java.util.List<String> result = new java.util.ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(file).getInputStream(), StandardCharsets.UTF_8))) {
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
            log.error("Failed to read file {}: {}", file, e.getMessage());
        }
        return result;
    }
}