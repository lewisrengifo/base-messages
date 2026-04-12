package com.lewisrp.basemessages.backend.application.port.outbound;

import com.lewisrp.basemessages.backend.domain.model.Contact;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Outbound port for contact persistence operations.
 */
public interface ContactRepositoryPort {

    Flux<Contact> findAll(Pageable pageable);

    Flux<Contact> search(String query, Pageable pageable);

    Mono<Contact> findById(Long id);

    Mono<Contact> save(Contact contact);

    Mono<Boolean> existsByPhone(Long userId, String phone);

    Mono<Long> countAll();

    Mono<Long> countSearch(String query);
}
