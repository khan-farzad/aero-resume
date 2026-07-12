/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.controller;

import com.aeroresume.backend.dto.TemplateResponse;
import com.aeroresume.backend.mapper.TemplateMapper;
import com.aeroresume.backend.model.Template;
import com.aeroresume.backend.service.TemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/**
 *
 * @author suhaib
 */
@RestController
@RequestMapping("/api/template")
public class TemplateController {
    
    private final TemplateService templateService;
    private final TemplateMapper templateMapper;

    public TemplateController(TemplateService templateService, TemplateMapper templateMapper) {
        this.templateService = templateService;
        this.templateMapper = templateMapper;
    }
    
    @GetMapping("/")
    public ResponseEntity<List<TemplateResponse>> getTemplates() {
        List<Template> templates = templateService.getAllTemplates();
        List<TemplateResponse> templateResponses = templateMapper.templatesToTemplateResponses(templates);
        
        return ResponseEntity.ok(templateResponses);
    }
}
