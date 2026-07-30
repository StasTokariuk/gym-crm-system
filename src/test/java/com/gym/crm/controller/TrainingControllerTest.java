package com.gym.crm.controller;

import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GymFacade gymFacade;

    @InjectMocks
    private TrainingController trainingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/trainings/trainee - Should return list of trainee trainings when authorized")
    void getTraineeTrainings_ShouldReturnList() throws Exception {
        Trainee trainee = new Trainee(new User("John", "Doe"), null, null);

        TrainingType spec = new TrainingType(TrainingTypeName.FITNESS);
        Trainer trainer = new Trainer(new User("Mike", "Brown"), spec);

        Training training = new Training(trainee, trainer, "Morning Cardio", spec, LocalDate.of(2026, 7, 29), 60);

        when(gymFacade.getTraineeTrainings(eq("John.Doe"), any(), any(), any(), any()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainings/trainee")
                        .param("username", "John.Doe")
                        .param("periodFrom", "2026-07-01")
                        .param("periodTo", "2026-07-31")
                        .param("trainerName", "Mike")
                        .param("trainingType", "FITNESS")
                        .requestAttr("authenticatedUser", "John.Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Cardio"))
                .andExpect(jsonPath("$[0].trainingType").value("FITNESS"))
                .andExpect(jsonPath("$[0].trainingDuration").value(60))
                .andExpect(jsonPath("$[0].trainerName").value("Mike"));
    }

    @Test
    @DisplayName("GET /api/trainings/trainee - Should return 403 Forbidden when unauthorized")
    void getTraineeTrainings_Forbidden_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/trainings/trainee")
                        .param("username", "John.Doe")
                        .requestAttr("authenticatedUser", "Other.User"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/trainings/trainer - Should return list of trainer trainings when authorized")
    void getTrainerTrainings_ShouldReturnList() throws Exception {
        Trainee trainee = new Trainee(new User("John", "Doe"), null, null);

        TrainingType spec = new TrainingType(TrainingTypeName.YOGA);
        Trainer trainer = new Trainer(new User("Mike", "Brown"), spec);

        Training training = new Training(trainee, trainer, "Evening Yoga", spec, LocalDate.of(2026, 7, 29), 45);

        when(gymFacade.getTrainerTrainings(eq("Mike.Brown"), any(), any(), any()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainings/trainer")
                        .param("username", "Mike.Brown")
                        .param("traineeName", "John")
                        .requestAttr("authenticatedUser", "Mike.Brown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Evening Yoga"))
                .andExpect(jsonPath("$[0].trainingType").value("YOGA"))
                .andExpect(jsonPath("$[0].trainingDuration").value(45))
                .andExpect(jsonPath("$[0].traineeName").value("John"));
    }

    @Test
    @DisplayName("GET /api/trainings/trainer - Should return 403 Forbidden when unauthorized")
    void getTrainerTrainings_Forbidden_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/trainings/trainer")
                        .param("username", "Mike.Brown")
                        .requestAttr("authenticatedUser", "Other.User"))
                .andExpect(status().isForbidden());
    }
}