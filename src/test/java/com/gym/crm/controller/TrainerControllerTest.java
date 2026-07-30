package com.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.dto.request.PasswordChangeRequest;
import com.gym.crm.dto.request.TrainerRegistrationRequest;
import com.gym.crm.dto.request.TrainerUpdateRequest;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.TrainingTypeName;
import com.gym.crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GymFacade gymFacade;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainerController trainerController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/trainers - Should register trainer and return 201 Created")
    void registerTrainer_ShouldReturn201() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Mike");
        request.setLastName("Brown");
        request.setSpecialization("FITNESS");

        TrainingType specialization = new TrainingType(TrainingTypeName.FITNESS);
        User user = new User("Mike", "Brown");
        user.setUsername("Mike.Brown");
        user.setPassword("secretPass");
        Trainer savedTrainer = new Trainer(user, specialization);

        when(trainingTypeDao.findByName(TrainingTypeName.FITNESS)).thenReturn(Optional.of(specialization));
        when(gymFacade.createTrainer(any(Trainer.class))).thenReturn(savedTrainer);

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Mike.Brown"))
                .andExpect(jsonPath("$.password").value("secretPass"));

        verify(gymFacade, times(1)).createTrainer(any(Trainer.class));
    }

    @Test
    @DisplayName("POST /api/trainers - Should return 400 when validation fails")
    void registerTrainer_ValidationFailure_ShouldReturn400() throws Exception {
        TrainerRegistrationRequest invalidRequest = new TrainerRegistrationRequest();
        invalidRequest.setFirstName("");
        invalidRequest.setLastName("Brown");

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/trainers/{username} - Should return trainer profile when authorized")
    void getTrainerProfile_ShouldReturnProfile() throws Exception {
        TrainingType specialization = new TrainingType(TrainingTypeName.YOGA);
        User user = new User("Mike", "Brown");
        user.setUsername("Mike.Brown");
        user.setActive(true);
        Trainer trainer = new Trainer(user, specialization);
        trainer.setTrainees(Collections.emptySet());

        when(gymFacade.getTrainerByUsername("Mike.Brown")).thenReturn(Optional.of(trainer));

        mockMvc.perform(get("/api/trainers/Mike.Brown")
                        .requestAttr("authenticatedUser", "Mike.Brown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Mike"))
                .andExpect(jsonPath("$.lastName").value("Brown"))
                .andExpect(jsonPath("$.specialization").value("YOGA"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("GET /api/trainers/{username} - Should return 403 Forbidden when user is not authorized")
    void getTrainerProfile_Forbidden_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/trainers/Mike.Brown")
                        .requestAttr("authenticatedUser", "Other.User"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/trainers/{username} - Should update trainer profile")
    void updateTrainer_ShouldReturnUpdatedProfile() throws Exception {
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setFirstName("MikeNew");
        request.setLastName("BrownNew");
        request.setIsActive(true);

        TrainingType specialization = new TrainingType(TrainingTypeName.YOGA);
        User user = new User("Mike", "Brown");
        user.setUsername("Mike.Brown");
        Trainer existingTrainer = new Trainer(user, specialization);
        existingTrainer.setTrainees(Collections.emptySet());

        when(gymFacade.getTrainerByUsername("Mike.Brown")).thenReturn(Optional.of(existingTrainer));
        when(gymFacade.updateTrainer(any(Trainer.class))).thenReturn(existingTrainer);

        mockMvc.perform(put("/api/trainers/Mike.Brown")
                        .requestAttr("authenticatedUser", "Mike.Brown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("MikeNew"))
                .andExpect(jsonPath("$.lastName").value("BrownNew"));
    }

    @Test
    @DisplayName("PUT /api/trainers/{username}/password - Should change password successfully")
    void changePassword_ShouldReturn200() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setUsername("Mike.Brown");
        request.setOldPassword("oldSecret123");
        request.setNewPassword("newSecret123");

        mockMvc.perform(put("/api/trainers/Mike.Brown/password")
                        .requestAttr("authenticatedUser", "Mike.Brown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade, times(1)).changeTrainerPassword(eq("Mike.Brown"), eq("newSecret123"));
    }

    @Test
    @DisplayName("PUT /api/trainers/{username}/status - Should update status successfully")
    void updateStatus_ShouldReturn200() throws Exception {
        mockMvc.perform(put("/api/trainers/Mike.Brown/status")
                        .param("isActive", "false")
                        .requestAttr("authenticatedUser", "Mike.Brown"))
                .andExpect(status().isOk());

        verify(gymFacade, times(1)).updateTrainerStatus(eq("Mike.Brown"), eq(false));
    }

    @Test
    @DisplayName("GET /api/trainers/not-assigned/{traineeUsername} - Should return unassigned active trainers")
    void getNotAssignedTrainers_ShouldReturnList() throws Exception {
        TrainingType spec = new TrainingType(TrainingTypeName.ZUMBA);
        User user = new User("Jane", "Doe");
        user.setUsername("Jane.Doe");
        Trainer trainer = new Trainer(user, spec);

        when(gymFacade.getActiveTrainersNotAssignedToTrainee("John.Smith")).thenReturn(List.of(trainer));

        mockMvc.perform(get("/api/trainers/not-assigned/John.Smith")
                        .requestAttr("authenticatedUser", "John.Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Jane.Doe"))
                .andExpect(jsonPath("$[0].specialization").value("ZUMBA"));
    }
}