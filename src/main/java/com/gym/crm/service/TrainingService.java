package com.gym.crm.service;

import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Training;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDao trainingDao;
    private final SessionFactory sessionFactory;
    private final Counter trainingCreationCounter;

    public TrainingService(TrainingDao trainingDao,
                           SessionFactory sessionFactory,
                           MeterRegistry meterRegistry) {
        this.trainingDao = trainingDao;
        this.sessionFactory = sessionFactory;
        this.trainingCreationCounter = Counter.builder("gym.training.created")
                .description("Total number of created trainings")
                .register(meterRegistry);
    }

    @Transactional
    public Training create(Training training) {
        Training saved = trainingDao.save(training);
        trainingCreationCounter.increment();
        log.info("Created training id={}", saved.getId());
        return saved;
    }

    public Optional<Training> select(Long id) {
        return trainingDao.findById(id);
    }

    public List<Training> selectAll() {
        return trainingDao.findAll();
    }

    public List<Training> getTraineeTrainings(String username, LocalDate fromDate, LocalDate toDate,
                                              String trainerName, String trainingTypeName) {
        log.debug("Filtering trainee trainings for: {}", username);
        Session session = sessionFactory.getCurrentSession();

        StringBuilder hql = new StringBuilder(
                "SELECT t FROM Training t JOIN t.trainee tn JOIN tn.user u " +
                        "WHERE u.username = :username"
        );
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", username);

        if (fromDate != null) {
            hql.append(" AND t.trainingDate >= :fromDate");
            parameters.put("fromDate", fromDate);
        }
        if (toDate != null) {
            hql.append(" AND t.trainingDate <= :toDate");
            parameters.put("toDate", toDate);
        }
        if (trainerName != null && !trainerName.trim().isEmpty()) {
            hql.append(" AND t.trainer.user.firstName = :trainerName");
            parameters.put("trainerName", trainerName);
        }
        if (trainingTypeName != null && !trainingTypeName.trim().isEmpty()) {
            hql.append(" AND t.trainingType.trainingTypeName = :typeName");
            parameters.put("typeName", com.gym.crm.model.TrainingTypeName.valueOf(trainingTypeName.toUpperCase()));
        }

        var query = session.createQuery(hql.toString(), Training.class);
        parameters.forEach(query::setParameter);
        return query.getResultList();
    }

    public List<Training> getTrainerTrainings(String username, LocalDate fromDate, LocalDate toDate,
                                              String traineeName) {
        log.debug("Filtering trainer trainings for: {}", username);
        Session session = sessionFactory.getCurrentSession();

        StringBuilder hql = new StringBuilder(
                "SELECT t FROM Training t JOIN t.trainer tr JOIN tr.user u " +
                        "WHERE u.username = :username"
        );
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", username);

        if (fromDate != null) {
            hql.append(" AND t.trainingDate >= :fromDate");
            parameters.put("fromDate", fromDate);
        }
        if (toDate != null) {
            hql.append(" AND t.trainingDate <= :toDate");
            parameters.put("toDate", toDate);
        }
        if (traineeName != null && !traineeName.trim().isEmpty()) {
            hql.append(" AND t.trainee.user.firstName = :traineeName");
            parameters.put("traineeName", traineeName);
        }

        var query = session.createQuery(hql.toString(), Training.class);
        parameters.forEach(query::setParameter);
        return query.getResultList();
    }
}