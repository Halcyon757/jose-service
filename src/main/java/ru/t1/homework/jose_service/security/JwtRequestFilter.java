package ru.t1.homework.jose_service.security;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.t1.homework.jose_service.service.UserService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtCryptoManager crypto;
    private final TokenBlacklistService blacklist;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                JWTClaimsSet claims = crypto.verifyAndDecrypt(token);
                String jti = claims.getJWTID();
                if (!blacklist.isRevoked(jti)) {
                    var userDetails = userService.loadUserByUsername(
                            claims.getSubject()
                    );
                    var authentication = new org.springframework.security.authentication.
                            UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            } catch (Exception ignored) {}
        }
        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getServletPath();
        return path.startsWith("/auth")
                || path.startsWith("/api/docs")
                || path.startsWith("/v3/api-docs");
    }
}
