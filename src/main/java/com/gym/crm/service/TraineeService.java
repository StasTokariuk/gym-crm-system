package com.gym.crm.service;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileService userProfileService;

    private final Counter traineeRegistrationCounter;
    private final Timer traineeCreationTimer;

    public TraineeService(TraineeDao traineeDao,
                          TrainerDao trainerDao,
                          PasswordEncoder passwordEncoder,
                          UserProfileService userProfileService,
                          MeterRegistry meterRegistry) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.passwordEncoder = passwordEncoder;
        this.userProfileService = userProfileService;

        this.traineeRegistrationCounter = Counter.builder("gym.trainee.registrations")
                .description("Total number of registered trainees")
                .register(meterRegistry);

        this.traineeCreationTimer = Timer.builder("gym.trainee.creation.time")
                .description("Time taken to create a trainee profile")
                .register(meterRegistry);
    }

    @Transactional
    public Trainee create(Trainee trainee) {
        return traineeCreationTimer.record(() -> {
            User user = trainee.getUser();
            String username = userProfileService.buildUsername(
                    user.getFirstName(), user.getLastName(),
                    candidate -> traineeDao.findByUsername(candidate).isPresent());

            user.setUsername(username);
            String rawPassword = userProfileService.generatePassword();
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setActive(true);

            Trainee saved = traineeDao.save(trainee);
            traineeRegistrationCounter.increment();
            log.info("Created trainee profile: username={}, temporary password={}", username, rawPassword);
            return saved;
        });
    }

    @Transactional
    public Trainee update(Trainee trainee) {
        if (trainee.getId() == null || traineeDao.findById(trainee.getId()).isEmpty()) {
            throw new com.gym.crm.exception.ResourceNotFoundException("Trainee not found: id=" + trainee.getId());
        }
        Trainee saved = traineeDao.save(trainee);
        log.info("Updated trainee username={}", saved.getUser().getUsername());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        traineeDao.deleteById(id);
        log.info("Deleted trainee id={}", id);
    }

    @Transactional
    public void deleteByUsername(String username) {
        Optional<Trainee> trainee = traineeDao.findByUsername(username);
        trainee.ifPresent(t -> {
            traineeDao.delete(t);
            log.info("Deleted trainee with username: {}", username);
        });
    }

    public Optional<Trainee> select(Long id) {
        return traineeDao.findById(id);
    }

    public Optional<Trainee> selectByUsername(String username) {
        return traineeDao.findByUsername(username);
    }

    public List<Trainee> selectAll() {
        return traineeDao.findAll();
    }

    public boolean authenticate(String username, String password) {
        Optional<Trainee> trainee = traineeDao.findByUsername(username);
        if (trainee.isPresent()) {
            return passwordEncoder.matches(password, trainee.get().getUser().getPassword());
        }
        return false;
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        trainee.getUser().setPassword(passwordEncoder.encode(newPassword));
        traineeDao.save(trainee);
        log.info("Changed password for trainee username={}", username);
    }

    @Transactional
    public void activateDeactivate(String username, boolean isActive) {
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        trainee.getUser().setActive(isActive);
        traineeDao.save(trainee);
        log.info("Set active={} for trainee username={}", isActive, username);
    }

    @Transactional
    public void updateTrainersList(String traineeUsername, List<String> trainerUsernames) {
        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + traineeUsername));

        Set<Trainer> trainers = trainee.getTrainers();
        trainers.clear();

        for (String tUsername : trainerUsernames) {
            Trainer trainer = trainerDao.findByUsername(tUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + tUsername));
            trainers.add(trainer);
        }
        traineeDao.save(trainee);
        log.info("Updated trainers list for trainee: {}", traineeUsername);
    }
}