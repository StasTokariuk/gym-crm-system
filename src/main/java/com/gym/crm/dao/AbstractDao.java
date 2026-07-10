package com.gym.crm.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDao<T> {

    @Autowired
    protected SessionFactory sessionFactory;

    protected final Class<T> clazz;

    protected AbstractDao(Class<T> clazz) {
        this.clazz = clazz;
    }

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public T save(T entity) {
        getCurrentSession().saveOrUpdate(entity);
        return entity;
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(getCurrentSession().get(clazz, id));
    }

    public List<T> findAll() {
        return getCurrentSession().createQuery("from " + clazz.getName(), clazz).getResultList();
    }

    public void delete(T entity) {
        getCurrentSession().delete(entity);
    }

    public void deleteById(Long id) {
        T entity = getCurrentSession().get(clazz, id);
        if (entity != null) {
            delete(entity);
        }
    }
}