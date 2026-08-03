package com.gym.crm.controller;

import com.gym.crm.dto.request.PasswordChangeRequest;
import com.gym.crm.dto.request.TraineeRegistrationRequest;
import com.gym.crm.dto.request.TraineeUpdateRequest;
import com.gym.crm.dto.response.TraineeProfileResponse;
import com.gym.crm.dto.response.TraineeRegistrationResponse;
import com.gym.crm.dto.response.TrainerShortInfo;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.Trainee;
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
@RequestMapping("/api/trainees")
@RequiredArgsConstructor
@Tag(name = "Trainee Management", description = "Endpoints for Trainee profiles and operations")
public class TraineeController {

    private static final Logger log = LoggerFactory.getLogger(TraineeController.class);
    private final GymFacade gymFacade;

    @PostMapping
    @Operation(summary = "Register a new Trainee profile",
            description = "Generates username and temporary password automatically.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainee registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<TraineeRegistrationResponse> registerTrainee(@Valid @RequestBody TraineeRegistrationRequest request) {
        log.info("REST request to register trainee: {} {}", request.getFirstName(), request.getLastName());

        User user = new User(request.getFirstName(), request.getLastName());
        Trainee trainee = new Trainee(user, request.getDateOfBirth(), request.getAddress());

        Trainee saved = gymFacade.createTrainee(trainee);

        TraineeRegistrationResponse response = new TraineeRegistrationResponse(
                saved.getUser().getUsername(),
                saved.getUser().getPassword()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get Trainee profile details by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Trainee profile not found")
    })
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(@PathVariable String username,
                                                                    jakarta.servlet.http.HttpServletRequest servletRequest) {
        log.info("REST request to get trainee profile: {}", username);

        String authenticatedUser = (String) servletRequest.getAttribute("authenticatedUser");
        if (!username.equals(authenticatedUser)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Trainee trainee = gymFacade.getTraineeByUsername(username)
                .orElseThrow(() -> new com.gym.crm.exception.ResourceNotFoundException("Trainee not found with username: " + username));

        TraineeProfileResponse response = mapToProfileResponse(trainee);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update an existing Trainee profile")
    public ResponseEntity<TraineeProfileResponse> updateTrainee(
            @PathVariable String username,
            @Valid @RequestBody TraineeUpdateRequest request,
            jakarta.servlet.http.HttpServletRequest servletRequest) {

        log.info("REST request to update trainee: {}", username);

        String authenticatedUser = (String) servletRequest.getAttribute("authenticatedUser");
        if (!username.equals(authenticatedUser)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Trainee existing = gymFacade.getTraineeByUsername(username)
                .orElseThrow(() -> new com.gym.crm.exception.ResourceNotFoundException("Trainee not found: " + username));

        existing.getUser().setFirstName(request.getFirstName());
        existing.getUser().setLastName(request.getLastName());
        existing.getUser().setActive(request.getIsActive());
        existing.setDateOfBirth(request.getDateOfBirth());
        existing.setAddress(request.getAddress());

        Trainee updated = gymFacade.updateTrainee(existing);
        return ResponseEntity.ok(mapToProfileResponse(updated));
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete Trainee profile")
    public ResponseEntity<Void> deleteTrainee(@PathVariable String username,
                                              jakarta.servlet.http.HttpServletRequest servletRequest) {
        log.info("REST request to delete trainee: {}", username);

        String authenticatedUser = (String) servletRequest.getAttribute("authenticatedUser");
        if (!username.equals(authenticatedUser)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        gymFacade.getTraineeByUsername(username)
                .orElseThrow(() -> new com.gym.crm.exception.ResourceNotFoundException("Trainee not found: " + username));

        gymFacade.deleteTraineeByUsername(username);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/password")
    @Operation(summary = "Change Trainee login password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String username,
            @Valid @RequestBody PasswordChangeRequest request,
            jakarta.servlet.http.HttpServletRequest servletRequest) {

        log.info("REST request to change password for: {}", username);

        String authenticatedUser = (String) servletRequest.getAttribute("authenticatedUser");
        if (!username.equals(authenticatedUser) || !username.equals(request.getUsername())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        gymFacade.changeTraineePassword(username, request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/status")
    @Operation(summary = "Activate or Deactivate Trainee profile")
    public ResponseEntity<Void> updateStatus(@PathVariable String username,
                                             @RequestParam boolean isActive,
                                             jakarta.servlet.http.HttpServletRequest servletRequest) {
        log.info("REST request to set active={} for trainee: {}", isActive, username);

        String authenticatedUser = (String) servletRequest.getAttribute("authenticatedUser");
        if (!username.equals(authenticatedUser)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        gymFacade.updateTraineeStatus(username, isActive);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update Trainee's list of assigned Trainers")
    public ResponseEntity<List<TrainerShortInfo>> updateTrainersList(@PathVariable String username,
                                                                     @RequestBody List<String> trainerUsernames,
                                                                     jakarta.servlet.http.HttpServletRequest servletRequest) {
        log.info("REST request to update trainers for trainee: {}", username);

        String authenticatedUser = (String) servletRequest.getAttribute("authenticatedUser");
        if (!username.equals(authenticatedUser)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        gymFacade.updateTraineeTrainers(username, trainerUsernames);

        Trainee updated = gymFacade.getTraineeByUsername(username)
                .orElseThrow(() -> new com.gym.crm.exception.ResourceNotFoundException("Trainee not found: " + username));

        List<TrainerShortInfo> response = updated.getTrainers() != null ?
                updated.getTrainers().stream()
                        .map(t -> new TrainerShortInfo(
                                t.getUser().getUsername(),
                                t.getUser().getFirstName(),
                                t.getUser().getLastName(),
                                t.getSpecialization().getTrainingTypeName().name()
                        ))
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return ResponseEntity.ok(response);
    }

    private TraineeProfileResponse mapToProfileResponse(Trainee trainee) {
        TraineeProfileResponse response = new TraineeProfileResponse();
        response.setFirstName(trainee.getUser().getFirstName());
        response.setLastName(trainee.getUser().getLastName());
        response.setDateOfBirth(trainee.getDateOfBirth());
        response.setAddress(trainee.getAddress());
        response.setActive(trainee.getUser().isActive());

        List<TrainerShortInfo> trainers = trainee.getTrainers() != null ?
                trainee.getTrainers().stream()
                        .map(t -> new TrainerShortInfo(
                                t.getUser().getUsername(),
                                t.getUser().getFirstName(),
                                t.getUser().getLastName(),
                                t.getSpecialization().getTrainingTypeName().name()
                        ))
                        .collect(Collectors.toList())
                : Collections.emptyList();

        response.setTrainers(trainers);
        return response;
    }
}