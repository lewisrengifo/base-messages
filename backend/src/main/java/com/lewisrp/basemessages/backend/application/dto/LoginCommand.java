package com.lewisrp.basemessages.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command object for login requests.
 * This is an immutable DTO used to pass login credentials from the infrastructure to the application layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginCommand {

    private String email;
    private String password;
}
