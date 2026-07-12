/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.service;

import com.aeroresume.backend.dto.SignupRequest;
import com.aeroresume.backend.dto.UserDto;
import com.aeroresume.backend.mapper.UserMapper;
import com.aeroresume.backend.model.User;
import com.aeroresume.backend.repository.UserRepository;
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
    private final UserMapper userMapper;
    
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userMapper = userMapper;
    }
    
    public UserDto signup(SignupRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists!");
        }
        
        // 2. Hash the password using the manual getter
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        User user = new User(
                request.getEmail(), 
                hashedPassword, 
                request.getFirstName(), 
                request.getLastName()
        );
        userRepository.save(user);
        UserDto userDto = userMapper.userToUserDto(user);
        return userDto;
    }
    
    public UserDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
            
        UserDto userDto = userMapper.userToUserDto(user);

        return userDto;
    }

    public UserDto updateUserProfile(String email, UserDto updatedUserDto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setFirstName(updatedUserDto.getFirstName());
        user.setLastName(updatedUserDto.getLastName());
        user.setMasterJsonData(updatedUserDto.getMasterJsonData());

        userRepository.save(user);

        return userMapper.userToUserDto(user);
    }
}
