package com.example.roleauthentication.service;
import com.example.roleauthentication.JwtUtil;
import com.example.roleauthentication.dto.Dto;
import com.example.roleauthentication.entity.User;
import com.example.roleauthentication.exception.AppException;
import com.example.roleauthentication.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Dto.AuthResponse register(Dto.RegisterRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new AppException("Email already registered");
        }

        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(req.email().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(User.Role.USER);

        userRepo.save(user);
        log.info("New user registered: {}", user.getEmail());

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return Dto.AuthResponse.of(token, user);
    }

    public Dto.AuthResponse login(Dto.LoginRequest req) {
        User user = userRepo.findByEmail(req.email().toLowerCase().trim())
                .orElseThrow(() -> new AppException("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new AppException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        log.info("User logged in: {}", user.getEmail());
        return Dto.AuthResponse.of(token, user);
    }

    public Dto.UserResponse getProfile(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found"));
        return Dto.UserResponse.from(user);
    }
}
