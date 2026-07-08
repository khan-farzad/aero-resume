/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 *
 * @author suhaib
 */
@Entity
@Table(name = "user")
public class User {
        
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name= "email", nullable = false, unique = true)
    private String email;
    
    @Column(name= "password", nullable = false)
    private String password;
    
    @Column(name= "first_name", nullable = false)
    private String firstName;
    
    @Column(name= "last_name", nullable = false)
    private String lastName;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resume> resumes;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "master_json_data", columnDefinition = "json")
    private Map<String, Object> masterJsonData;
    
    @CreationTimestamp
    @Column(name= "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
    
    public User() {}

    public User(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public List<Resume> getResumes() {
        return resumes;
    }

    public void setResumes(List<Resume> resumes) {
        this.resumes = resumes;
    }

    public Map<String, Object> getMasterJsonData() {
        return masterJsonData;
    }

    public void setMasterJsonData(Map<String, Object> masterJsonData) {
        this.masterJsonData = masterJsonData;
    }
    
}
