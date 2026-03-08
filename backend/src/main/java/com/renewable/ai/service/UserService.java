package com.renewable.ai.service;

import com.renewable.ai.exception.RegistrationException;
import com.renewable.ai.entity.User;
import com.renewable.ai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.renewable.ai.util.SecurityUtil;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RegistrationException("用户名已存在");
        }

        // Set user status based on role
        switch (user.getRole()) {
            case "property":
            case "street":
            case "station":
            case "fleet":
                user.setStatus(0); // 0: Pending for audit
                break;
            case "individual":
            case "driver":
            case "vip":
            default:
                user.setStatus(1); // 1: Active
                break;
        }

        user.setPasswordHash(SecurityUtil.hashPassword(user.getPasswordHash()));
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> SecurityUtil.verifyPassword(password, u.getPasswordHash()))
                .orElse(null);
    }
}
