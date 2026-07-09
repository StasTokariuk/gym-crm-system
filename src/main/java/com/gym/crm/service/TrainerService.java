package com.gym.crm.service;

import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDao trainerDao;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileService userProfileService;

    public Trainer create(Trainer trainer) {
        String username = userProfileService.buildUsername(
                trainer.getFirstName(), trainer.getLastName(),
                candidate -> trainerDao.findByUsername(candidate).isPresent());
        trainer.setUsername(username);
        trainer.setPassword(passwordEncoder.encode(userProfileService.generatePassword()));
        trainer.setActive(true);
        Trainer saved = trainerDao.save(trainer);
        log.info("Created trainer profile: username={}", saved.getUsername());
        return saved;
    }

    public Trainer update(Trainer trainer) {
        if (trainer.getTrainerId() == null || trainerDao.findById(trainer.getTrainerId()).isEmpty()) {
            throw new IllegalArgumentException("Trainer not found: id=" + trainer.getTrainerId());
        }
        Trainer saved = trainerDao.save(trainer);
        log.info("Updated trainer id={}", saved.getTrainerId());
        return saved;
    }

    public void delete(Long id) {
        trainerDao.deleteById(id);
        log.info("Deleted trainer id={}", id);
    }

    public Optional<Trainer> select(Long id) {
        log.debug("Selecting trainer id={}", id);
        return trainerDao.findById(id);
    }

    public List<Trainer> selectAll() {
        return trainerDao.findAll();
    }
}