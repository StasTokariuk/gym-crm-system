package com.gym.crm.dao;

import com.gym.crm.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingDao extends AbstractDao<Training> {

    private static final Logger log = LoggerFactory.getLogger(TrainingDao.class);

    public TrainingDao() {
        super(Training.class);
    }

    @Override
    public Training save(Training training) {
        Training saved = super.save(training);
        log.info("Saved training with id={}", saved.getId());
        return saved;
    }
}