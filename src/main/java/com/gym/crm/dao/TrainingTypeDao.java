package com.gym.crm.dao;

import com.gym.crm.model.TrainingType;
import com.gym.crm.model.TrainingTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TrainingTypeDao extends AbstractDao<TrainingType> {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeDao.class);

    public TrainingTypeDao() {
        super(TrainingType.class);
    }

    public Optional<TrainingType> findByName(TrainingTypeName name) {
        log.debug("Finding training type by name: {}", name);
        return entityManager
                .createQuery("SELECT tt FROM TrainingType tt WHERE tt.trainingTypeName = :name", TrainingType.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }
}