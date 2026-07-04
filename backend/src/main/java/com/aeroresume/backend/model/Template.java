/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.model;

import jakarta.persistence.*;

/**
 *
 * @author suhaib
 */
@Entity
@Table(name = "template")
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "latex_boilerplate", columnDefinition = "TEXT", nullable = false)
    private String latexBoilerplate;

    @Column(name = "preview_image_url")
    private String previewImageUrl;

    public Template() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatexBoilerplate() {
        return latexBoilerplate;
    }

    public void setLatexBoilerplate(String latexBoilerplate) {
        this.latexBoilerplate = latexBoilerplate;
    }

    public String getPreviewImageUrl() {
        return previewImageUrl;
    }

    public void setPreviewImageUrl(String previewImageUrl) {
        this.previewImageUrl = previewImageUrl;
    }
    
    
}
