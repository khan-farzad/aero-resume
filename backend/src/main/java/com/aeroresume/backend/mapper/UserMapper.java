/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.mapper;

import com.aeroresume.backend.dto.SignupRequest;
import com.aeroresume.backend.dto.UserDto;
import com.aeroresume.backend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 *
 * @author suhaib
 */
// componentModel = "spring" tells MapStruct to make this a standard Spring @Component
// unmappedTargetPolicy = ReportingPolicy.IGNORE tells MapStruct not to complain about missing fields
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    
    // 1. Map incoming Request DTO to Entity
    User signupRequestToUser(SignupRequest request);
    
    // 2. Map outgoing Entity to Response DTO
    UserDto userToUserDto(User user);
}
