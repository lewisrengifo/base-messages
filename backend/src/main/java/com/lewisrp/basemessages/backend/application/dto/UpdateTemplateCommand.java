package com.lewisrp.basemessages.backend.application.dto;

import java.util.List;

/**
 * DTO for updating an existing template.
 */
public class UpdateTemplateCommand {
    private final String name;
    private final String content;
    private final List<TemplateVariableDto> variables;

    public UpdateTemplateCommand(String name, String content, List<TemplateVariableDto> variables) {
        this.name = name;
        this.content = content;
        this.variables = variables;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
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
