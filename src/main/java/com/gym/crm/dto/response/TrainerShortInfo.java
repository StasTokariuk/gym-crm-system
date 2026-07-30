package com.gym.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TrainerShortInfo {
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
}