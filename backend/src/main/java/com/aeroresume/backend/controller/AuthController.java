/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.controller;

import com.aeroresume.backend.dto.*;
import com.aeroresume.backend.mapper.UserMapper;
import com.aeroresume.backend.model.User;
import com.aeroresume.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author suhaib
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins="http://localhost:3000")
public class AuthController {
    
    private final UserService userService;
    private final UserMapper userMapper;
    
    public AuthController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try{
            User user = userService.signup(request);
            
            UserDto userDto = userMapper.userToUserDto(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
     
    @PostMapping("/login")
    public ResponseEntity<?> Login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            //Verify user
            User user = userService.verifyLogin(request.getEmail(), request.getPassword());
            
            //Create Session
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("USER_ID", user.getId());
            
            UserDto userDto = userMapper.userToUserDto(user);
            
            return ResponseEntity.ok(userDto);
        }
        catch(Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}

