package ru.t1.homework.jose_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenPairDto {
    private String accessToken;
    private String refreshToken;
}
