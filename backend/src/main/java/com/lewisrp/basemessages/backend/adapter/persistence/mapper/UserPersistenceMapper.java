package com.lewisrp.basemessages.backend.adapter.persistence.mapper;

import com.lewisrp.basemessages.backend.domain.model.User;
import com.lewisrp.basemessages.backend.adapter.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

  User toDomain(UserEntity entity);
}
