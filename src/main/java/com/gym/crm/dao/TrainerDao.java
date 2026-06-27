package com.gym.crm.dao;

import com.gym.crm.model.Trainer;
import com.gym.crm.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TrainerDao {

    private static final Logger log = LoggerFactory.getLogger(TrainerDao.class);

    private InMemoryStorage storage;
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public Trainer save(Trainer trainer) {
        if (trainer.getTrainerId() == null) {
            long id = generateNextId();
            trainer.setTrainerId(id);
            trainer.setUserId(id);
        }
        storage.getTrainerStorage().put(trainer.getTrainerId(), trainer);
        log.info("Saved trainer with id={}", trainer.getTrainerId());
        return trainer;
    }

    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(storage.getTrainerStorage().get(id));
    }

    public Optional<Trainer> findByUsername(String username) {
        return storage.getTrainerStorage().values().stream()
                .filter(t -> username.equals(t.getUsername()))
                .findFirst();
    }

    public List<Trainer> findAll() {
        return new ArrayList<>(storage.getTrainerStorage().values());
    }

    private long generateNextId() {
        Map<Long, Trainer> map = storage.getTrainerStorage();
        long max = map.keySet().stream().mapToLong(Long::longValue).max().orElse(0);
        idGenerator.set(Math.max(idGenerator.get(), max));
        return idGenerator.incrementAndGet();
    }
}