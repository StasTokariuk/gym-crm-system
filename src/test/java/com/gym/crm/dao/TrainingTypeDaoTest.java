package com.gym.crm.dao;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeDaoTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<TrainingType> query;

    @InjectMocks
    private TrainingTypeDao dao;

    @BeforeEach
    void setUp() {
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    @DisplayName("findByName executes correct HQL query and returns training type")
    void findByName_returnsTrainingType() {
        TrainingTypeName name = TrainingTypeName.FITNESS;
        TrainingType expectedType = new TrainingType(name);

        when(session.createQuery(anyString(), eq(TrainingType.class))).thenReturn(query);
        when(query.setParameter("name", name)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(expectedType));

        Optional<TrainingType> result = dao.findByName(name);

        assertTrue(result.isPresent());
        assertEquals(name, result.get().getTrainingTypeName());
        verify(session).createQuery("SELECT tt FROM TrainingType tt WHERE tt.trainingTypeName = :name", TrainingType.class);
    }

    @Test
    @DisplayName("findByName returns empty Optional when not found")
    void findByName_returnsEmpty() {
        TrainingTypeName name = TrainingTypeName.YOGA;

        when(session.createQuery(anyString(), eq(TrainingType.class))).thenReturn(query);
        when(query.setParameter("name", name)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        Optional<TrainingType> result = dao.findByName(name);

        assertTrue(result.isEmpty());
    }
}