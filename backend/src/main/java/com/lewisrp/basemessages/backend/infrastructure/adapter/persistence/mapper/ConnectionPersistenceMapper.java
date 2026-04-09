package com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.mapper;

import com.lewisrp.basemessages.backend.domain.model.Connection;
import com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.entity.ConnectionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConnectionPersistenceMapper {

    Connection toDomain(ConnectionEntity entity);

    ConnectionEntity toEntity(Connection domain);
}
