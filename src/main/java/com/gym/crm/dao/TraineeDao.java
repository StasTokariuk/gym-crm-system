package com.gym.crm.dao;

import com.gym.crm.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class TraineeDao extends AbstractDao<Trainee> {

    private static final Logger log = LoggerFactory.getLogger(TraineeDao.class);

    @Override
    protected Map<Long, Trainee> getStorageMap() {
        return storage.getTraineeStorage();
    }

    @Override
    protected Long getEntityId(Trainee entity) {
        return entity.getTraineeId();
    }

    @Override
    protected void setEntityId(Trainee entity, Long id) {
        entity.setTraineeId(id);
        entity.setUserId(id);
    }

    @Override
    public Trainee save(Trainee trainee) {
        Trainee saved = super.save(trainee);
        log.info("Saved trainee with id={}", saved.getTraineeId());
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        super.deleteById(id);
        log.info("Deleted trainee with id={}", id);
    }

    @Override
    public void initializeIdGenerator() {
        super.initializeIdGenerator();
        log.debug("Initialized TraineeDao idGenerator to {}", idGenerator.get());
    }

    public Optional<Trainee> findByUsername(String username) {
        return getStorageMap().values().stream()
                .filter(t -> username.equals(t.getUsername()))
                .findFirst();
    }
}