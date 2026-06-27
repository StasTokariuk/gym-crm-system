package com.gym.crm.storage;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryStorageTest {

    @Test
    @DisplayName("Storage exposes the injected maps")
    void storage_exposesInjectedMaps() {
        Map<Long, Trainee> traineeMap = new HashMap<>();
        Map<Long, Trainer> trainerMap = new HashMap<>();
        Map<Long, Training> trainingMap = new HashMap<>();

        InMemoryStorage storage = new InMemoryStorage(traineeMap, trainerMap, trainingMap);

        assertSame(traineeMap, storage.getTraineeStorage());
        assertSame(trainerMap, storage.getTrainerStorage());
        assertSame(trainingMap, storage.getTrainingStorage());
    }

    @Test
    @DisplayName("Each namespace is independent")
    void storage_namespacesAreIndependent() {
        InMemoryStorage storage = new InMemoryStorage(new HashMap<>(), new HashMap<>(), new HashMap<>());

        storage.getTraineeStorage().put(1L, new Trainee());

        assertEquals(1, storage.getTraineeStorage().size());
        assertEquals(0, storage.getTrainerStorage().size());
        assertEquals(0, storage.getTrainingStorage().size());
    }
}