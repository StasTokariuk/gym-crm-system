package com.gym.crm.dao;

import com.gym.crm.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerDao extends AbstractDao<Trainer> {

    private static final Logger log = LoggerFactory.getLogger(TrainerDao.class);

    @Override
    protected Map<Long, Trainer> getStorageMap() {
        return storage.getTrainerStorage();
    }

    @Override
    protected Long getEntityId(Trainer entity) {
        return entity.getTrainerId();
    }

    @Override
    protected void setEntityId(Trainer entity, Long id) {
        entity.setTrainerId(id);
        entity.setUserId(id);
    }

    @Override
    public Trainer save(Trainer trainer) {
        Trainer saved = super.save(trainer);
        log.info("Saved trainer with id={}", saved.getTrainerId());
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        super.deleteById(id);
        log.info("Deleted trainer with id={}", id);
    }

    @Override
    public void initializeIdGenerator() {
        super.initializeIdGenerator();
        log.debug("Initialized TrainerDao idGenerator to {}", idGenerator.get());
    }

    public Optional<Trainer> findByUsername(String username) {
        return getStorageMap().values().stream()
                .filter(t -> username.equals(t.getUsername()))
                .findFirst();
    }
}