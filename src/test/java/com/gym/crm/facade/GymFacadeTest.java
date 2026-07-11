package com.gym.crm.facade;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.TrainingTypeName;
import com.gym.crm.model.User;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
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
class GymFacadeTest {

    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;

    private GymFacade facade;

    @BeforeEach
    void setUp() {
        facade = new GymFacade(traineeService, trainerService, trainingService);
    }

    @Test
    @DisplayName("createTrainee delegates to TraineeService")
    void createTrainee_delegates() {
        Trainee t = new Trainee(new User("John", "Smith"), LocalDate.now(), "Kyiv");
        when(traineeService.create(t)).thenReturn(t);

        Trainee result = facade.createTrainee(t);

        assertSame(t, result);
        verify(traineeService).create(t);
    }

    @Test
    @DisplayName("updateTrainee delegates to TraineeService")
    void updateTrainee_delegates() {
        Trainee t = new Trainee();
        when(traineeService.update(t)).thenReturn(t);

        facade.updateTrainee(t);

        verify(traineeService).update(t);
    }

    @Test
    @DisplayName("deleteTrainee delegates to TraineeService")
    void deleteTrainee_delegates() {
        facade.deleteTrainee(1L);
        verify(traineeService).delete(1L);
    }

    @Test
    @DisplayName("getTrainee delegates to TraineeService")
    void getTrainee_delegates() {
        when(traineeService.select(1L)).thenReturn(Optional.of(new Trainee()));

        Optional<Trainee> result = facade.getTrainee(1L);

        assertTrue(result.isPresent());
        verify(traineeService).select(1L);
    }

    @Test
    @DisplayName("getAllTrainees delegates to TraineeService")
    void getAllTrainees_delegates() {
        when(traineeService.selectAll()).thenReturn(List.of(new Trainee()));

        List<Trainee> result = facade.getAllTrainees();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("createTrainer delegates to TrainerService")
    void createTrainer_delegates() {
        Trainer t = new Trainer(new User("Mike", "Brown"), new TrainingType(TrainingTypeName.FITNESS));
        when(trainerService.create(t)).thenReturn(t);

        Trainer result = facade.createTrainer(t);

        assertSame(t, result);
        verify(trainerService).create(t);
    }

    @Test
    @DisplayName("updateTrainer delegates to TrainerService")
    void updateTrainer_delegates() {
        Trainer t = new Trainer();
        when(trainerService.update(t)).thenReturn(t);

        facade.updateTrainer(t);

        verify(trainerService).update(t);
    }

    @Test
    @DisplayName("getTrainer delegates to TrainerService")
    void getTrainer_delegates() {
        when(trainerService.select(1L)).thenReturn(Optional.of(new Trainer()));

        facade.getTrainer(1L);

        verify(trainerService).select(1L);
    }

    @Test
    @DisplayName("getAllTrainers delegates to TrainerService")
    void getAllTrainers_delegates() {
        when(trainerService.selectAll()).thenReturn(List.of(new Trainer()));

        List<Trainer> result = facade.getAllTrainers();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("createTraining delegates to TrainingService")
    void createTraining_delegates() {
        Training t = new Training(new Trainee(), new Trainer(), "Cardio",
                new TrainingType(TrainingTypeName.CARDIO), LocalDate.now(), 60);
        when(trainingService.create(t)).thenReturn(t);

        Training result = facade.createTraining(t);

        assertSame(t, result);
        verify(trainingService).create(t);
    }

    @Test
    @DisplayName("getTraining delegates to TrainingService")
    void getTraining_delegates() {
        when(trainingService.select(1L)).thenReturn(Optional.of(new Training()));

        facade.getTraining(1L);

        verify(trainingService).select(1L);
    }

    @Test
    @DisplayName("getAllTrainings delegates to TrainingService")
    void getAllTrainings_delegates() {
        when(trainingService.selectAll()).thenReturn(List.of(new Training()));

        List<Training> result = facade.getAllTrainings();

        assertEquals(1, result.size());
    }
}