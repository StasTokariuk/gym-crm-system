package com.gym.crm.dao;

import com.gym.crm.model.Training;
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
public class TrainingDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingDao.class);

    private InMemoryStorage storage;
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public Training save(Training training) {
        if (training.getTrainingId() == null) {
            training.setTrainingId(generateNextId());
        }
        storage.getTrainingStorage().put(training.getTrainingId(), training);
        log.info("Saved training with id={}", training.getTrainingId());
        return training;
    }

    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(storage.getTrainingStorage().get(id));
    }

    public List<Training> findAll() {
        return new ArrayList<>(storage.getTrainingStorage().values());
    }

    private long generateNextId() {
        Map<Long, Training> map = storage.getTrainingStorage();
        long max = map.keySet().stream().mapToLong(Long::longValue).max().orElse(0);
        idGenerator.set(Math.max(idGenerator.get(), max));
        return idGenerator.incrementAndGet();
    }
}