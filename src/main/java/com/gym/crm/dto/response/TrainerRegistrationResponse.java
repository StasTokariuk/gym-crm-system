package com.gym.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TrainerRegistrationResponse {
    private String username;
    private String password;
}