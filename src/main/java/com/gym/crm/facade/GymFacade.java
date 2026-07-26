package com.gym.crm.facade;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {

    private static final Logger log = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        log.info("GymFacade initialized and ready");
    }

    // ----- Trainee Operations -----
    public Trainee createTrainee(Trainee t) {
        return traineeService.create(t);
    }

    public Trainee updateTrainee(Trainee t) {
        traineeService.update(t);
        return t;
    }

    public void deleteTrainee(Long id) {
        traineeService.delete(id);
    }

    public void deleteTraineeByUsername(String username) {
        traineeService.deleteByUsername(username);
    }

    public Optional<Trainee> getTrainee(Long id) {
        return traineeService.select(id);
    }

    public Optional<Trainee> getTraineeByUsername(String username) {
        return traineeService.selectByUsername(username);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.selectAll();
    }

    public boolean authenticateTrainee(String username, String password) {
        return traineeService.authenticate(username, password);
    }

    public void changeTraineePassword(String username, String newPassword) {
        traineeService.changePassword(username, newPassword);
    }

    public void updateTraineeStatus(String username, boolean isActive) {
        traineeService.activateDeactivate(username, isActive);
    }

    public void updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames) {
        traineeService.updateTrainersList(traineeUsername, trainerUsernames);
    }

    // ----- Trainer Operations -----
    public Trainer createTrainer(Trainer t) {
        return trainerService.create(t);
    }

    public Trainer updateTrainer(Trainer t) {
        trainerService.update(t);
        return t;
    }

    public Optional<Trainer> getTrainer(Long id) {
        return trainerService.select(id);
    }

    public Optional<Trainer> getTrainerByUsername(String username) {
        return trainerService.selectByUsername(username);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.selectAll();
    }

    public boolean authenticateTrainer(String username, String password) {
        return trainerService.authenticate(username, password);
    }

    public void changeTrainerPassword(String username, String newPassword) {
        trainerService.changePassword(username, newPassword);
    }

    public void updateTrainerStatus(String username, boolean isActive) {
        trainerService.activateDeactivate(username, isActive);
    }

    public List<Trainer> getActiveTrainersNotAssignedToTrainee(String traineeUsername) {
        return trainerService.getActiveTrainersNotAssignedToTrainee(traineeUsername);
    }

    // ----- Training Operations -----
    public Training createTraining(Training t) {
        return trainingService.create(t);
    }

    public Optional<Training> getTraining(Long id) {
        return trainingService.select(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.selectAll();
    }

    public List<Training> getTraineeTrainings(String username, LocalDate fromDate, LocalDate toDate,
                                              String trainerName, String trainingTypeName) {
        return trainingService.getTraineeTrainings(username, fromDate, toDate, trainerName, trainingTypeName);
    }

    public List<Training> getTrainerTrainings(String username, LocalDate fromDate, LocalDate toDate,
                                              String traineeName) {
        return trainingService.getTrainerTrainings(username, fromDate, toDate, traineeName);
    }
}