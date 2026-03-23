package com.lewisrp.basemessages.backend.infrastructure.adapter.web.mapper;

import com.lewisrp.basemessages.backend.application.dto.LoginCommand;
import com.lewisrp.basemessages.backend.application.dto.LoginResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.model.LoginRequest;
import org.openapitools.model.LoginResponse;
import org.openapitools.model.User;

/**
 * MapStruct mapper for authentication-related conversions.
 */
@Mapper(componentModel = "spring")
public interface AuthMapper {

    /**
     * Convert OpenAPI LoginRequest to application LoginCommand.
     */
    LoginCommand toLoginCommand(LoginRequest loginRequest);

    /**
     * Convert application LoginResult to OpenAPI LoginResponse.
     */
    @Mapping(target = "token", source = "accessToken")
    @Mapping(target = "user", source = "user")
    LoginResponse toLoginResponse(LoginResult loginResult);

    /**
     * Convert LoginResult.UserInfo to OpenAPI User.
     */
    @Mapping(target = "avatar", expression = "java(userInfo.getAvatarUrl() != null ? java.net.URI.create(userInfo.getAvatarUrl()) : null)")
    User toOpenApiUser(LoginResult.UserInfo userInfo);
}
