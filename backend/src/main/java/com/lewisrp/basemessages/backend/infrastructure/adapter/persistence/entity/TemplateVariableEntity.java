package com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * R2DBC entity for template variables.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("template_variables")
public class TemplateVariableEntity {

    @Id
    private Long id;

    private Long templateId;

    private Integer position;

    private String example;
}
