/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.service;

import com.aeroresume.backend.Repository.UserRepository;
import com.aeroresume.backend.dto.SignupRequest;
import com.aeroresume.backend.model.User;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * @author suhaib
 */
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    public User signup(SignupRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists!");
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))    //Hash Password
                .build();
        
        return userRepository.save(user);
    }
    
    public User verifyLogin(String email, String rawPassword) {
        
        Optional<User> userOptional;
        userOptional = userRepository.findByEmail(email);
        
        User user = userOptional.orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        //Comparing raw password with database hashed password
        if(!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password!");
        }
        
        return user;
    }
}
