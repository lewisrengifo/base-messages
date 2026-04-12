package com.lewisrp.basemessages.backend.adapter.persistence.mapper;

import com.lewisrp.basemessages.backend.domain.model.Contact;
import com.lewisrp.basemessages.backend.adapter.persistence.entity.ContactEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContactPersistenceMapper {

    Contact toDomain(ContactEntity entity);

    ContactEntity toEntity(Contact domain);
}
