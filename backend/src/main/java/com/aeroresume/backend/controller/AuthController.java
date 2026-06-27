/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.controller;

import com.aeroresume.backend.dto.*;
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
public class AuthController {
    
    @Autowired
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try{
            User user = userService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created with id: "+user.getId());
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
            HttpSession session = httpRequest.getSession(false);
            session.setAttribute("USER_ID", user.getId());
            
            return ResponseEntity.ok("Login Successful. Session ID: "+session.getId());
        }
        catch(Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}

