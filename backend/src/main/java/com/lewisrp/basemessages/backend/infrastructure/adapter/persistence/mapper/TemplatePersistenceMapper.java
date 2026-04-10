package com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.mapper;

import com.lewisrp.basemessages.backend.domain.model.Template;
import com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.entity.TemplateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TemplatePersistenceMapper {

    @Mapping(target = "variables", ignore = true)
    @Mapping(source = "category", target = "category", qualifiedByName = "stringToCategory")
    @Mapping(source = "language", target = "language", qualifiedByName = "stringToLanguage")
    @Mapping(source = "status", target = "status", qualifiedByName = "stringToStatus")
    Template toDomain(TemplateEntity entity);

    @Mapping(source = "category", target = "category", qualifiedByName = "categoryToString")
    @Mapping(source = "language", target = "language", qualifiedByName = "languageToString")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    TemplateEntity toEntity(Template domain);

    @Named("stringToCategory")
    default Template.TemplateCategory stringToCategory(String category) {
        if (category == null) return null;
        return Template.TemplateCategory.valueOf(category.toUpperCase());
    }

    @Named("categoryToString")
    default String categoryToString(Template.TemplateCategory category) {
        if (category == null) return null;
        return category.name();
    }

    @Named("stringToLanguage")
    default Template.TemplateLanguage stringToLanguage(String language) {
        if (language == null) return null;
        // Handle language codes with underscores like "en_GB", "zh_CN"
        String normalized = language.toUpperCase().replace("-", "_");
        return Template.TemplateLanguage.valueOf(normalized);
    }

    @Named("languageToString")
    default String languageToString(Template.TemplateLanguage language) {
        if (language == null) return null;
        return language.name();
    }

    @Named("stringToStatus")
    default Template.TemplateStatus stringToStatus(String status) {
        if (status == null) return null;
        return Template.TemplateStatus.valueOf(status.toUpperCase());
    }

    @Named("statusToString")
    default String statusToString(Template.TemplateStatus status) {
        if (status == null) return null;
        return status.name();
    }
}
