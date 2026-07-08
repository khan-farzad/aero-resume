/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.controller;

import com.aeroresume.backend.dto.JwtResponse;
import com.aeroresume.backend.Security.JwtUtils;
import com.aeroresume.backend.dto.*;
import com.aeroresume.backend.mapper.UserMapper;
import com.aeroresume.backend.model.User;
import com.aeroresume.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    
    public AuthController(UserService userService, UserMapper userMapper, 
                          AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
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
            // Let Spring Security verify the email and BCrypt password
            // (This automatically calls the UserDetailsServiceImpl class)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            
            // Officially set the user as "logged in" for this request context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generate the JWT string using their email
            String jwt = jwtUtils.generateJwtToken(authentication.getName()); // getName() returns unique String that was used to create JWT token. Here, email.
            
            // Create a clean JSON response containing the token
            JwtResponse response = new JwtResponse(jwt);
            
            return ResponseEntity.ok(response);
        }
        catch(Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}

