/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.controller;

import com.aeroresume.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author suhaib
 */
@RestController
public class UserController {
    
    @Autowired
    private UserService userService;
    
//    public ResponseEntity<UserDTO> getAllUsers() {
//        return
//    }
}
