/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.service;

import java.util.*;
import com.aeroresume.backend.model.Template;
import com.aeroresume.backend.repository.TemplateRepository;

import org.springframework.stereotype.Service;

/**
 *
 * @author suhaib
 */
@Service
public class TemplateService {
    private final TemplateRepository templateRepository;
    
    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }
    
    public List<Template> getAllTemplates() {
        List<Template> templateResponses = templateRepository.findAll();
        
        return templateResponses;
    }
}
