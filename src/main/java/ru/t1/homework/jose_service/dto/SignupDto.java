package ru.t1.homework.jose_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignupDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
