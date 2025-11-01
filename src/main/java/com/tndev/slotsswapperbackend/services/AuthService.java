package com.tndev.slotsswapperbackend.services;

import com.tndev.slotsswapperbackend.entity.User;
import com.tndev.slotsswapperbackend.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepo, JwtService jwtService) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    public void signup(String name, String email, String password) {
        if (userRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        User u = new User(name, email, hash);
        userRepo.save(u);
    }

    public String login(String email, String password) {
        User u = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!BCrypt.checkpw(password, u.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // FIX: Update to call the new generateToken signature with both email and userId
        return jwtService.generateToken(u.getEmail(), u.getId());
    }

    public User findById(String id) {
        return userRepo.findById(id).orElse(null);
    }
}