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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainees")
@RequiredArgsConstructor
@Api(tags = "Trainee Management", description = "Endpoints for Trainee profiles and operations")
public class TraineeController {

    private static final Logger log = LoggerFactory.getLogger(TraineeController.class);
    private final GymFacade gymFacade;

    @PostMapping
    @ApiOperation(value = "Register a new Trainee profile", notes = "Generates username and temporary password automatically.")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Trainee registered successfully", response = TraineeRegistrationResponse.class),
            @ApiResponse(code = 400, message = "Invalid input data")
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
    @ApiOperation(value = "Get Trainee profile details by username")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile found", response = TraineeProfileResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized access"),
            @ApiResponse(code = 404, message = "Trainee profile not found")
    })
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(@PathVariable String username) {
        log.info("REST request to get trainee profile: {}", username);
        Trainee trainee = gymFacade.getTraineeByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with username: " + username));

        TraineeProfileResponse response = mapToProfileResponse(trainee);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @ApiOperation(value = "Update an existing Trainee profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee profile updated", response = TraineeProfileResponse.class),
            @ApiResponse(code = 400, message = "Invalid input data"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    public ResponseEntity<TraineeProfileResponse> updateTrainee(@Valid @RequestBody TraineeUpdateRequest request) {
        log.info("REST request to update trainee: {}", request.getUsername());
        Trainee existing = gymFacade.getTraineeByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + request.getUsername()));

        existing.getUser().setFirstName(request.getFirstName());
        existing.getUser().setLastName(request.getLastName());
        existing.getUser().setActive(request.getIsActive());
        existing.setDateOfBirth(request.getDateOfBirth());
        existing.setAddress(request.getAddress());

        Trainee updated = gymFacade.updateTrainee(existing);
        return ResponseEntity.ok(mapToProfileResponse(updated));
    }

    @DeleteMapping("/{username}")
    @ApiOperation(value = "Delete Trainee profile by username (hard delete and cascade trainings)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee profile deleted successfully"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    public ResponseEntity<Void> deleteTrainee(@PathVariable String username) {
        log.info("REST request to delete trainee: {}", username);
        gymFacade.getTraineeByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));

        gymFacade.deleteTraineeByUsername(username);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @ApiOperation(value = "Change Trainee login password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        log.info("REST request to change password for: {}", request.getUsername());
        if (!gymFacade.authenticateTrainee(request.getUsername(), request.getOldPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        gymFacade.changeTraineePassword(request.getUsername(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/status")
    @ApiOperation(value = "Activate or Deactivate Trainee profile")
    public ResponseEntity<Void> updateStatus(@PathVariable String username, @RequestParam boolean isActive) {
        log.info("REST request to set active={} for trainee: {}", isActive, username);
        gymFacade.updateTraineeStatus(username, isActive);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/trainers")
    @ApiOperation(value = "Update Trainee's list of assigned Trainers")
    public ResponseEntity<List<TrainerShortInfo>> updateTrainersList(@PathVariable String username, @RequestBody List<String> trainerUsernames) {
        log.info("REST request to update trainers for trainee: {}", username);
        gymFacade.updateTraineeTrainers(username, trainerUsernames);

        Trainee updated = gymFacade.getTraineeByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));

        List<TrainerShortInfo> response = updated.getTrainers().stream()
                .map(t -> new TrainerShortInfo(
                        t.getUser().getUsername(),
                        t.getUser().getFirstName(),
                        t.getUser().getLastName(),
                        t.getSpecialization().getTrainingTypeName().name()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private TraineeProfileResponse mapToProfileResponse(Trainee trainee) {
        TraineeProfileResponse response = new TraineeProfileResponse();
        response.setFirstName(trainee.getUser().getFirstName());
        response.setLastName(trainee.getUser().getLastName());
        response.setDateOfBirth(trainee.getDateOfBirth());
        response.setAddress(trainee.getAddress());
        response.setActive(trainee.getUser().isActive());

        List<TrainerShortInfo> trainers = trainee.getTrainers().stream()
                .map(t -> new TrainerShortInfo(
                        t.getUser().getUsername(),
                        t.getUser().getFirstName(),
                        t.getUser().getLastName(),
                        t.getSpecialization().getTrainingTypeName().name()
                ))
                .collect(Collectors.toList());
        response.setTrainers(trainers);
        return response;
    }
}