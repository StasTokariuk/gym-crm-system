package com.gym.crm.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class TrainerProfileResponse {
    private String firstName;
    private String lastName;
    private String specialization;
    private boolean isActive;
    private List<TraineeShortInfo> trainees;
}