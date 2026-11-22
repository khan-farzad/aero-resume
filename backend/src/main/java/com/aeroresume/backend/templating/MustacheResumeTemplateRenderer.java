package com.aeroresume.backend.templating;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.samskivert.mustache.Mustache;

@Component
public class MustacheResumeTemplateRenderer implements ResumeTemplateRenderer {
    private final Mustache.Compiler compiler = Mustache.compiler().escapeHTML(false);

    @Override
    public String render(String template, Map<String, Object> data) {
        if (template == null || template.isBlank()) {
            throw new IllegalArgumentException("LaTeX template must not be blank.");
        }
        if (data == null) {
            throw new IllegalArgumentException("Template data must not be null.");
        }

        System.out.println("Rendering template: " + template);

        return compiler.compile(template).execute(data);
    }
}
