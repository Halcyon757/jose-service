package ru.t1.homework.jose_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import ru.t1.homework.jose_service.dto.UserSummaryDto;
import ru.t1.homework.jose_service.entity.AppUser;
import ru.t1.homework.jose_service.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        AppUser u = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Not found"));
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(), u.getPasswordHash(), java.util.Collections.emptyList()
        );
    }

    public UserSummaryDto getProfile(String username) {
        AppUser u = userRepo.findByUsername(username).orElseThrow();
        return new UserSummaryDto(u.getId(), u.getUsername());
    }
}
