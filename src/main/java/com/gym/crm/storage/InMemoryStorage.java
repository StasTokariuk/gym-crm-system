package com.gym.crm.storage;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Component
public class InMemoryStorage {

    private static final Logger log = LoggerFactory.getLogger(InMemoryStorage.class);

    private final Map<Long, Trainee> traineeStorage;
    private final Map<Long, Trainer> trainerStorage;
    private final Map<Long, Training> trainingStorage;

    public InMemoryStorage(@Qualifier("traineeStorage") Map<Long, Trainee> traineeStorage,
                           @Qualifier("trainerStorage") Map<Long, Trainer> trainerStorage,
                           @Qualifier("trainingStorage") Map<Long, Training> trainingStorage) {
        this.traineeStorage = traineeStorage;
        this.trainerStorage = trainerStorage;
        this.trainingStorage = trainingStorage;
        log.info("InMemoryStorage initialized with separate namespace maps");
    }

}