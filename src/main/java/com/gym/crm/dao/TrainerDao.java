package com.gym.crm.dao;

import com.gym.crm.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDao extends AbstractDao<Trainer> {

    private static final Logger log = LoggerFactory.getLogger(TrainerDao.class);

    public TrainerDao() {
        super(Trainer.class);
    }

    @Override
    public Trainer save(Trainer trainer) {
        Trainer saved = super.save(trainer);
        log.info("Saved trainer with id={}", saved.getId());
        return saved;
    }

    public Optional<Trainer> findByUsername(String username) {
        log.debug("Finding trainer by username: {}", username);
        return getCurrentSession()
                .createQuery("SELECT t FROM Trainer t JOIN t.user u WHERE u.username = :username", Trainer.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    /**
     * Отримує список активних тренерів, які ще не призначені даному Trainee.
     */
    public List<Trainer> findActiveTrainersNotAssignedToTrainee(String traineeUsername) {
        log.debug("Finding active trainers not assigned to trainee: {}", traineeUsername);
        String hql = "SELECT t FROM Trainer t WHERE t.user.isActive = true AND t NOT IN " +
                "(SELECT tr FROM Trainee tn JOIN tn.trainers tr WHERE tn.user.username = :username)";
        return getCurrentSession()
                .createQuery(hql, Trainer.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }
}