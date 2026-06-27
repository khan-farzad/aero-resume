/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.dto;

import lombok.Data;

/**
 *
 * @author suhaib
 */
@Data
public class LoginRequest {
    private String email;
    private String password;
}
