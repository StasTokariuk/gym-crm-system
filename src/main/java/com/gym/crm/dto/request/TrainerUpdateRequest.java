package com.gym.crm.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class TrainerUpdateRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @jakarta.validation.constraints.NotNull(message = "Active status is required")
    private Boolean isActive;
}