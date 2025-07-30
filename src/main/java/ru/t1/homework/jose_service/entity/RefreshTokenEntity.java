package ru.t1.homework.jose_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenEntity {
    @Id
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
