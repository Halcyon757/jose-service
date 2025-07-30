package ru.t1.homework.jose_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.t1.homework.jose_service.dto.*;
import ru.t1.homework.jose_service.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Validated SignupDto dto) {
        authService.signup(dto);
        return ResponseEntity.ok(Map.of("msg", "OK"));
    }

    @PostMapping("/signin")
    public ResponseEntity<TokenPairDto> signin(@RequestBody @Validated SigninDto dto) {
        return ResponseEntity.ok(authService.signin(dto));
    }

    @PostMapping("/renew")
    public ResponseEntity<TokenPairDto> renew(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.renew(body.get("refreshToken")));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signout(
            @RequestHeader("Authorization") String hdr,
            @RequestBody Map<String, String> body
    ) {
        String token = hdr.replaceFirst("^Bearer\\s+", "");
        authService.signout(token, body.get("refreshToken"));
        return ResponseEntity.ok(Map.of("msg", "Bye"));
    }
}
