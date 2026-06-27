package com.gym.crm.dao;

import com.gym.crm.model.Trainee;
import com.gym.crm.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TraineeDao {

    private static final Logger log = LoggerFactory.getLogger(TraineeDao.class);

    private InMemoryStorage storage;
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public Trainee save(Trainee trainee) {
        if (trainee.getTraineeId() == null) {
            long id = generateNextId();
            trainee.setTraineeId(id);
            trainee.setUserId(id);
        }
        storage.getTraineeStorage().put(trainee.getTraineeId(), trainee);
        log.info("Saved trainee with id={}", trainee.getTraineeId());
        return trainee;
    }

    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(storage.getTraineeStorage().get(id));
    }

    public Optional<Trainee> findByUsername(String username) {
        return storage.getTraineeStorage().values().stream()
                .filter(t -> username.equals(t.getUsername()))
                .findFirst();
    }

    public List<Trainee> findAll() {
        return new java.util.ArrayList<>(storage.getTraineeStorage().values());
    }

    public void deleteById(Long id) {
        storage.getTraineeStorage().remove(id);
        log.info("Deleted trainee with id={}", id);
    }

    private long generateNextId() {
        Map<Long, Trainee> map = storage.getTraineeStorage();
        long max = map.keySet().stream().mapToLong(Long::longValue).max().orElse(0);
        idGenerator.set(Math.max(idGenerator.get(), max));
        return idGenerator.incrementAndGet();
    }
}