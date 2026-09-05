package com.annasahay.service;

import com.annasahay.entity.Admin;
import com.annasahay.repository.AdminRepository;
import com.annasahay.config.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public Optional<Admin> authenticate(String username, String password) {
        String hashedPassword = PasswordUtils.hashPassword(password);
        return adminRepository.findByUsername(username)
                .filter(admin -> admin.getPassword().equals(hashedPassword));
    }

    public Admin createAdmin(Admin admin) {
        admin.setPassword(PasswordUtils.hashPassword(admin.getPassword()));
        return adminRepository.save(admin);
    }
}
