package com.example.roleauthentication.config;

import com.example.roleauthentication.entity.User;
import com.example.roleauthentication.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepo.existsByEmail("admin@taskflow.com")) return;

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@taskflow.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(User.Role.ADMIN);
        userRepo.save(admin);

        System.out.println("Default admin created → admin@taskflow.com / admin123");
    }
}