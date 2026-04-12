package com.lewisrp.basemessages.backend.adapter.persistence;

import com.lewisrp.basemessages.backend.application.port.outbound.ContactRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.Contact;
import com.lewisrp.basemessages.backend.adapter.persistence.entity.ContactEntity;
import com.lewisrp.basemessages.backend.adapter.persistence.mapper.ContactPersistenceMapper;
import com.lewisrp.basemessages.backend.adapter.persistence.repository.ContactR2dbcRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Adapter implementing the ContactRepositoryPort using R2DBC.
 */
@Component
public class ContactRepositoryAdapter implements ContactRepositoryPort {

    private static final long MVP_USER_ID = 1L;

    private final ContactR2dbcRepository contactRepository;
    private final ContactPersistenceMapper contactMapper;

    public ContactRepositoryAdapter(ContactR2dbcRepository contactRepository, ContactPersistenceMapper contactMapper) {
        this.contactRepository = contactRepository;
        this.contactMapper = contactMapper;
    }

    @Override
    public Flux<Contact> findAll(Pageable pageable) {
        return contactRepository.findAllByUserId(MVP_USER_ID, pageable)
                .map(contactMapper::toDomain);
    }

    @Override
    public Flux<Contact> search(String query, Pageable pageable) {
        return contactRepository.searchByUserId(MVP_USER_ID, query, pageable)
                .map(contactMapper::toDomain);
    }

    @Override
    public Mono<Contact> findById(Long id) {
        return contactRepository.findById(id)
                .filter(entity -> Long.valueOf(MVP_USER_ID).equals(entity.getUserId()))
                .map(contactMapper::toDomain);
    }

    @Override
    public Mono<Contact> save(Contact contact) {
        ContactEntity entity = contactMapper.toEntity(contact);

        if (entity.getUserId() == null) {
            entity.setUserId(1L);
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());

        return contactRepository.save(entity)
                .map(contactMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByPhone(Long userId, String phone) {
        return contactRepository.existsByUserIdAndPhone(userId, phone);
    }

    @Override
    public Mono<Long> countAll() {
        return contactRepository.countByUserId(MVP_USER_ID);
    }

    @Override
    public Mono<Long> countSearch(String query) {
        return contactRepository.countSearchByUserId(MVP_USER_ID, query);
    }
}
