package com.gym.crm.config;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    /**
     * Exposes Hibernate SessionFactory extracted from the JPA EntityManagerFactory
     * that Spring Boot auto-configures. Keeps existing DAO layer working without changes.
     */
    @Bean
    public SessionFactory sessionFactory(EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.unwrap(SessionFactory.class);
    }
}