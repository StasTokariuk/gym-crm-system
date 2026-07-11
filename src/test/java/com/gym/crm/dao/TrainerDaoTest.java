package com.gym.crm.dao;

import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.TrainingTypeName;
import com.gym.crm.model.User;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerDaoTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Trainer> query;

    @InjectMocks
    private TrainerDao dao;

    @BeforeEach
    void setUp() {
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    @DisplayName("save calls saveOrUpdate on session")
    void save_callsHibernateSaveOrUpdate() {
        Trainer t = new Trainer(new User("Mike", "Brown"), new TrainingType(TrainingTypeName.FITNESS));

        Trainer saved = dao.save(t);

        verify(session).saveOrUpdate(t);
        assertEquals(t, saved);
    }

    @Test
    @DisplayName("findById returns the stored trainer")
    void findById_returnsTrainer() {
        Trainer t = new Trainer(new User("Mike", "Brown"), new TrainingType(TrainingTypeName.YOGA));
        t.setId(1L);
        when(session.get(Trainer.class, 1L)).thenReturn(t);

        Optional<Trainer> found = dao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals(TrainingTypeName.YOGA, found.get().getSpecialization().getTrainingTypeName());
    }

    @Test
    @DisplayName("findByUsername returns the matching trainer")
    void findByUsername_returnsTrainer() {
        String username = "Mike.Brown";
        Trainer t = new Trainer();

        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter("username", username)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(t));

        Optional<Trainer> found = dao.findByUsername(username);

        assertTrue(found.isPresent());
        assertEquals(t, found.get());
    }

    @Test
    @DisplayName("findAll returns all stored trainers")
    void findAll_returnsAll() {
        List<Trainer> list = List.of(new Trainer(), new Trainer());
        when(session.createQuery("from com.gym.crm.model.Trainer", Trainer.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(list);

        List<Trainer> result = dao.findAll();

        assertEquals(2, result.size());
    }
}