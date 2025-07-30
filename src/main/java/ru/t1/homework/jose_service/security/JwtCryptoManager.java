package ru.t1.homework.jose_service.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtCryptoManager {

    private final RSAKey privateKey;
    private final RSAKey publicKey;
    private final long accessTtl;

    public JwtCryptoManager(
            @Value("${jwt.rsa.private-key-file}") Resource priv,
            @Value("${jwt.rsa.public-key-file}") Resource pub,
            @Value("${jwt.access-token.ttl-seconds}") long ttl
    ) throws Exception {
        // Сначала парсим в JWK, затем кастим в RSAKey
        JWK jwkPriv = JWK.parseFromPEMEncodedObjects(readAll(priv));
        JWK jwkPub  = JWK.parseFromPEMEncodedObjects(readAll(pub));

        if (!(jwkPriv instanceof RSAKey) || !(jwkPub instanceof RSAKey)) {
            throw new IllegalArgumentException("PEM-файлы не содержат RSA-ключи");
        }

        this.privateKey = (RSAKey) jwkPriv;
        this.publicKey  = (RSAKey) jwkPub;
        this.accessTtl  = ttl;
    }

    @SneakyThrows
    private String readAll(Resource res) {
        return new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public String generateAccess(String username) throws JOSEException {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(username)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(accessTtl)))
                .jwtID(UUID.randomUUID().toString())
                .build();

        // 1) JWE: шифруем payload
        EncryptedJWT jwe = new EncryptedJWT(
                new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM),
                claims
        );
        jwe.encrypt(new RSAEncrypter(publicKey.toRSAPublicKey()));
        String serializedJwe = jwe.serialize();

        // 2) JWS: подписываем JWE-строку как claim "payload"
        JWTClaimsSet wrapper = new JWTClaimsSet.Builder()
                .claim("payload", serializedJwe)
                .build();
        SignedJWT jws = new SignedJWT(
                new JWSHeader(JWSAlgorithm.RS256),
                wrapper
        );
        jws.sign(new RSASSASigner(privateKey.toRSAPrivateKey()));
        return jws.serialize();
    }

    @SneakyThrows
    public JWTClaimsSet verifyAndDecrypt(String token) {
        SignedJWT jws = SignedJWT.parse(token);
        if (!jws.verify(new RSASSAVerifier(publicKey.toRSAPublicKey()))) {
            throw new JOSEException("Invalid JWS signature");
        }
        String jweStr = jws.getJWTClaimsSet().getStringClaim("payload");
        EncryptedJWT jwe = EncryptedJWT.parse(jweStr);
        jwe.decrypt(new RSADecrypter(privateKey.toRSAPrivateKey()));
        return jwe.getJWTClaimsSet();
    }
}
