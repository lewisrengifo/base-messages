package com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.mapper;

import com.lewisrp.basemessages.backend.domain.model.User;
import com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

  User toDomain(UserEntity entity);
}
