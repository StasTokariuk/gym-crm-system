package com.gym.crm.dao;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.TrainingTypeName;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingDaoTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Training> query;

    @InjectMocks
    private TrainingDao dao;

    @BeforeEach
    void setUp() {
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    @DisplayName("save calls saveOrUpdate on session")
    void save_callsHibernateSaveOrUpdate() {
        Training t = new Training(new Trainee(), new Trainer(), "Cardio",
                new TrainingType(TrainingTypeName.CARDIO), LocalDate.now(), 60);

        Training saved = dao.save(t);

        verify(session).saveOrUpdate(t);
        assertEquals(t, saved);
    }

    @Test
    @DisplayName("findById returns the stored training")
    void findById_returnsTraining() {
        Training t = new Training(new Trainee(), new Trainer(), "Yoga",
                new TrainingType(TrainingTypeName.YOGA), LocalDate.now(), 45);
        t.setId(1L);
        when(session.get(Training.class, 1L)).thenReturn(t);

        Optional<Training> found = dao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Yoga", found.get().getTrainingName());
    }

    @Test
    @DisplayName("findById returns empty Optional when missing")
    void findById_returnsEmpty() {
        when(session.get(Training.class, 999L)).thenReturn(null);
        assertTrue(dao.findById(999L).isEmpty());
    }

    @Test
    @DisplayName("findAll returns all stored trainings")
    void findAll_returnsAll() {
        List<Training> list = List.of(new Training(), new Training());
        when(session.createQuery("from com.gym.crm.model.Training", Training.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(list);

        List<Training> result = dao.findAll();

        assertEquals(2, result.size());
    }
}