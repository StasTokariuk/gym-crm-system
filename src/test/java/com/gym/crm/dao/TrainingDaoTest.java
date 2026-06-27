package com.gym.crm.dao;

import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingTypeName;
import com.gym.crm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDaoTest {

    private TrainingDao dao;
    private Map<Long, Training> trainingMap;

    @BeforeEach
    void setUp() {
        trainingMap = new HashMap<>();
        InMemoryStorage storage = new InMemoryStorage(new HashMap<>(), new HashMap<>(), trainingMap);
        dao = new TrainingDao();
        dao.setStorage(storage);
    }

    @Test
    @DisplayName("save generates id when it is null")
    void save_generatesId() {
        Training t = new Training(1L, 2L, "Cardio",
                TrainingTypeName.CARDIO, LocalDate.now(), 60);

        Training saved = dao.save(t);

        assertNotNull(saved.getTrainingId());
        assertTrue(trainingMap.containsKey(saved.getTrainingId()));
    }

    @Test
    @DisplayName("findById returns the stored training")
    void findById_returnsTraining() {
        Training t = new Training(1L, 2L, "Yoga",
                TrainingTypeName.YOGA, LocalDate.now(), 45);
        dao.save(t);

        Optional<Training> found = dao.findById(t.getTrainingId());

        assertTrue(found.isPresent());
        assertEquals("Yoga", found.get().getTrainingName());
    }

    @Test
    @DisplayName("findById returns empty Optional when missing")
    void findById_returnsEmpty() {
        assertTrue(dao.findById(999L).isEmpty());
    }

    @Test
    @DisplayName("findAll returns all stored trainings")
    void findAll_returnsAll() {
        dao.save(new Training(1L, 1L, "T1", TrainingTypeName.FITNESS, LocalDate.now(), 30));
        dao.save(new Training(2L, 2L, "T2", TrainingTypeName.CARDIO, LocalDate.now(), 40));

        List<Training> all = dao.findAll();

        assertEquals(2, all.size());
    }
}