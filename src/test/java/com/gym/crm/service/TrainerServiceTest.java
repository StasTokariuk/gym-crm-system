package com.gym.crm.service;

import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingTypeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TrainerService service;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("create generates username, password and sets active flag")
    void create_generatesUsernameAndPassword() {
        when(passwordEncoder.encode(any(String.class))).thenAnswer(inv -> inv.getArgument(0));
        when(trainerDao.findAll()).thenReturn(List.of());
        when(trainerDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trainer t = new Trainer("Mike", "Brown", TrainingTypeName.FITNESS);
        Trainer result = service.create(t);

        assertEquals("Mike.Brown", result.getUsername());
        assertEquals(10, result.getPassword().length());
        assertTrue(result.isActive());
        assertEquals(TrainingTypeName.FITNESS, result.getSpecialization());
    }

    @Test
    @DisplayName("create adds serial suffix on username collision")
    void create_addsSerialSuffixOnCollision() {
        when(passwordEncoder.encode(any(String.class))).thenAnswer(inv -> inv.getArgument(0));
        Trainer existing = new Trainer("Mike", "Brown", TrainingTypeName.FITNESS);
        existing.setUsername("Mike.Brown");
        when(trainerDao.findAll()).thenReturn(List.of(existing));
        when(trainerDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trainer t = new Trainer("Mike", "Brown", TrainingTypeName.YOGA);
        Trainer result = service.create(t);

        assertEquals("Mike.Brown1", result.getUsername());
    }

    @Test
    @DisplayName("update updates an existing trainer")
    void update_existingTrainer() {
        Trainer t = new Trainer();
        t.setTrainerId(1L);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(t));
        when(trainerDao.save(t)).thenReturn(t);

        Trainer result = service.update(t);

        assertEquals(1L, result.getTrainerId());
        verify(trainerDao).save(t);
    }

    @Test
    @DisplayName("update throws when trainer not found")
    void update_throwsWhenNotFound() {
        Trainer t = new Trainer();
        t.setTrainerId(99L);
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.update(t));
        verify(trainerDao, never()).save(any());
    }

    @Test
    @DisplayName("select returns trainer by id")
    void select_returnsTrainer() {
        Trainer t = new Trainer();
        t.setTrainerId(1L);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(t));

        Optional<Trainer> result = service.select(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getTrainerId());
    }

    @Test
    @DisplayName("selectAll returns all trainers")
    void selectAll_returnsAll() {
        when(trainerDao.findAll()).thenReturn(List.of(new Trainer(), new Trainer()));

        List<Trainer> result = service.selectAll();

        assertEquals(2, result.size());
    }
}