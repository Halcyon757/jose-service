package ru.t1.homework.jose_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SigninDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
