package com.gym.crm.dao;

import com.gym.crm.model.Trainee;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeDaoTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Trainee> query;

    @InjectMocks
    private TraineeDao dao;

    @BeforeEach
    void setUp() {
        // Дозволяє методу getCurrentSession() повертати наш мок сесії
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    @DisplayName("save calls saveOrUpdate on session")
    void save_callsHibernateSaveOrUpdate() {
        Trainee t = new Trainee(new User("John", "Smith"), LocalDate.of(2000, 1, 1), "Kyiv");

        Trainee saved = dao.save(t);

        verify(session).saveOrUpdate(t);
        assertEquals(t, saved);
    }

    @Test
    @DisplayName("findById returns empty Optional when missing")
    void findById_returnsEmpty() {
        when(session.get(Trainee.class, 999L)).thenReturn(null);

        Optional<Trainee> found = dao.findById(999L);

        assertTrue(found.isEmpty());
        verify(session).get(Trainee.class, 999L);
    }

    @Test
    @DisplayName("findById returns the trainee when found")
    void findById_returnsTrainee() {
        Trainee t = new Trainee();
        t.setId(1L);
        when(session.get(Trainee.class, 1L)).thenReturn(t);

        Optional<Trainee> found = dao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
    }

    @Test
    @DisplayName("findByUsername executes correct HQL query")
    void findByUsername_executesHQL() {
        String username = "John.Smith";
        Trainee t = new Trainee();

        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter("username", username)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(t));

        Optional<Trainee> found = dao.findByUsername(username);

        assertTrue(found.isPresent());
        assertEquals(t, found.get());
        verify(session).createQuery("SELECT t FROM Trainee t JOIN t.user u WHERE u.username = :username", Trainee.class);
    }

    @Test
    @DisplayName("findAll returns list of trainees")
    void findAll_returnsList() {
        List<Trainee> list = List.of(new Trainee(), new Trainee());
        when(session.createQuery("from com.gym.crm.model.Trainee", Trainee.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(list);

        List<Trainee> result = dao.findAll();

        assertEquals(2, result.size());
        verify(session).createQuery("from com.gym.crm.model.Trainee", Trainee.class);
    }

    @Test
    @DisplayName("deleteById deletes trainee when found")
    void deleteById_deletesTrainee() {
        Trainee t = new Trainee();
        t.setId(1L);
        when(session.get(Trainee.class, 1L)).thenReturn(t);

        dao.deleteById(1L);

        verify(session).delete(t);
    }
}