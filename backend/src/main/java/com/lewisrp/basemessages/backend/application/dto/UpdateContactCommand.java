package com.lewisrp.basemessages.backend.application.dto;

/**
 * DTO for updating a contact.
 */
public class UpdateContactCommand {
    private final String name;
    private final String phone;
    private final String email;

    public UpdateContactCommand(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
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
}
