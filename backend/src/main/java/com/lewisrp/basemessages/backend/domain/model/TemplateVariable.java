package com.lewisrp.basemessages.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Template variable domain entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVariable {

    private Integer position;
    private String example;
}
