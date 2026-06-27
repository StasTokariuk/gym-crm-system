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
        log.info("GymFacade created with constructor injection");
    }

    // ----- Trainee -----
    public Trainee createTrainee(Trainee t) {
        return traineeService.create(t);
    }

    public void updateTrainee(Trainee t) {
        traineeService.update(t);
    }

    public void deleteTrainee(Long id) {
        traineeService.delete(id);
    }

    public Optional<Trainee> getTrainee(Long id) {
        return traineeService.select(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.selectAll();
    }

    // ----- Trainer -----
    public Trainer createTrainer(Trainer t) {
        return trainerService.create(t);
    }

    public void updateTrainer(Trainer t) {
        trainerService.update(t);
    }

    public Optional<Trainer> getTrainer(Long id) {
        return trainerService.select(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.selectAll();
    }

    // ----- Training -----
    public Training createTraining(Training t) {
        return trainingService.create(t);
    }

    public Optional<Training> getTraining(Long id) {
        return trainingService.select(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.selectAll();
    }
}