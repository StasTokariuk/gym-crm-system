package com.gym.crm.controller;

import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.dto.request.PasswordChangeRequest;
import com.gym.crm.dto.request.TrainerRegistrationRequest;
import com.gym.crm.dto.request.TrainerUpdateRequest;
import com.gym.crm.dto.response.TraineeShortInfo;
import com.gym.crm.dto.response.TrainerProfileResponse;
import com.gym.crm.dto.response.TrainerRegistrationResponse;
import com.gym.crm.dto.response.TrainerShortInfo;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.TrainingTypeName;
import com.gym.crm.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainer Management", description = "Endpoints for Trainer profiles and operations")
public class TrainerController {

    private static final Logger log = LoggerFactory.getLogger(TrainerController.class);
    private final GymFacade gymFacade;
    private final TrainingTypeDao trainingTypeDao;

    @PostMapping
    @Operation(summary = "Register a new Trainer profile")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainer registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<TrainerRegistrationResponse> registerTrainer(@Valid @RequestBody TrainerRegistrationRequest request) {
        log.info("REST request to register trainer: {} {}", request.getFirstName(), request.getLastName());

        TrainingTypeName typeName = TrainingTypeName.valueOf(request.getSpecialization().toUpperCase());
        TrainingType specialization = trainingTypeDao.findByName(typeName)
                .orElseThrow(() -> new IllegalArgumentException("Specialization not found: " + request.getSpecialization()));

        User user = new User(request.getFirstName(), request.getLastName());
        Trainer trainer = new Trainer(user, specialization);

        Trainer saved = gymFacade.createTrainer(trainer);

        TrainerRegistrationResponse response = new TrainerRegistrationResponse(
                saved.getUser().getUsername(),
                saved.getUser().getPassword()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get Trainer profile details by username")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(@PathVariable String username,
                                                                    jakarta.servlet.http.HttpServletRequest servletRequest) {
        log.info("REST request to get trainer profile: {}", username);

        String authenticatedUser = (String) servletRequest.getAttribute("authenticatedUser");
        if (!username.equals(authenticatedUser)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Trainer trainer = gymFacade.getTrainerByUsername(username)
                .orElseThrow(() -> new com.gym.crm.exception.ResourceNotFoundException("Trainer not found with username: " + username));

        TrainerProfileResponse response = mapToProfileResponse(trainer);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update an existing Trainer profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainer(
            @PathVariable String username,
            @Valid @RequestBody TrainerUpdateRequest request,
            jakarta.servlet.http.HttpServletRequest servletRequest) {

        log.info("REST request to update trainer: {}", username);

        String authenticatedUser = (String) servletRequest.getAttribute("authenticatedUser");
        if (!username.equals(authenticatedUser)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Trainer existing = gymFacade.getTrainerByUsername(username)
                .orElseThrow(() -> new com.gym.crm.exception.ResourceNotFoundException("Trainer not found: " + username));

        existing.getUser().setFirstName(request.getFirstName());
        existing.getUser().setLastName(request.getLastName());
        existing.getUser().setActive(request.getIsActive());

        Trainer updated = gymFacade.updateTrainer(existing);
        return ResponseEntity.ok(mapToProfileResponse(updated));
    }

    @PutMapping("/{username}/password")
    @Operation(summary = "Change Trainer login password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String username,
            @Valid @RequestBody PasswordChangeRequest request,
            jakarta.servlet.http.HttpServletRequest servletRequest) {

        log.info("REST request to change password for: {}", username);

        String authenticatedUser = (String) servletRequest.getAttribute("authenticatedUser");
        if (!username.equals(authenticatedUser)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        gymFacade.changeTrainerPassword(username, request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/status")
    @Operation(summary = "Activate or Deactivate Trainer profile")
    public ResponseEntity<Void> updateStatus(@PathVariable String username,
                                             @RequestParam boolean isActive,
                                             jakarta.servlet.http.HttpServletRequest servletRequest) {

        log.info("REST request to set active={} for trainer: {}", isActive, username);

        String authenticatedUser = (String) servletRequest.getAttribute("authenticatedUser");
        if (!username.equals(authenticatedUser)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        gymFacade.updateTrainerStatus(username, isActive);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/not-assigned/{traineeUsername}")
    @Operation(summary = "Get active trainers not assigned to a specific trainee")
    public ResponseEntity<List<TrainerShortInfo>> getNotAssignedTrainers(@PathVariable String traineeUsername,
                                                                         jakarta.servlet.http.HttpServletRequest servletRequest) {
        log.info("REST request to get active trainers not assigned to trainee: {}", traineeUsername);

        String authenticatedUser = (String) servletRequest.getAttribute("authenticatedUser");
        if (!traineeUsername.equals(authenticatedUser)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Trainer> trainers = gymFacade.getActiveTrainersNotAssignedToTrainee(traineeUsername);

        List<TrainerShortInfo> response = trainers.stream()
                .map(t -> new TrainerShortInfo(
                        t.getUser().getUsername(),
                        t.getUser().getFirstName(),
                        t.getUser().getLastName(),
                        t.getSpecialization().getTrainingTypeName().name()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private TrainerProfileResponse mapToProfileResponse(Trainer trainer) {
        TrainerProfileResponse response = new TrainerProfileResponse();
        response.setFirstName(trainer.getUser().getFirstName());
        response.setLastName(trainer.getUser().getLastName());
        response.setSpecialization(trainer.getSpecialization().getTrainingTypeName().name());
        response.setActive(trainer.getUser().isActive());

        List<TraineeShortInfo> trainees = trainer.getTrainees().stream()
                        .map(t -> new TraineeShortInfo(
                                t.getUser().getUsername(),
                                t.getUser().getFirstName(),
                                t.getUser().getLastName()
                        ))
                        .collect(Collectors.toList());

        response.setTrainees(trainees);
        return response;
    }
}