package com.financebuddy.backend.billsplit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantRequest {

    @NotBlank(message = "Participant name is required")
    @Size(max = 150, message = "Participant name must not exceed 150 characters")
    private String participantName;
}
