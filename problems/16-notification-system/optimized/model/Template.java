/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Template.java — Notification template with variable substitution

import java.util.Map;

public class Template {
    private String templateId;
    private String name;
    private String bodyTemplate;

    public Template(String templateId, String name, String bodyTemplate) {
        this.templateId = templateId;
        this.name = name;
        this.bodyTemplate = bodyTemplate;
    }

    public String render(Map<String, String> variables) {
        String result = bodyTemplate;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    public String getTemplateId() { return templateId; }
    public String getName() { return name; }
    public String getBodyTemplate() { return bodyTemplate; }

    @Override
    public String toString() { return String.format("Template[%s: %s]", templateId, name); }
}
