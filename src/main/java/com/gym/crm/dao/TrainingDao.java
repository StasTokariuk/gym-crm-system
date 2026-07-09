package com.gym.crm.dao;

import com.gym.crm.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class TrainingDao extends AbstractDao<Training> {

    private static final Logger log = LoggerFactory.getLogger(TrainingDao.class);

    @Override
    protected Map<Long, Training> getStorageMap() {
        return storage.getTrainingStorage();
    }

    @Override
    protected Long getEntityId(Training entity) {
        return entity.getTrainingId();
    }

    @Override
    protected void setEntityId(Training entity, Long id) {
        entity.setTrainingId(id);
    }

    @Override
    public Training save(Training training) {
        Training saved = super.save(training);
        log.info("Saved training with id={}", saved.getTrainingId());
        return saved;
    }

    @Override
    public void initializeIdGenerator() {
        super.initializeIdGenerator();
        log.debug("Initialized TrainingDao idGenerator to {}", idGenerator.get());
    }
}