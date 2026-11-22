package com.aeroresume.backend.templating;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ResumeDataPreparer {

    public Map<String, Object> prepare(Map<String, Object> source) {
        if (source == null) {
            throw new IllegalArgumentException("Resume data must not be null.");
        }

        return escapeMap(source);
    }

    private Map<String, Object> escapeMap(Map<?, ?> source) {
        Map<String, Object> escaped = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException("Resume data keys must be strings.");
            }
            escaped.put(key, escapeValue(entry.getValue()));
        }
        return escaped;
    }

    private Object escapeValue(Object value) {
        if (value instanceof String text) {
            return escapeLatex(text);
        }
        if (value instanceof Map<?, ?> map) {
            return escapeMap(map);
        }
        if (value instanceof Iterable<?> values) {
            List<Object> escaped = new ArrayList<>();
            for (Object item : values) {
                escaped.add(escapeValue(item));
            }
            return escaped;
        }
        if (value != null && value.getClass().isArray()) {
            List<Object> escaped = new ArrayList<>();
            for (int i = 0; i < Array.getLength(value); i++) {
                escaped.add(escapeValue(Array.get(value, i)));
            }
            return escaped;
        }
        return value;
    }

    private String escapeLatex(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            escaped.append(switch (value.charAt(i)) {
                case '&' -> "\\&";
                case '%' -> "\\%";
                case '$' -> "\\$";
                case '#' -> "\\#";
                case '_' -> "\\_";
                case '{' -> "\\{";
                case '}' -> "\\}";
                case '~' -> "\\textasciitilde{}";
                case '^' -> "\\textasciicircum{}";
                case '\\' -> "\\textbackslash{}";
                default -> value.charAt(i);
            });
        }
        return escaped.toString();
    }
}
