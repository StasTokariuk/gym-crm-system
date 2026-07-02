package com.gym.crm.storage;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Component
public class StorageInitializer {

    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    // Інжектуємо файли одразу як Spring Resource з classpath
    @Value("classpath:${storage.trainees.file}")
    private Resource traineesResource;

    @Value("classpath:${storage.trainers.file}")
    private Resource trainersResource;

    @Value("classpath:${storage.trainings.file}")
    private Resource trainingsResource;

    private InMemoryStorage storage;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingDao trainingDao;

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @PostConstruct
    public void initializeData() {
        log.info("Loading initial data from CSV files...");
        loadTrainees(traineesResource);
        loadTrainers(trainersResource);
        loadTrainings(trainingsResource);

        // Ініціалізація генераторів ID після завантаження всіх даних
        traineeDao.initializeIdGenerator();
        trainerDao.initializeIdGenerator();
        trainingDao.initializeIdGenerator();

        log.info("Initial data loaded. Trainees={}, Trainers={}, Trainings={}",
                storage.getTraineeStorage().size(),
                storage.getTrainerStorage().size(),
                storage.getTrainingStorage().size());
    }

    // CSV: id,firstName,lastName,username,password,isActive,dateOfBirth,address
    private void loadTrainees(Resource resource) {
        try {
            List<String> lines = readLines(resource);
            lines.forEach(line -> {
                try {
                    String[] p = line.split(",");
                    if (p.length < 8) {
                        log.error("Invalid trainee record (insufficient fields): {}", line);
                        return;
                    }
                    Trainee t = new Trainee();
                    t.setTraineeId(Long.parseLong(p[0]));
                    t.setFirstName(p[1]);
                    t.setLastName(p[2]);
                    t.setUsername(p[3]);
                    t.setPassword(p[4]);
                    t.setActive(Boolean.parseBoolean(p[5]));
                    t.setDateOfBirth(LocalDate.parse(p[6]));
                    t.setAddress(p[7]);
                    storage.getTraineeStorage().put(t.getTraineeId(), t);
                } catch (Exception e) {
                    log.error("Failed to parse trainee record: {}", line, e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to load trainees from {}: {}", resource.getFilename(), e.getMessage());
        }
    }

    // CSV: id,firstName,lastName,username,password,isActive,specialization
    private void loadTrainers(Resource resource) {
        try {
            List<String> lines = readLines(resource);
            lines.forEach(line -> {
                try {
                    String[] p = line.split(",");
                    if (p.length < 7) {
                        log.error("Invalid trainer record (insufficient fields): {}", line);
                        return;
                    }
                    Trainer t = new Trainer();
                    t.setTrainerId(Long.parseLong(p[0]));
                    t.setFirstName(p[1]);
                    t.setLastName(p[2]);
                    t.setUsername(p[3]);
                    t.setPassword(p[4]);
                    t.setActive(Boolean.parseBoolean(p[5]));
                    t.setSpecialization(TrainingTypeName.valueOf(p[6]));
                    storage.getTrainerStorage().put(t.getTrainerId(), t);
                } catch (Exception e) {
                    log.error("Failed to parse trainer record: {}", line, e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to load trainers from {}: {}", resource.getFilename(), e.getMessage());
        }
    }

    // CSV: id,traineeId,trainerId,trainingName,trainingType,trainingDate,duration
    private void loadTrainings(Resource resource) {
        try {
            List<String> lines = readLines(resource);
            lines.forEach(line -> {
                try {
                    String[] p = line.split(",");
                    if (p.length < 7) {
                        log.error("Invalid training record (insufficient fields, expected 7 but got {}): {}", p.length, line);
                        return;
                    }
                    Training t = new Training();
                    t.setTrainingId(Long.parseLong(p[0]));
                    t.setTraineeId(Long.parseLong(p[1]));
                    t.setTrainerId(Long.parseLong(p[2]));
                    t.setTrainingName(p[3]);
                    t.setTrainingType(TrainingTypeName.valueOf(p[4]));
                    t.setTrainingDate(LocalDate.parse(p[5]));
                    t.setTrainingDuration(Integer.parseInt(p[6]));
                    storage.getTrainingStorage().put(t.getTrainingId(), t);
                } catch (Exception e) {
                    log.error("Failed to parse training record: {}", line, e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to load trainings from {}: {}", resource.getFilename(), e.getMessage());
        }
    }

    // Оновлений метод для безпосереднього читання з об'єкта Resource
    private List<String> readLines(Resource resource) {
        List<String> result = new java.util.ArrayList<>();
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
            String msg = String.format("Failed to read required data file '%s': %s", resource.getFilename(), e.getMessage());
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        return result;
    }
}