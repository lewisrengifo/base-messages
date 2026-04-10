package com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.converter;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Component;

/**
 * R2DBC converters for PostgreSQL enum types.
 * Handles conversion between Java Strings and PostgreSQL custom enum types.
 */
@Component
public class EnumConverters {

    /**
     * Converter for reading template_category enum from PostgreSQL.
     */
    @ReadingConverter
    public static class TemplateCategoryReadConverter implements Converter<Row, String> {
        @Override
        public String convert(Row source) {
            return source.get("category", String.class);
        }
    }

    /**
     * Converter for writing template_category enum to PostgreSQL.
     */
    @WritingConverter
    public static class TemplateCategoryWriteConverter implements Converter<String, Parameter> {
        @Override
        public Parameter convert(String source) {
            // Cast the string to the PostgreSQL enum type
            return Parameter.from("CAST('" + source + "' AS template_category)");
        }
    }

    /**
     * Converter for reading template_status enum from PostgreSQL.
     */
    @ReadingConverter
    public static class TemplateStatusReadConverter implements Converter<Row, String> {
        @Override
        public String convert(Row source) {
            return source.get("status", String.class);
        }
    }

    /**
     * Converter for writing template_status enum to PostgreSQL.
     */
    @WritingConverter
    public static class TemplateStatusWriteConverter implements Converter<String, Parameter> {
        @Override
        public Parameter convert(String source) {
            return Parameter.from("CAST('" + source + "' AS template_status)");
        }
    }
}
