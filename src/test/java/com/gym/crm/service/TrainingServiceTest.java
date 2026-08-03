package com.gym.crm.service;

import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.TrainingTypeName;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;

    private MeterRegistry meterRegistry;
    private TrainingService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new TrainingService(trainingDao, meterRegistry);
    }

    @Test
    @DisplayName("create saves the training")
    void create_savesTraining() {
        Training training = new Training(new Trainee(), new Trainer(), "Cardio",
                new TrainingType(TrainingTypeName.CARDIO), LocalDate.now(), 60);
        when(trainingDao.save(training)).thenAnswer(inv -> inv.getArgument(0));

        Training result = service.create(training);

        assertEquals("Cardio", result.getTrainingName());
        verify(trainingDao).save(training);
    }

    @Test
    @DisplayName("create increments the training creation counter metric")
    void create_incrementsCounter() {
        Training training = new Training(new Trainee(), new Trainer(), "Cardio",
                new TrainingType(TrainingTypeName.CARDIO), LocalDate.now(), 60);
        when(trainingDao.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(training);

        double count = meterRegistry.get("gym.training.created").counter().count();
        assertEquals(1.0, count);
    }

    @Test
    @DisplayName("select returns training by id")
    void select_returnsTraining() {
        Training training = new Training();
        training.setId(1L);
        when(trainingDao.findById(1L)).thenReturn(Optional.of(training));

        Optional<Training> result = service.select(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("selectAll returns all trainings")
    void selectAll_returnsAll() {
        when(trainingDao.findAll()).thenReturn(List.of(new Training(), new Training()));

        List<Training> result = service.selectAll();

        assertEquals(2, result.size());
    }
}