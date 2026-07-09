package com.gym.crm.service;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.model.Trainee;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeDao traineeDao;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileService userProfileService;

    public Trainee create(Trainee trainee) {
        String username = userProfileService.buildUsername(
                trainee.getFirstName(), trainee.getLastName(),
                candidate -> traineeDao.findByUsername(candidate).isPresent());
        trainee.setUsername(username);
        trainee.setPassword(passwordEncoder.encode(userProfileService.generatePassword()));
        trainee.setActive(true);
        Trainee saved = traineeDao.save(trainee);
        log.info("Created trainee profile: username={}", saved.getUsername());
        return saved;
    }

    public Trainee update(Trainee trainee) {
        if (trainee.getTraineeId() == null || traineeDao.findById(trainee.getTraineeId()).isEmpty()) {
            throw new IllegalArgumentException("Trainee not found: id=" + trainee.getTraineeId());
        }
        Trainee saved = traineeDao.save(trainee);
        log.info("Updated trainee id={}", saved.getTraineeId());
        return saved;
    }

    public void delete(Long id) {
        traineeDao.deleteById(id);
        log.info("Deleted trainee id={}", id);
    }

    public Optional<Trainee> select(Long id) {
        log.debug("Selecting trainee id={}", id);
        return traineeDao.findById(id);
    }

    public List<Trainee> selectAll() {
        return traineeDao.findAll();
    }
}