package com.gym.crm.controller;

import com.gym.crm.dto.response.TraineeTrainingResponse;
import com.gym.crm.dto.response.TrainerTrainingResponse;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.Training;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
@Api(tags = "Training Management", description = "Endpoints for retrieving trainings lists with filters")
public class TrainingController {

    private static final Logger log = LoggerFactory.getLogger(TrainingController.class);
    private final GymFacade gymFacade;

    @GetMapping("/trainee")
    @ApiOperation(value = "Get Trainee's trainings list with optional filters")
    public ResponseEntity<List<TraineeTrainingResponse>> getTraineeTrainings(
            @RequestParam String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType,
            jakarta.servlet.http.HttpServletRequest servletRequest) {

        log.info("REST request to get trainee trainings for user: {}", username);

        String authenticatedUser = (String) servletRequest.getAttribute("authenticatedUser");
        if (!username.equals(authenticatedUser)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Training> trainings = gymFacade.getTraineeTrainings(username, periodFrom, periodTo, trainerName, trainingType);

        List<TraineeTrainingResponse> response = trainings != null ? trainings.stream()
                .map(t -> new TraineeTrainingResponse(
                        t.getTrainingName(),
                        t.getTrainingDate(),
                        t.getTrainingType() != null && t.getTrainingType().getTrainingTypeName() != null
                        ? t.getTrainingType().getTrainingTypeName().name() : null,
                        t.getTrainingDuration(),
                        t.getTrainer() != null && t.getTrainer().getUser() != null
                        ? t.getTrainer().getUser().getFirstName() : null
                ))
                .collect(Collectors.toList()) : Collections.emptyList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/trainer")
    @ApiOperation(value = "Get Trainer's trainings list with optional filters")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            @RequestParam String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String traineeName,
            jakarta.servlet.http.HttpServletRequest servletRequest) {

        log.info("REST request to get trainer trainings for user: {}", username);

        String authenticatedUser = (String) servletRequest.getAttribute("authenticatedUser");
        if (!username.equals(authenticatedUser)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Training> trainings = gymFacade.getTrainerTrainings(username, periodFrom, periodTo, traineeName);

        List<TrainerTrainingResponse> response = trainings != null ? trainings.stream()
                .map(t -> new TrainerTrainingResponse(
                        t.getTrainingName(),
                        t.getTrainingDate(),
                        t.getTrainingType() != null && t.getTrainingType().getTrainingTypeName() != null
                        ? t.getTrainingType().getTrainingTypeName().name() : null,
                        t.getTrainingDuration(),
                        t.getTrainee() != null && t.getTrainee().getUser() != null
                        ? t.getTrainee().getUser().getFirstName() : null
                ))
                .collect(Collectors.toList()) : Collections.emptyList();

        return ResponseEntity.ok(response);
    }
}