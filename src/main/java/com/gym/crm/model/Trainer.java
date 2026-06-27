package com.gym.crm.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Trainer extends User {
    private Long trainerId;
    private TrainingTypeName specialization;

    public Trainer(String firstName, String lastName, TrainingTypeName specialization) {
        super(firstName, lastName);
        this.specialization = specialization;
    }
}