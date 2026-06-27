package com.gym.crm.service;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.model.Trainee;
import com.gym.crm.service.util.UserProfileUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeDao traineeDao;
    private final PasswordEncoder passwordEncoder;

    public Trainee create(Trainee trainee) {
        Set<String> existing = collectUsernames();
        String username = UserProfileUtil.buildUsername(
                trainee.getFirstName(), trainee.getLastName(), existing);
        trainee.setUsername(username);
        trainee.setPassword(passwordEncoder.encode(UserProfileUtil.generatePassword()));
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

    private Set<String> collectUsernames() {
        return traineeDao.findAll().stream()
                .map(Trainee::getUsername)
                .collect(Collectors.toSet());
    }
}