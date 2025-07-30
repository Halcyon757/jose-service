package ru.t1.homework.jose_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.t1.homework.jose_service.dto.UserSummaryDto;
import ru.t1.homework.jose_service.service.UserService;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserSummaryDto> me(Authentication auth) {
        return ResponseEntity.ok(userService.getProfile(auth.getName()));
    }
}
