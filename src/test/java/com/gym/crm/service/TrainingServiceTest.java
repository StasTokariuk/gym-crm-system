package com.gym.crm.service;

import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingTypeName;
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
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;

    @InjectMocks
    private TrainingService service;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("create saves the training")
    void create_savesTraining() {
        Training training = new Training(1L, 2L, "Cardio",
                TrainingTypeName.CARDIO, LocalDate.now(), 60);
        when(trainingDao.save(training)).thenAnswer(inv -> inv.getArgument(0));

        Training result = service.create(training);

        assertEquals("Cardio", result.getTrainingName());
        verify(trainingDao).save(training);
    }

    @Test
    @DisplayName("select returns training by id")
    void select_returnsTraining() {
        Training training = new Training();
        training.setTrainingId(1L);
        when(trainingDao.findById(1L)).thenReturn(Optional.of(training));

        Optional<Training> result = service.select(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getTrainingId());
    }

    @Test
    @DisplayName("select returns empty Optional when not found")
    void select_returnsEmptyWhenNotFound() {
        when(trainingDao.findById(404L)).thenReturn(Optional.empty());

        Optional<Training> result = service.select(404L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("selectAll returns all trainings")
    void selectAll_returnsAll() {
        when(trainingDao.findAll()).thenReturn(List.of(new Training(), new Training()));

        List<Training> result = service.selectAll();

        assertEquals(2, result.size());
    }
}