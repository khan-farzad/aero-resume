/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.dto;

/**
 *
 * @author suhaib
 */
public class TemplateResponse {
    
    private long id;
    private String name;
    private String previewImageUrl;
    
    public TemplateResponse() {}

    public TemplateResponse(long id, String name, String previewImageUrl) {
        this.id = id;
        this.name = name;
        this.previewImageUrl = previewImageUrl;
    }    

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreviewImageUrl() {
        return previewImageUrl;
    }

    public void setPreviewImageUrl(String previewImageUrl) {
        this.previewImageUrl = previewImageUrl;
    }
    
    
}
