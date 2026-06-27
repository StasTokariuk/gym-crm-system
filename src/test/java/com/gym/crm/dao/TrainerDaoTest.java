package com.gym.crm.dao;

import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingTypeName;
import com.gym.crm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDaoTest {

    private TrainerDao dao;
    private Map<Long, Trainer> trainerMap;

    @BeforeEach
    void setUp() {
        trainerMap = new HashMap<>();
        InMemoryStorage storage = new InMemoryStorage(new HashMap<>(), trainerMap, new HashMap<>());
        dao = new TrainerDao();
        dao.setStorage(storage);
    }

    @Test
    @DisplayName("save generates id when it is null")
    void save_generatesId() {
        Trainer t = new Trainer("Mike", "Brown", TrainingTypeName.FITNESS);

        Trainer saved = dao.save(t);

        assertNotNull(saved.getTrainerId());
        assertTrue(trainerMap.containsKey(saved.getTrainerId()));
    }

    @Test
    @DisplayName("findById returns the stored trainer")
    void findById_returnsTrainer() {
        Trainer t = new Trainer("Mike", "Brown", TrainingTypeName.YOGA);
        dao.save(t);

        Optional<Trainer> found = dao.findById(t.getTrainerId());

        assertTrue(found.isPresent());
        assertEquals(TrainingTypeName.YOGA, found.get().getSpecialization());
    }

    @Test
    @DisplayName("findByUsername returns the matching trainer")
    void findByUsername_returnsTrainer() {
        Trainer t = new Trainer("Mike", "Brown", TrainingTypeName.FITNESS);
        t.setUsername("Mike.Brown");
        dao.save(t);

        Optional<Trainer> found = dao.findByUsername("Mike.Brown");

        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("findAll returns all stored trainers")
    void findAll_returnsAll() {
        dao.save(new Trainer("A", "B", TrainingTypeName.CARDIO));
        dao.save(new Trainer("C", "D", TrainingTypeName.ZUMBA));

        List<Trainer> all = dao.findAll();

        assertEquals(2, all.size());
    }
}