package com.gym.crm.service;

import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDao trainerDao;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileService userProfileService;

    @Transactional
    public Trainer create(Trainer trainer) {
        User user = trainer.getUser();
        String username = userProfileService.buildUsername(
                user.getFirstName(), user.getLastName(),
                candidate -> trainerDao.findByUsername(candidate).isPresent());

        user.setUsername(username);
        String rawPassword = userProfileService.generatePassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);

        Trainer saved = trainerDao.save(trainer);
        log.info("Created trainer profile: username={}, temporary password={}", username, rawPassword);
        return saved;
    }

    @Transactional
    public Trainer update(Trainer trainer) {
        if (trainer.getId() == null || trainerDao.findById(trainer.getId()).isEmpty()) {
            throw new com.gym.crm.exception.ResourceNotFoundException("Trainer not found: id=" + trainer.getId());
        }
        Trainer saved = trainerDao.save(trainer);
        log.info("Updated trainer username={}", saved.getUser().getUsername());
        return saved;
    }

    public Optional<Trainer> select(Long id) {
        return trainerDao.findById(id);
    }

    public Optional<Trainer> selectByUsername(String username) {
        return trainerDao.findByUsername(username);
    }

    public List<Trainer> selectAll() {
        return trainerDao.findAll();
    }

    public boolean authenticate(String username, String password) {
        Optional<Trainer> trainer = trainerDao.findByUsername(username);
        if (trainer.isPresent()) {
            return passwordEncoder.matches(password, trainer.get().getUser().getPassword());
        }
        return false;
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
        trainer.getUser().setPassword(passwordEncoder.encode(newPassword));
        trainerDao.save(trainer);
        log.info("Changed password for trainer username={}", username);
    }

    @Transactional
    public void activateDeactivate(String username, boolean isActive) {
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
        trainer.getUser().setActive(isActive);
        trainerDao.save(trainer);
        log.info("Set active={} for trainer username={}", isActive, username);
    }

    public List<Trainer> getActiveTrainersNotAssignedToTrainee(String traineeUsername) {
        return trainerDao.findActiveTrainersNotAssignedToTrainee(traineeUsername);
    }
}