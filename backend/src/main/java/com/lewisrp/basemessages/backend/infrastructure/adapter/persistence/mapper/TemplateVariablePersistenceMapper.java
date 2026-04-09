package com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.mapper;

import com.lewisrp.basemessages.backend.domain.model.TemplateVariable;
import com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.entity.TemplateVariableEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TemplateVariablePersistenceMapper {

    TemplateVariable toDomain(TemplateVariableEntity entity);

    TemplateVariableEntity toEntity(TemplateVariable domain);
}
