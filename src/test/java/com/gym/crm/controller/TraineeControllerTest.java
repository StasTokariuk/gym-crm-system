package com.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.dto.request.PasswordChangeRequest;
import com.gym.crm.dto.request.TraineeRegistrationRequest;
import com.gym.crm.dto.request.TraineeUpdateRequest;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.Trainee;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GymFacade gymFacade;

    @InjectMocks
    private TraineeController traineeController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(traineeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/trainees - Should register trainee and return 201 Created")
    void registerTrainee_ShouldReturn201() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setAddress("Kyiv");

        User user = new User("John", "Doe");
        user.setUsername("John.Doe");
        user.setPassword("rawPassword");
        Trainee savedTrainee = new Trainee(user, request.getDateOfBirth(), request.getAddress());

        when(gymFacade.createTrainee(any(Trainee.class))).thenReturn(savedTrainee);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("John.Doe"))
                .andExpect(jsonPath("$.password").value("rawPassword"));

        verify(gymFacade, times(1)).createTrainee(any(Trainee.class));
    }

    @Test
    @DisplayName("POST /api/trainees - Should return 400 Bad Request when validation fails")
    void registerTrainee_ValidationFailure_ShouldReturn400() throws Exception {
        TraineeRegistrationRequest invalidRequest = new TraineeRegistrationRequest();
        invalidRequest.setFirstName("");
        invalidRequest.setLastName("Doe");

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(gymFacade, never()).createTrainee(any(Trainee.class));
    }

    @Test
    @DisplayName("GET /api/trainees/{username} - Should return profile details when authorized")
    void getTraineeProfile_ShouldReturnProfile() throws Exception {
        User user = new User("John", "Doe");
        user.setUsername("John.Doe");
        user.setActive(true);
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Kyiv");
        trainee.setTrainers(Collections.emptySet());

        when(gymFacade.getTraineeByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        mockMvc.perform(get("/api/trainees/John.Doe")
                        .requestAttr("authenticatedUser", "John.Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.address").value("Kyiv"));
    }

    @Test
    @DisplayName("GET /api/trainees/{username} - Should return 403 Forbidden when unauthorized")
    void getTraineeProfile_Forbidden_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/trainees/John.Doe")
                        .requestAttr("authenticatedUser", "Wrong.User"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/trainees/{username} - Should return 404 when not found")
    void getTraineeProfile_NotFound_ShouldReturn404() throws Exception {
        when(gymFacade.getTraineeByUsername("Unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trainees/Unknown")
                        .requestAttr("authenticatedUser", "Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("PUT /api/trainees/{username} - Should update trainee and return updated details")
    void updateTrainee_ShouldReturnUpdatedProfile() throws Exception {
        TraineeUpdateRequest request = new TraineeUpdateRequest();
        request.setFirstName("JohnNew");
        request.setLastName("DoeNew");
        request.setIsActive(false);

        User existingUser = new User("John", "Doe");
        existingUser.setUsername("John.Doe");
        Trainee existingTrainee = new Trainee(existingUser, null, null);
        existingTrainee.setTrainers(Collections.emptySet());

        when(gymFacade.getTraineeByUsername("John.Doe")).thenReturn(Optional.of(existingTrainee));
        when(gymFacade.updateTrainee(any(Trainee.class))).thenReturn(existingTrainee);

        mockMvc.perform(put("/api/trainees/John.Doe")
                        .requestAttr("authenticatedUser", "John.Doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("JohnNew"))
                .andExpect(jsonPath("$.lastName").value("DoeNew"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("DELETE /api/trainees/{username} - Should delete trainee and return 200 OK")
    void deleteTrainee_ShouldReturn200() throws Exception {
        Trainee trainee = new Trainee();
        when(gymFacade.getTraineeByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        mockMvc.perform(delete("/api/trainees/John.Doe")
                        .requestAttr("authenticatedUser", "John.Doe"))
                .andExpect(status().isOk());

        verify(gymFacade, times(1)).deleteTraineeByUsername("John.Doe");
    }

    @Test
    @DisplayName("PUT /api/trainees/{username}/password - Should change password successfully")
    void changePassword_ShouldReturn200() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setUsername("John.Doe");
        request.setOldPassword("oldSecret123");
        request.setNewPassword("newSecret123");

        mockMvc.perform(put("/api/trainees/John.Doe/password")
                        .requestAttr("authenticatedUser", "John.Doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade, times(1)).changeTraineePassword(eq("John.Doe"), eq("newSecret123"));
    }

    @Test
    @DisplayName("PUT /api/trainees/{username}/status - Should update status successfully")
    void updateStatus_ShouldReturn200() throws Exception {
        mockMvc.perform(put("/api/trainees/John.Doe/status")
                        .param("isActive", "false")
                        .requestAttr("authenticatedUser", "John.Doe"))
                .andExpect(status().isOk());

        verify(gymFacade, times(1)).updateTraineeStatus(eq("John.Doe"), eq(false));
    }

    @Test
    @DisplayName("PUT /api/trainees/{username}/trainers - Should update trainers list successfully")
    void updateTrainersList_ShouldReturnList() throws Exception {
        List<String> trainerUsernames = List.of("Trainer.One");
        User user = new User("John", "Doe");
        user.setUsername("John.Doe");
        Trainee trainee = new Trainee(user, null, null);
        trainee.setTrainers(Collections.emptySet());

        when(gymFacade.getTraineeByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        mockMvc.perform(put("/api/trainees/John.Doe/trainers")
                        .requestAttr("authenticatedUser", "John.Doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainerUsernames)))
                .andExpect(status().isOk());

        verify(gymFacade, times(1)).updateTraineeTrainers(eq("John.Doe"), eq(trainerUsernames));
    }
}