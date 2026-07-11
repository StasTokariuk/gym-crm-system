package com.gym.crm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "trainers")
public class Trainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingType specialization;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToMany(mappedBy = "trainers", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Trainee> trainees = new HashSet<>();

    public Trainer(User user, TrainingType specialization) {
        this.user = user;
        this.specialization = specialization;
    }
}