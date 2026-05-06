package com.lewisrp.basemessages.backend.adapter.persistence.mapper;

import com.lewisrp.basemessages.backend.adapter.persistence.entity.CampaignEntity;
import com.lewisrp.basemessages.backend.domain.model.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampaignPersistenceMapper {

    @Mapping(target = "status", expression = "java(toCampaignStatus(entity.getStatus()))")
    @Mapping(target = "audienceType", expression = "java(toAudienceType(entity.getAudienceType()))")
    @Mapping(target = "scheduleType", expression = "java(toScheduleType(entity.getScheduleType()))")
    Campaign toDomain(CampaignEntity entity);

    @Mapping(target = "status", expression = "java(toDbValue(domain.getStatus()))")
    @Mapping(target = "audienceType", expression = "java(toDbValue(domain.getAudienceType()))")
    @Mapping(target = "scheduleType", expression = "java(toDbValue(domain.getScheduleType()))")
    CampaignEntity toEntity(Campaign domain);

    default String toDbValue(Enum<?> value) {
        return value != null ? value.name().toLowerCase() : null;
    }

    default Campaign.CampaignStatus toCampaignStatus(String value) {
        return value != null ? Campaign.CampaignStatus.valueOf(value.toUpperCase()) : null;
    }

    default Campaign.AudienceType toAudienceType(String value) {
        return value != null ? Campaign.AudienceType.valueOf(value.toUpperCase()) : null;
    }

    default Campaign.ScheduleType toScheduleType(String value) {
        return value != null ? Campaign.ScheduleType.valueOf(value.toUpperCase()) : null;
    }
}
