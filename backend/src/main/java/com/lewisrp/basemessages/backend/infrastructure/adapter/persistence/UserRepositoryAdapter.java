package com.lewisrp.basemessages.backend.infrastructure.adapter.persistence;

import com.lewisrp.basemessages.backend.application.port.outbound.UserRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.User;
import com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.entity.UserEntity;
import com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.mapper.UserPersistenceMapper;
import com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.repository.UserR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Adapter implementing the UserRepositoryPort using R2DBC. This is the infrastructure
 * implementation of the persistence port.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryAdapter implements UserRepositoryPort {

  private final UserR2dbcRepository r2dbcRepository;
  private final UserPersistenceMapper userPersistenceMapper;

  @Override
  public Mono<User> findByEmail(String email) {
    return r2dbcRepository
        .findByEmail(email)
        .doOnSubscribe(s -> log.debug("findByEmail started for email={}", email))
        .doOnNext(entity -> log.info("User found: {}", entity.getEmail()))
        .switchIfEmpty(
            Mono.defer(
                () -> {
                  log.warn("No user found for email={}", email);
                  return Mono.empty();
                }))
        .map(userPersistenceMapper::toDomain)
        .doOnSuccess(
            user -> log.debug("findByEmail completed for email={}, found={}", email, user != null))
        .doOnError(error -> log.error("findByEmail failed for email={}", email, error));
  }

  @Override
  public Mono<User> findById(Long id) {
    return r2dbcRepository.findById(id).map(userPersistenceMapper::toDomain);
  }

  @Override
  public Mono<User> save(User user) {
    UserEntity entity = toEntity(user);
    return r2dbcRepository.save(entity).map(userPersistenceMapper::toDomain);
  }

  @Override
  public Mono<Boolean> existsByEmail(String email) {
    return r2dbcRepository.existsByEmail(email);
  }


  private UserEntity toEntity(User user) {
    UserEntity entity = new UserEntity();
    entity.setId(user.getId());
    entity.setEmail(user.getEmail());
    entity.setPasswordHash(user.getPasswordHash());
    entity.setName(user.getName());
    entity.setAvatarUrl(user.getAvatarUrl());
    entity.setIsActive(user.isActive());
    return entity;
  }
}
