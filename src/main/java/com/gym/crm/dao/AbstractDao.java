package com.gym.crm.dao;

import com.gym.crm.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractDao<T> {

    protected InMemoryStorage storage;
    protected final AtomicLong idGenerator = new AtomicLong(0);

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    protected abstract Map<Long, T> getStorageMap();
    protected abstract Long getEntityId(T entity);
    protected abstract void setEntityId(T entity, Long id);

    public T save(T entity) {
        if (getEntityId(entity) == null) {
            setEntityId(entity, generateNextId());
        }
        getStorageMap().put(getEntityId(entity), entity);
        return entity;
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(getStorageMap().get(id));
    }

    public List<T> findAll() {
        return new ArrayList<>(getStorageMap().values());
    }

    public void deleteById(Long id) {
        getStorageMap().remove(id);
    }

    public void initializeIdGenerator() {
        long max = getStorageMap().keySet().stream().mapToLong(Long::longValue).max().orElse(0);
        idGenerator.set(max);
    }

    private long generateNextId() {
        return idGenerator.incrementAndGet();
    }
}