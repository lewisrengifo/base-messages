package com.lewisrp.basemessages.backend.infrastructure.adapter.persistence;

import com.lewisrp.basemessages.backend.application.port.outbound.UserRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.User;
import com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.entity.UserEntity;
import com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.repository.UserR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Adapter implementing the UserRepositoryPort using R2DBC.
 * This is the infrastructure implementation of the persistence port.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserR2dbcRepository r2dbcRepository;

    @Override
    public Mono<User> findByEmail(String email) {
        return r2dbcRepository.findByEmail(email)
                .map(this::toDomain);
    }

    @Override
    public Mono<User> findById(Long id) {
        return r2dbcRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Mono<User> save(User user) {
        UserEntity entity = toEntity(user);
        return r2dbcRepository.save(entity)
                .map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return r2dbcRepository.existsByEmail(email);
    }

    private User toDomain(UserEntity entity) {
        User user = new User();
        user.setId(entity.getId());
        user.setEmail(entity.getEmail());
        user.setPasswordHash(entity.getPasswordHash());
        user.setName(entity.getName());
        user.setAvatarUrl(entity.getAvatarUrl());
        user.setActive(entity.getActive() != null ? entity.getActive() : true);
        return user;
    }

    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setName(user.getName());
        entity.setAvatarUrl(user.getAvatarUrl());
        entity.setActive(user.isActive());
        return entity;
    }
}
