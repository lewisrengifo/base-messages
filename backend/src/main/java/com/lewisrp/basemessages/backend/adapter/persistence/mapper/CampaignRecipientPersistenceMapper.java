package com.lewisrp.basemessages.backend.adapter.persistence.mapper;

import com.lewisrp.basemessages.backend.adapter.persistence.entity.CampaignRecipientEntity;
import com.lewisrp.basemessages.backend.domain.model.CampaignRecipient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampaignRecipientPersistenceMapper {

    @Mapping(target = "status", expression = "java(toMessageStatus(entity.getStatus()))")
    CampaignRecipient toDomain(CampaignRecipientEntity entity);

    @Mapping(target = "status", expression = "java(toDbValue(domain.getStatus()))")
    CampaignRecipientEntity toEntity(CampaignRecipient domain);

    default String toDbValue(Enum<?> value) {
        return value != null ? value.name().toLowerCase() : null;
    }

    default CampaignRecipient.MessageStatus toMessageStatus(String value) {
        return value != null ? CampaignRecipient.MessageStatus.valueOf(value.toUpperCase()) : null;
    }
}
