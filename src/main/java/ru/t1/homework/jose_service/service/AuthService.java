package ru.t1.homework.jose_service.service;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.t1.homework.jose_service.dto.SigninDto;
import ru.t1.homework.jose_service.dto.SignupDto;
import ru.t1.homework.jose_service.dto.TokenPairDto;
import ru.t1.homework.jose_service.entity.AppUser;
import ru.t1.homework.jose_service.entity.RefreshTokenEntity;
import ru.t1.homework.jose_service.repository.RefreshTokenRepository;
import ru.t1.homework.jose_service.repository.UserRepository;
import ru.t1.homework.jose_service.security.JwtCryptoManager;
import ru.t1.homework.jose_service.security.TokenBlacklistService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtCryptoManager jwtManager;
    private final RefreshTokenRepository refreshRepo;
    private final TokenBlacklistService blacklist;

    @Value("${jwt.refresh-token.ttl-days}")
    private long refreshTtlDays;

    public void signup(SignupDto dto) {
        userRepo.findByUsername(dto.getUsername())
                .ifPresent(u -> { throw new RuntimeException("User exists"); });
        AppUser u = new AppUser();
        u.setUsername(dto.getUsername());
        u.setPasswordHash(encoder.encode(dto.getPassword()));
        userRepo.save(u);
    }

    public TokenPairDto signin(SigninDto dto) {
        AppUser u = userRepo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Bad credentials"));
        if (!encoder.matches(dto.getPassword(), u.getPasswordHash())) {
            throw new RuntimeException("Bad credentials");
        }
        try {
            String access = jwtManager.generateAccess(u.getUsername());
            RefreshTokenEntity rt = createRefresh(u.getUsername());
            return new TokenPairDto(access, rt.getToken());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RefreshTokenEntity createRefresh(String username) {
        String token = UUID.randomUUID().toString();
        Instant exp = Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS);
        return refreshRepo.save(new RefreshTokenEntity(token, username, exp));
    }

    public TokenPairDto renew(String oldRefresh) {
        RefreshTokenEntity ent = refreshRepo.findById(oldRefresh)
                .orElseThrow(() -> new RuntimeException("Invalid refresh"));
        if (ent.getExpiresAt().isBefore(Instant.now())) {
            refreshRepo.delete(ent);
            throw new RuntimeException("Refresh expired");
        }
        refreshRepo.delete(ent);
        try {
            String access = jwtManager.generateAccess(ent.getUsername());
            RefreshTokenEntity rt = createRefresh(ent.getUsername());
            return new TokenPairDto(access, rt.getToken());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void signout(String accessToken, String refreshToken) {
        try {
            JWTClaimsSet claims = jwtManager.verifyAndDecrypt(accessToken);
            blacklist.revoke(claims.getJWTID());
        } catch (Exception ignored) {}
        refreshRepo.findById(refreshToken)
                .ifPresent(refreshRepo::delete);
    }
}
