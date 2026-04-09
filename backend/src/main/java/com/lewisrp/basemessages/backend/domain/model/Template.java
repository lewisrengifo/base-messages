package com.lewisrp.basemessages.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Template domain entity representing a WhatsApp message template.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Template {

    private Long id;
    private String name;
    private TemplateCategory category;
    private TemplateLanguage language;
    private TemplateStatus status;
    private String content;
    private List<TemplateVariable> variables;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Template categories as defined by Meta.
     */
    public enum TemplateCategory {
        MARKETING, UTILITY, AUTHENTICATION
    }

    /**
     * Template status lifecycle.
     */
    public enum TemplateStatus {
        DRAFT, PENDING, APPROVED, REJECTED
    }

    /**
     * Supported template languages.
     */
    public enum TemplateLanguage {
        EN_US, ES, PT_BR
    }

    /**
     * Check if template can be updated.
     */
    public boolean canBeUpdated() {
        return status == TemplateStatus.DRAFT || status == TemplateStatus.REJECTED;
    }

    /**
     * Check if template can be resubmitted.
     */
    public boolean canBeResubmitted() {
        return status == TemplateStatus.REJECTED;
    }

    /**
     * Transition template to PENDING status for submission.
     */
    public void submitForApproval() {
        if (status != TemplateStatus.DRAFT && status != TemplateStatus.REJECTED) {
            throw new IllegalStateException("Only DRAFT or REJECTED templates can be submitted");
        }
        this.status = TemplateStatus.PENDING;
    }

    /**
     * Mark template as approved.
     */
    public void approve() {
        this.status = TemplateStatus.APPROVED;
    }

    /**
     * Mark template as rejected with reason.
     */
    public void reject(String reason) {
        this.status = TemplateStatus.REJECTED;
        this.rejectionReason = reason;
    }
}
