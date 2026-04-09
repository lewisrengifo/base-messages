package com.lewisrp.basemessages.backend.application.dto;

import java.util.List;

/**
 * DTO for creating a new template.
 */
public class CreateTemplateCommand {
    private final String name;
    private final String category;
    private final String content;
    private final String language;
    private final List<TemplateVariableDto> variables;

    public CreateTemplateCommand(String name, String category, String content, 
                                 String language, List<TemplateVariableDto> variables) {
        this.name = name;
        this.category = category;
        this.content = content;
        this.language = language;
        this.variables = variables;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }

    public String getLanguage() {
        return language;
    }

    public List<TemplateVariableDto> getVariables() {
        return variables;
    }

    public static class TemplateVariableDto {
        private final String example;

        public TemplateVariableDto(String example) {
            this.example = example;
        }

        public String getExample() {
            return example;
        }
    }
}
