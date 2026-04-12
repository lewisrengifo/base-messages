package com.lewisrp.basemessages.backend.adapter.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * R2DBC entity for the templates table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("templates")
public class TemplateEntity {

    @Id
    private Long id;

    private String name;

    private String category;

    private String language;

    private String status;

    private String content;

    private String rejectionReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
