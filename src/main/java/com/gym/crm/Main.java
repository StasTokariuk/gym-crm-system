package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        var context = new AnnotationConfigApplicationContext(AppConfig.class);
        GymFacade facade = context.getBean(GymFacade.class);

        log.info("Trainees from file: {}", facade.getAllTrainees());

        Trainee newTrainee = new Trainee("John", "Smith",
                LocalDate.of(2000, 1, 1), "Odesa");
        Trainee created = facade.createTrainee(newTrainee);
        log.info("New trainee username (with serial suffix): {}", created.getUsername());

        Training training = new Training(created.getTraineeId(), 1L,
                "Cardio Blast", TrainingTypeName.CARDIO, LocalDate.now(), 50);
        facade.createTraining(training);

        log.info("All trainings: {}", facade.getAllTrainings());

        context.close();
    }
}