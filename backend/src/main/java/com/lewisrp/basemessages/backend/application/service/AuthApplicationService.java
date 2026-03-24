package com.lewisrp.basemessages.backend.application.service;

import com.lewisrp.basemessages.backend.application.dto.LoginCommand;
import com.lewisrp.basemessages.backend.application.dto.LoginResult;
import com.lewisrp.basemessages.backend.application.port.inbound.AuthUseCase;
import com.lewisrp.basemessages.backend.application.port.outbound.PasswordEncoderPort;
import com.lewisrp.basemessages.backend.application.port.outbound.RefreshTokenStorePort;
import com.lewisrp.basemessages.backend.application.port.outbound.TokenProviderPort;
import com.lewisrp.basemessages.backend.application.port.outbound.UserRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Application service implementing authentication use cases.
 * This service orchestrates the authentication flow using the defined ports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService implements AuthUseCase {
    
    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final RefreshTokenStorePort refreshTokenStore;
    
    @Override
    public Mono<LoginResult> login(LoginCommand command) {
        log.debug("Attempting login for user: {}", command.getEmail());
        
        return userRepository.findByEmail(command.getEmail())
                .flatMap(user -> {
                    log.info("User found: {}", user.getEmail());
                    return authenticateUser(user, command.getPassword());
                })
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid email or password")));
    }
    
    @Override
    public Mono<Void> logout(String refreshToken) {
        log.debug("Processing logout");
        
        return Mono.fromRunnable(() -> {
            if (refreshToken != null && !refreshToken.isEmpty()) {
                refreshTokenStore.revoke(refreshToken);
                log.info("Refresh token revoked successfully");
            }
        });
    }
    
    private Mono<LoginResult> authenticateUser(User user, String rawPassword) {
        if (!user.canLogin()) {
            log.info("User account is disabled: {}", user.getEmail());
            return Mono.error(new InvalidCredentialsException("Account is disabled"));
        }
        
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.info("Invalid password for user: {}", user.getEmail());
            return Mono.error(new InvalidCredentialsException("Invalid email or password"));
        }
        
        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        log.info("Tokens generated for user: {}", user.getEmail());
        // Store refresh token server-side (7 days = 604800 seconds)
        refreshTokenStore.store(refreshToken, user.getId(), 604800);
        
        log.info("User logged in successfully: {}", user.getEmail());
        
        LoginResult.UserInfo userInfo = new LoginResult.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAvatarUrl()
        );
        
        LoginResult result = new LoginResult(
                accessToken,
                refreshToken,
                "Bearer",
                900L, // 15 minutes in seconds
                userInfo
        );
        
        return Mono.just(result);
    }
    
    /**
     * Custom exception for authentication failures.
     */
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }
}
