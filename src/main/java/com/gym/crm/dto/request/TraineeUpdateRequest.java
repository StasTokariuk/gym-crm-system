package com.gym.crm.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
public class TraineeUpdateRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private LocalDate dateOfBirth;
    private String address;

    @jakarta.validation.constraints.NotNull(message = "Active status is required")
    private Boolean isActive;
}