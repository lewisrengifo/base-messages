package com.lewisrp.basemessages.backend.application.dto;

import java.time.LocalDateTime;

/**
 * DTO for contact response.
 */
public class ContactDto {
    private final Long id;
    private final String name;
    private final String phone;
    private final String email;
    private final String initials;
    private final String color;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ContactDto(
            Long id,
            String name,
            String phone,
            String email,
            String initials,
            String color,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.initials = initials;
        this.color = color;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getInitials() {
        return initials;
    }

    public String getColor() {
        return color;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
