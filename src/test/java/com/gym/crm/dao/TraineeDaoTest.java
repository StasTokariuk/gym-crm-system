package com.gym.crm.dao;

import com.gym.crm.model.Trainee;
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

class TraineeDaoTest {

    private TraineeDao dao;
    private Map<Long, Trainee> traineeMap;

    @BeforeEach
    void setUp() {
        traineeMap = new HashMap<>();
        InMemoryStorage storage = new InMemoryStorage(traineeMap, new HashMap<>(), new HashMap<>());
        dao = new TraineeDao();
        dao.setStorage(storage);
    }

    @Test
    @DisplayName("save generates id when it is null")
    void save_generatesId() {
        Trainee t = new Trainee("John", "Smith", LocalDate.of(2000, 1, 1), "Kyiv");

        Trainee saved = dao.save(t);

        assertNotNull(saved.getTraineeId());
        assertTrue(traineeMap.containsKey(saved.getTraineeId()));
    }

    @Test
    @DisplayName("save keeps existing id on update")
    void save_keepsExistingId() {
        Trainee t = new Trainee();
        t.setTraineeId(10L);

        Trainee saved = dao.save(t);

        assertEquals(10L, saved.getTraineeId());
    }

    @Test
    @DisplayName("findById returns the stored trainee")
    void findById_returnsTrainee() {
        Trainee t = new Trainee("John", "Smith", null, null);
        dao.save(t);

        Optional<Trainee> found = dao.findById(t.getTraineeId());

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    @DisplayName("findById returns empty Optional when missing")
    void findById_returnsEmpty() {
        assertTrue(dao.findById(999L).isEmpty());
    }

    @Test
    @DisplayName("findByUsername returns the matching trainee")
    void findByUsername_returnsTrainee() {
        Trainee t = new Trainee("John", "Smith", null, null);
        t.setUsername("John.Smith");
        dao.save(t);

        Optional<Trainee> found = dao.findByUsername("John.Smith");

        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("findAll returns all stored trainees")
    void findAll_returnsAll() {
        dao.save(new Trainee("A", "B", null, null));
        dao.save(new Trainee("C", "D", null, null));

        List<Trainee> all = dao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("deleteById removes the trainee")
    void deleteById_removesTrainee() {
        Trainee t = new Trainee("John", "Smith", null, null);
        dao.save(t);

        dao.deleteById(t.getTraineeId());

        assertTrue(dao.findById(t.getTraineeId()).isEmpty());
    }
}