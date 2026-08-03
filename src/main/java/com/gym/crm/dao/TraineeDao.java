package com.gym.crm.dao;

import com.gym.crm.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TraineeDao extends AbstractDao<Trainee> {

    private static final Logger log = LoggerFactory.getLogger(TraineeDao.class);

    public TraineeDao() {
        super(Trainee.class);
    }

    @Override
    public Trainee save(Trainee trainee) {
        Trainee saved = super.save(trainee);
        log.info("Saved trainee with id={}", saved.getId());
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        super.deleteById(id);
        log.info("Deleted trainee with id={}", id);
    }

    public Optional<Trainee> findByUsername(String username) {
        log.debug("Finding trainee by username: {}", username);
        return entityManager
                .createQuery("SELECT t FROM Trainee t JOIN t.user u WHERE u.username = :username", Trainee.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }
}