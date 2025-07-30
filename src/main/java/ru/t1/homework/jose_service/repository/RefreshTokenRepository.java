package ru.t1.homework.jose_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.homework.jose_service.entity.RefreshTokenEntity;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {
}
