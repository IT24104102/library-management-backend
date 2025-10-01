package com.amarakeerthi.userservice.config;

import com.amarakeerthi.userservice.constants.UserRole;
import com.amarakeerthi.userservice.dto.CreateAdminRequest;
import com.amarakeerthi.userservice.repositories.UserRepository;
import com.amarakeerthi.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final UserService userService;
    
    @Value("${app.admin.default.email:admin@library.com}")
    private String defaultAdminEmail;
    
    @Value("${app.admin.default.password:admin123456}")
    private String defaultAdminPassword;
    
    @Value("${app.admin.default.fullName:System Administrator}")
    private String defaultAdminName;
    
    @Value("${app.admin.default.adminId:ADMIN001}")
    private String defaultAdminId;
    
    @Value("${app.admin.default.department:IT Administration}")
    private String defaultAdminDept;
    
    @Override
    public void run(String... args) throws Exception {
        initializeAdminAccount();
    }
    
    private void initializeAdminAccount() {
        try {
            // Check if any admin account exists
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            
            if (adminCount == 0) {
                log.info("No admin account found. Creating default admin account...");
                
                CreateAdminRequest adminRequest = new CreateAdminRequest();
                adminRequest.setFullName(defaultAdminName);
                adminRequest.setEmail(defaultAdminEmail);
                adminRequest.setPassword(defaultAdminPassword);
                adminRequest.setAdminId(defaultAdminId);
                adminRequest.setDepartment(defaultAdminDept);
                adminRequest.setContactNumber("+1234567890");
                adminRequest.setPermissions("ALL");
                
                var adminResponse = userService.createAdmin(adminRequest);
                
                log.info("✅ Default admin account created successfully!");
                log.info("📧 Email: {}", defaultAdminEmail);
                log.info("🔑 Password: {}", defaultAdminPassword);
                log.info("⚠️  Please change the default password after first login!");
                log.info("👤 Admin ID: {}", adminResponse.getId());
                log.info("🆔 Admin Employee ID: {}", adminResponse.getEmployeeId());
                
            } else {
                log.info("Admin account(s) already exist. Skipping admin creation.");
            }
        } catch (Exception e) {
            log.error("❌ Failed to create default admin account", e);
            // Don't throw exception to prevent application startup failure
        }
    }
}