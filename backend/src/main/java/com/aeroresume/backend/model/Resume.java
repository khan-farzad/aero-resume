/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.model;

import jakarta.persistence.*;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 *
 * @author suhaib
 */
@Entity
@Table(name = "resume")
public class Resume {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", referencedColumnName = "id")
    private Template template;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_json_data", columnDefinition = "json")
    private Map<String, Object> rawJsonData;

    @Column(name = "latex_content", columnDefinition = "TEXT")
    private String latexContent;

    @Column(name = "original_file_url")
    private String originalFileUrl;

    @Column(name = "final_export_url")
    private String finalExportUrl;

    public Resume() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public Map<String, Object> getRawJsonData() {
        return rawJsonData;
    }

    public void setRawJsonData(Map<String, Object> rawJsonData) {
        this.rawJsonData = rawJsonData;
    }

    public String getLatexContent() {
        return latexContent;
    }

    public void setLatexContent(String latexContent) {
        this.latexContent = latexContent;
    }

    public String getOriginalFileUrl() {
        return originalFileUrl;
    }

    public void setOriginalFileUrl(String originalFileUrl) {
        this.originalFileUrl = originalFileUrl;
    }

    public String getFinalExportUrl() {
        return finalExportUrl;
    }

    public void setFinalExportUrl(String finalExportUrl) {
        this.finalExportUrl = finalExportUrl;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
}
