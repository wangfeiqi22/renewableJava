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

        // Set user status based on role & driver type
        String role = user.getRole();
        if (role == null) {
            role = "individual";
            user.setRole(role);
        }

        switch (role) {
            // 企业 / 管理类用户：需要审核
            case "property":
            case "street":
            case "station":
            case "fleet":
                user.setStatus(0); // Pending
                break;
            case "driver":
                // 司机：类型A（车队司机）需要审核，类型B（个人司机）免审核
                if ("A".equalsIgnoreCase(user.getDriverType())) {
                    user.setStatus(0);
                } else {
                    user.setStatus(1);
                }
                break;
            case "individual":
            case "vip":
            default:
                // 个体户 / 个人类账号：默认直接启用
                user.setStatus(1);
                break;
        }

        user.setPasswordHash(SecurityUtil.hashPassword(user.getPasswordHash()));
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .filter(u -> SecurityUtil.verifyPassword(password, u.getPasswordHash()))
                .orElse(null);

        if (user == null) {
            return null;
        }

        // 超级管理员账号不受审核状态限制
        if ("admin".equalsIgnoreCase(user.getRole())) {
            return user;
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new RuntimeException("账号未审核通过或已被禁用，请联系管理员处理");
        }

        return user;
    }
}
