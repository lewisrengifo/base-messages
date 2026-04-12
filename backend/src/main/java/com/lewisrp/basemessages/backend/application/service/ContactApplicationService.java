package com.lewisrp.basemessages.backend.application.service;

import com.lewisrp.basemessages.backend.application.dto.ContactDto;
import com.lewisrp.basemessages.backend.application.dto.ContactPageDto;
import com.lewisrp.basemessages.backend.application.dto.CreateContactCommand;
import com.lewisrp.basemessages.backend.application.port.outbound.ContactRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.Contact;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Application service for contact management.
 */
@Service
public class ContactApplicationService {

    private static final long MVP_USER_ID = 1L;

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");
    private static final List<String> COLOR_PALETTE = List.of(
            "bg-blue-100 text-blue-700",
            "bg-indigo-100 text-indigo-700",
            "bg-sky-100 text-sky-700",
            "bg-emerald-100 text-emerald-700",
            "bg-amber-100 text-amber-700",
            "bg-rose-100 text-rose-700"
    );

    private final ContactRepositoryPort contactRepository;

    public ContactApplicationService(ContactRepositoryPort contactRepository) {
        this.contactRepository = contactRepository;
    }

    public Mono<ContactDto> createContact(CreateContactCommand command) {
        String normalizedName = command.getName() != null ? command.getName().trim() : "";
        String normalizedPhone = command.getPhone() != null ? command.getPhone().trim() : "";
        String normalizedEmail = command.getEmail() != null && !command.getEmail().isBlank()
                ? command.getEmail().trim()
                : null;

        if (normalizedName.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Contact name is required"));
        }
        if (!E164_PATTERN.matcher(normalizedPhone).matches()) {
            return Mono.error(new IllegalArgumentException("Phone must be in E.164 format (e.g. +15550123456)"));
        }

        return contactRepository.existsByPhone(MVP_USER_ID, normalizedPhone)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalStateException("Contact with this phone number already exists"));
                    }

                    Contact contact = Contact.builder()
                            .userId(MVP_USER_ID)
                            .name(normalizedName)
                            .phone(normalizedPhone)
                            .email(normalizedEmail)
                            .initials(generateInitials(normalizedName))
                            .color(pickColor(normalizedName))
                            .build();

                    return contactRepository.save(contact).map(this::toDto);
                });
    }

    public Mono<ContactPageDto> listContacts(String search, Pageable pageable) {
        boolean hasSearch = search != null && !search.isBlank();
        String normalizedSearch = hasSearch ? search.trim() : null;

        Mono<java.util.List<ContactDto>> dataMono = (hasSearch
                ? contactRepository.search(normalizedSearch, pageable)
                : contactRepository.findAll(pageable))
                .map(this::toDto)
                .collectList();

        Mono<Long> totalMono = hasSearch
                ? contactRepository.countSearch(normalizedSearch)
                : contactRepository.countAll();

        return Mono.zip(dataMono, totalMono)
                .map(tuple -> new ContactPageDto(tuple.getT1(), tuple.getT2()));
    }

    public Mono<ContactDto> getContact(Long id) {
        return contactRepository.findById(id)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new NotFoundException("Contact not found with id: " + id)));
    }

    private ContactDto toDto(Contact contact) {
        return new ContactDto(
                contact.getId(),
                contact.getName(),
                contact.getPhone(),
                contact.getEmail(),
                contact.getInitials(),
                contact.getColor(),
                contact.getCreatedAt(),
                contact.getUpdatedAt()
        );
    }

    private String generateInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0) {
            return "NA";
        }
        if (parts.length == 1) {
            String token = parts[0];
            return token.substring(0, Math.min(2, token.length())).toUpperCase(Locale.ROOT);
        }
        char first = Character.toUpperCase(parts[0].charAt(0));
        char last = Character.toUpperCase(parts[parts.length - 1].charAt(0));
        return new String(new char[]{first, last});
    }

    private String pickColor(String name) {
        int idx = Math.abs(name.toLowerCase(Locale.ROOT).hashCode()) % COLOR_PALETTE.size();
        return COLOR_PALETTE.get(idx);
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
