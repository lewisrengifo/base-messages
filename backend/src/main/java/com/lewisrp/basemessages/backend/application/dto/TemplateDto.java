package com.lewisrp.basemessages.backend.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for template response.
 */
public class TemplateDto {
    private final Long id;
    private final String name;
    private final String category;
    private final String language;
    private final String status;
    private final String content;
    private final List<TemplateVariableDto> variables;
    private final String rejectionReason;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TemplateDto(Long id, String name, String category, String language, 
                       String status, String content, List<TemplateVariableDto> variables,
                       String rejectionReason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.language = language;
        this.status = status;
        this.content = content;
        this.variables = variables;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getLanguage() {
        return language;
    }

    public String getStatus() {
        return status;
    }

    public String getContent() {
        return content;
    }

    public List<TemplateVariableDto> getVariables() {
        return variables;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static class TemplateVariableDto {
        private final Integer position;
        private final String example;

        public TemplateVariableDto(Integer position, String example) {
            this.position = position;
            this.example = example;
        }

        public Integer getPosition() {
            return position;
        }

        public String getExample() {
            return example;
        }
    }
}
