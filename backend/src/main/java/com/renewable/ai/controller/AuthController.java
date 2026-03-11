package com.renewable.ai.controller;

import com.renewable.ai.entity.User;
import com.renewable.ai.service.UserService;
import com.renewable.ai.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.renewable.ai.util.SecurityUtil;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    /**
     * 带附件的注册接口（用于企业/清运站、司机上传营业执照/身份证/驾驶证扫描件）。
     * 前端通过 multipart/form-data 传递：
     * - user: JSON 字符串，包含基础字段
     * - businessLicense: 企业/站点营业执照
     * - companyIdCard: 企业/站点法人身份证
     * - driverIdCard: 司机身份证
     * - driverLicense: 司机驾驶证
     */
    @PostMapping(value = "/register-with-docs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> registerWithDocs(
            @RequestPart("user") User user,
            @RequestPart(name = "businessLicense", required = false) MultipartFile businessLicense,
            @RequestPart(name = "companyIdCard", required = false) MultipartFile companyIdCard,
            @RequestPart(name = "driverIdCard", required = false) MultipartFile driverIdCard,
            @RequestPart(name = "driverLicense", required = false) MultipartFile driverLicense
    ) {

        // 企业/清运站附件
        if (businessLicense != null && !businessLicense.isEmpty()) {
            String url = fileStorageService.storeUserDocument(businessLicense, "company-license");
            user.setCompanyLicenseUrl(url);
        }
        if (companyIdCard != null && !companyIdCard.isEmpty()) {
            String url = fileStorageService.storeUserDocument(companyIdCard, "company-idcard");
            user.setCompanyLegalIdCardUrl(url);
        }

        // 司机附件
        if (driverIdCard != null && !driverIdCard.isEmpty()) {
            String url = fileStorageService.storeUserDocument(driverIdCard, "driver-idcard");
            user.setDriverIdCardUrl(url);
        }
        if (driverLicense != null && !driverLicense.isEmpty()) {
            String url = fileStorageService.storeUserDocument(driverLicense, "driver-license");
            user.setDriverLicenseUrl(url);
        }

        return ResponseEntity.ok(userService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> payload) {
        User user = userService.login(payload.get("username"), payload.get("password"));
        if (user != null) {
            String token = SecurityUtil.generateToken();
            SecurityUtil.bindTokenToUser(token, user.getId());
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("user", user);
            response.put("token", token);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).build();
    }
}
