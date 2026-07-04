/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aeroresume.backend.mapper;

import com.aeroresume.backend.dto.TemplateResponse;
import com.aeroresume.backend.model.Template;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.*;

/**
 *
 * @author suhaib
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TemplateMapper {
    
    List<TemplateResponse> templatesToTemplateResponses(List<Template> templates);
    
}
