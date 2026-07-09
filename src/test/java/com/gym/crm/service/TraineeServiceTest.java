package com.gym.crm.service;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.model.Trainee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private TraineeService service;

    @Test
    @DisplayName("create generates username, password and sets active flag")
    void create_generatesUsernameAndPassword() {
        when(userProfileService.buildUsername(eq("John"), eq("Smith"), any(Predicate.class))).thenReturn("John.Smith");
        when(userProfileService.generatePassword()).thenReturn("1234567890");
        when(passwordEncoder.encode("1234567890")).thenReturn("encodedPassword");
        when(traineeDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trainee t = new Trainee("John", "Smith", LocalDate.of(2000, 1, 1), "Kyiv");
        Trainee result = service.create(t);

        assertEquals("John.Smith", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.isActive());

        verify(userProfileService).buildUsername(eq("John"), eq("Smith"), any(Predicate.class));
        verify(traineeDao).save(t);
    }

    @Test
    @DisplayName("create adds serial suffix on username collision")
    void create_addsSerialSuffixOnCollision() {
        when(userProfileService.buildUsername(eq("John"), eq("Smith"), any(Predicate.class))).thenReturn("John.Smith1");
        when(userProfileService.generatePassword()).thenReturn("1234567890");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(traineeDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trainee t = new Trainee("John", "Smith", LocalDate.of(2000, 1, 1), "Kyiv");
        Trainee result = service.create(t);

        assertEquals("John.Smith1", result.getUsername());
        verify(traineeDao).save(t);
    }

    @Test
    @DisplayName("create adds multiple serial suffixes on multiple collisions")
    void create_addsMultipleSerialSuffixes() {
        when(userProfileService.buildUsername(eq("John"), eq("Smith"), any(Predicate.class))).thenReturn("John.Smith3");
        when(userProfileService.generatePassword()).thenReturn("1234567890");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(traineeDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trainee t = new Trainee("John", "Smith", LocalDate.of(2000, 1, 1), "Kyiv");
        Trainee result = service.create(t);

        assertEquals("John.Smith3", result.getUsername());
        verify(traineeDao).save(t);
    }

    @Test
    @DisplayName("update updates an existing trainee")
    void update_existingTrainee() {
        Trainee t = new Trainee();
        t.setTraineeId(1L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(t));
        when(traineeDao.save(t)).thenReturn(t);

        Trainee result = service.update(t);

        assertEquals(1L, result.getTraineeId());
        verify(traineeDao).save(t);
    }

    @Test
    @DisplayName("update throws when trainee not found")
    void update_throwsWhenNotFound() {
        Trainee t = new Trainee();
        t.setTraineeId(99L);
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.update(t));
        verify(traineeDao, never()).save(any());
    }

    @Test
    @DisplayName("update throws when id is null")
    void update_throwsWhenIdNull() {
        Trainee t = new Trainee();
        assertThrows(IllegalArgumentException.class, () -> service.update(t));
    }

    @Test
    @DisplayName("delete delegates the call to DAO")
    void delete_delegatesToDao() {
        service.delete(5L);
        verify(traineeDao).deleteById(5L);
    }

    @Test
    @DisplayName("select returns trainee by id")
    void select_returnsTrainee() {
        Trainee t = new Trainee();
        t.setTraineeId(1L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(t));

        Optional<Trainee> result = service.select(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getTraineeId());
    }

    @Test
    @DisplayName("select returns empty Optional when not found")
    void select_returnsEmptyWhenNotFound() {
        when(traineeDao.findById(404L)).thenReturn(Optional.empty());

        Optional<Trainee> result = service.select(404L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("selectAll returns all trainees")
    void selectAll_returnsAll() {
        when(traineeDao.findAll()).thenReturn(List.of(new Trainee(), new Trainee()));

        List<Trainee> result = service.selectAll();

        assertEquals(2, result.size());
    }
}