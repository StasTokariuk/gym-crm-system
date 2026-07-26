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
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
@Api(tags = "Trainer Management", description = "Endpoints for Trainer profiles and operations")
public class TrainerController {

    private static final Logger log = LoggerFactory.getLogger(TrainerController.class);
    private final GymFacade gymFacade;
    private final TrainingTypeDao trainingTypeDao;

    @PostMapping
    @ApiOperation(value = "Register a new Trainer profile")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Trainer registered successfully", response = TrainerRegistrationResponse.class),
            @ApiResponse(code = 400, message = "Invalid input data")
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
    @ApiOperation(value = "Get Trainer profile details by username")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(@PathVariable String username) {
        log.info("REST request to get trainer profile: {}", username);
        Trainer trainer = gymFacade.getTrainerByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found with username: " + username));

        TrainerProfileResponse response = mapToProfileResponse(trainer);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @ApiOperation(value = "Update an existing Trainer profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainer(@Valid @RequestBody TrainerUpdateRequest request) {
        log.info("REST request to update trainer: {}", request.getUsername());
        Trainer existing = gymFacade.getTrainerByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + request.getUsername()));

        existing.getUser().setFirstName(request.getFirstName());
        existing.getUser().setLastName(request.getLastName());
        existing.getUser().setActive(request.getIsActive());

        Trainer updated = gymFacade.updateTrainer(existing);
        return ResponseEntity.ok(mapToProfileResponse(updated));
    }

    @PutMapping("/password")
    @ApiOperation(value = "Change Trainer login password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        log.info("REST request to change password for trainer: {}", request.getUsername());
        if (!gymFacade.authenticateTrainer(request.getUsername(), request.getOldPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        gymFacade.changeTrainerPassword(request.getUsername(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/status")
    @ApiOperation(value = "Activate or Deactivate Trainer profile")
    public ResponseEntity<Void> updateStatus(@PathVariable String username, @RequestParam boolean isActive) {
        log.info("REST request to set active={} for trainer: {}", isActive, username);
        gymFacade.updateTrainerStatus(username, isActive);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/not-assigned/{traineeUsername}")
    @ApiOperation(value = "Get active trainers not assigned to a specific trainee")
    public ResponseEntity<List<TrainerShortInfo>> getNotAssignedTrainers(@PathVariable String traineeUsername) {
        log.info("REST request to get active trainers not assigned to trainee: {}", traineeUsername);
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