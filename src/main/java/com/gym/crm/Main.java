package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Starting Gym CRM Application...");

        var context = new AnnotationConfigApplicationContext(AppConfig.class);
        GymFacade facade = context.getBean(GymFacade.class);

        log.info("Trainees from file: {}", facade.getAllTrainees());

        User user = new User("John", "Smith");
        Trainee newTrainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Odesa");
        Trainee created = facade.createTrainee(newTrainee);
        log.info("New trainee username (with serial suffix): {}", created.getUser().getUsername());

        List<Trainer> trainers = facade.getAllTrainers();
        if (!trainers.isEmpty()) {
            Trainer trainer = trainers.get(0);
            TrainingType trainingType = trainer.getSpecialization();

            Training training = new Training(
                    created,
                    trainer,
                    "Cardio Blast",
                    trainingType,
                    LocalDate.now(),
                    50
            );
            facade.createTraining(training);
            log.info("Created training 'Cardio Blast' for Trainee {} and Trainer {}",
                    created.getUser().getUsername(), trainer.getUser().getUsername());
        } else {
            log.warn("No trainers found in the database. Cannot create training!");
        }

        log.info("All trainings: {}", facade.getAllTrainings());

        context.close();
        log.info("Application context closed.");
    }
}