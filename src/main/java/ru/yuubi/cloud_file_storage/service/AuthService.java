package ru.yuubi.cloud_file_storage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.yuubi.cloud_file_storage.repository.UserRepository;
import ru.yuubi.cloud_file_storage.entity.User;
import ru.yuubi.cloud_file_storage.exception.UserAlreadyExistsException;
import ru.yuubi.cloud_file_storage.service.security.CustomUserDetails;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void createUser(String login, String password) {
        if (userRepository.findByLogin(login).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(login, encodedPassword);
        userRepository.save(user);
    }

    public Integer getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getId();
        }
        return null;
    }
}
