package com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.mapper;

import com.lewisrp.basemessages.backend.domain.model.Template;
import com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.entity.TemplateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TemplatePersistenceMapper {

    @Mapping(target = "variables", ignore = true)
    Template toDomain(TemplateEntity entity);

    TemplateEntity toEntity(Template domain);
}
