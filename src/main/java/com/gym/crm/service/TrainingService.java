package com.gym.crm.service;

import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Training;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDao trainingDao;

    public Training create(Training training) {
        Training saved = trainingDao.save(training);
        log.info("Created training id={}", saved.getTrainingId());
        return saved;
    }

    public Optional<Training> select(Long id) {
        log.debug("Selecting training id={}", id);
        return trainingDao.findById(id);
    }

    public List<Training> selectAll() {
        return trainingDao.findAll();
    }
}