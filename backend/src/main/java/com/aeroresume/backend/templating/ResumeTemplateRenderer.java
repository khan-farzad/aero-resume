package com.aeroresume.backend.templating;

import java.util.Map;

public interface ResumeTemplateRenderer {
    String render(String template, Map<String, Object> data);
}
