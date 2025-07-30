package ru.t1.homework.jose_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.homework.jose_service.entity.AppUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}
