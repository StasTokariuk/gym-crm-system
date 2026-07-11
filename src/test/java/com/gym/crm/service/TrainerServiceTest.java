package com.gym.crm.service;

import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.TrainingTypeName;
import com.gym.crm.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private TrainerService service;

    @Test
    @DisplayName("create generates username, password and sets active flag")
    void create_generatesUsernameAndPassword() {
        when(userProfileService.buildUsername(eq("Mike"), eq("Brown"), any(Predicate.class))).thenReturn("Mike.Brown");
        when(userProfileService.generatePassword()).thenReturn("1234567890");
        when(passwordEncoder.encode("1234567890")).thenReturn("encodedPassword");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        TrainingType spec = new TrainingType(TrainingTypeName.FITNESS);
        Trainer t = new Trainer(new User("Mike", "Brown"), spec);
        Trainer result = service.create(t);

        assertEquals("Mike.Brown", result.getUser().getUsername());
        assertEquals("encodedPassword", result.getUser().getPassword());
        assertTrue(result.getUser().isActive());
        assertEquals(TrainingTypeName.FITNESS, result.getSpecialization().getTrainingTypeName());

        verify(userProfileService).buildUsername(eq("Mike"), eq("Brown"), any(Predicate.class));
        verify(trainerDao).save(t);
    }

    @Test
    @DisplayName("create uses username with serial suffix on collision")
    void create_addsSerialSuffixOnCollision() {
        when(userProfileService.buildUsername(eq("Mike"), eq("Brown"), any(Predicate.class))).thenReturn("Mike.Brown1");
        when(userProfileService.generatePassword()).thenReturn("1234567890");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer t = new Trainer(new User("Mike", "Brown"), new TrainingType(TrainingTypeName.YOGA));
        Trainer result = service.create(t);

        assertEquals("Mike.Brown1", result.getUser().getUsername());
        verify(trainerDao).save(t);
    }

    @Test
    @DisplayName("update updates an existing trainer")
    void update_existingTrainer() {
        Trainer t = new Trainer();
        t.setId(1L);
        t.setUser(new User("Mike", "Brown"));
        when(trainerDao.findById(1L)).thenReturn(Optional.of(t));
        when(trainerDao.save(t)).thenReturn(t);

        Trainer result = service.update(t);

        assertEquals(1L, result.getId());
        verify(trainerDao).save(t);
    }

    @Test
    @DisplayName("update throws when trainer not found")
    void update_throwsWhenNotFound() {
        Trainer t = new Trainer();
        t.setId(99L);
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.update(t));
        verify(trainerDao, never()).save(any());
    }

    @Test
    @DisplayName("select returns trainer by id")
    void select_returnsTrainer() {
        Trainer t = new Trainer();
        t.setId(1L);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(t));

        Optional<Trainer> result = service.select(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("selectAll returns all trainers")
    void selectAll_returnsAll() {
        when(trainerDao.findAll()).thenReturn(List.of(new Trainer(), new Trainer()));

        List<Trainer> result = service.selectAll();

        assertEquals(2, result.size());
    }
}