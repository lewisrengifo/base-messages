package com.lewisrp.basemessages.backend.application.service;

import com.lewisrp.basemessages.backend.application.dto.ContactDto;
import com.lewisrp.basemessages.backend.application.dto.ContactPageDto;
import com.lewisrp.basemessages.backend.application.dto.CreateContactCommand;
import com.lewisrp.basemessages.backend.application.dto.UpdateContactCommand;
import com.lewisrp.basemessages.backend.application.port.outbound.ContactRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.Contact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactApplicationServiceTest {

    @Mock
    private ContactRepositoryPort contactRepository;

    private ContactApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ContactApplicationService(contactRepository);
    }

    @Test
    void createContact_success() {
        CreateContactCommand command = new CreateContactCommand("Jane Doe", "+15550123456", "jane@example.com");

        when(contactRepository.existsByPhone(1L, "+15550123456")).thenReturn(Mono.just(false));
        when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(LocalDateTime.now());
            return Mono.just(c);
        });

        StepVerifier.create(service.createContact(command))
                .assertNext(dto -> {
                    assertThat(dto.getName()).isEqualTo("Jane Doe");
                    assertThat(dto.getPhone()).isEqualTo("+15550123456");
                    assertThat(dto.getEmail()).isEqualTo("jane@example.com");
                    assertThat(dto.getInitials()).isEqualTo("JD");
                    assertThat(dto.getColor()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void createContact_emptyName_returnsBadRequest() {
        CreateContactCommand command = new CreateContactCommand("  ", "+15550123456", null);

        StepVerifier.create(service.createContact(command))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().equals("Contact name is required"))
                .verify();

        verify(contactRepository, never()).existsByPhone(any(), any());
    }

    @Test
    void createContact_invalidPhone_returnsBadRequest() {
        CreateContactCommand command = new CreateContactCommand("Jane", "5550123456", null);

        StepVerifier.create(service.createContact(command))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("E.164"))
                .verify();

        verify(contactRepository, never()).existsByPhone(any(), any());
    }

    @Test
    void createContact_duplicatePhone_returnsConflict() {
        CreateContactCommand command = new CreateContactCommand("Jane Doe", "+15550123456", null);

        when(contactRepository.existsByPhone(1L, "+15550123456")).thenReturn(Mono.just(true));

        StepVerifier.create(service.createContact(command))
                .expectErrorMatches(e -> e instanceof IllegalStateException
                        && e.getMessage().contains("already exists"))
                .verify();
    }

    @Test
    void listContacts_withoutSearch() {
        Contact contact = Contact.builder()
                .id(1L)
                .userId(1L)
                .name("Jane")
                .phone("+15550123456")
                .initials("J")
                .color("blue")
                .createdAt(LocalDateTime.now())
                .build();

        when(contactRepository.findAll(any(PageRequest.class))).thenReturn(Flux.just(contact));
        when(contactRepository.countAll()).thenReturn(Mono.just(1L));

        StepVerifier.create(service.listContacts(null, PageRequest.of(0, 20)))
                .assertNext(page -> {
                    assertThat(page.getData()).hasSize(1);
                    assertThat(page.getTotal()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @Test
    void listContacts_withSearch() {
        Contact contact = Contact.builder()
                .id(1L)
                .userId(1L)
                .name("Jane")
                .phone("+15550123456")
                .initials("J")
                .color("blue")
                .createdAt(LocalDateTime.now())
                .build();

        when(contactRepository.search(eq("jane"), any(PageRequest.class))).thenReturn(Flux.just(contact));
        when(contactRepository.countSearch("jane")).thenReturn(Mono.just(1L));

        StepVerifier.create(service.listContacts("jane", PageRequest.of(0, 20)))
                .assertNext(page -> {
                    assertThat(page.getData()).hasSize(1);
                    assertThat(page.getTotal()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @Test
    void getContact_found() {
        Contact contact = Contact.builder()
                .id(1L)
                .userId(1L)
                .name("Jane")
                .phone("+15550123456")
                .initials("J")
                .color("blue")
                .createdAt(LocalDateTime.now())
                .build();

        when(contactRepository.findById(1L)).thenReturn(Mono.just(contact));

        StepVerifier.create(service.getContact(1L))
                .assertNext(dto -> assertThat(dto.getName()).isEqualTo("Jane"))
                .verifyComplete();
    }

    @Test
    void getContact_notFound() {
        when(contactRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(service.getContact(99L))
                .expectErrorMatches(e -> e instanceof ContactApplicationService.NotFoundException)
                .verify();
    }

    @Test
    void updateContact_success() {
        Contact existing = Contact.builder()
                .id(1L)
                .userId(1L)
                .name("Jane Doe")
                .phone("+15550123456")
                .email("old@example.com")
                .initials("JD")
                .color("blue")
                .createdAt(LocalDateTime.now())
                .build();

        UpdateContactCommand command = new UpdateContactCommand("Jane Smith", "+15550987654", "jane@example.com");

        when(contactRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(contactRepository.existsByPhoneAndIdNot(1L, "+15550987654", 1L)).thenReturn(Mono.just(false));
        when(contactRepository.update(any(Contact.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.updateContact(1L, command))
                .assertNext(dto -> {
                    assertThat(dto.getName()).isEqualTo("Jane Smith");
                    assertThat(dto.getPhone()).isEqualTo("+15550987654");
                    assertThat(dto.getEmail()).isEqualTo("jane@example.com");
                    assertThat(dto.getInitials()).isEqualTo("JS");
                })
                .verifyComplete();
    }

    @Test
    void updateContact_notFound() {
        UpdateContactCommand command = new UpdateContactCommand("Jane", "+15550123456", null);

        when(contactRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(service.updateContact(99L, command))
                .expectErrorMatches(e -> e instanceof ContactApplicationService.NotFoundException)
                .verify();
    }

    @Test
    void updateContact_duplicatePhone_returnsConflict() {
        Contact existing = Contact.builder()
                .id(1L)
                .userId(1L)
                .name("Jane Doe")
                .phone("+15550123456")
                .initials("JD")
                .color("blue")
                .createdAt(LocalDateTime.now())
                .build();

        UpdateContactCommand command = new UpdateContactCommand(null, "+15550987654", null);

        when(contactRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(contactRepository.existsByPhoneAndIdNot(1L, "+15550987654", 1L)).thenReturn(Mono.just(true));

        StepVerifier.create(service.updateContact(1L, command))
                .expectErrorMatches(e -> e instanceof IllegalStateException
                        && e.getMessage().contains("already exists"))
                .verify();
    }

    @Test
    void updateContact_invalidPhone_returnsBadRequest() {
        Contact existing = Contact.builder()
                .id(1L)
                .userId(1L)
                .name("Jane Doe")
                .phone("+15550123456")
                .initials("JD")
                .color("blue")
                .createdAt(LocalDateTime.now())
                .build();

        UpdateContactCommand command = new UpdateContactCommand(null, "invalid", null);

        when(contactRepository.findById(1L)).thenReturn(Mono.just(existing));

        StepVerifier.create(service.updateContact(1L, command))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("E.164"))
                .verify();
    }

    @Test
    void updateContact_emptyName_returnsBadRequest() {
        Contact existing = Contact.builder()
                .id(1L)
                .userId(1L)
                .name("Jane Doe")
                .phone("+15550123456")
                .initials("JD")
                .color("blue")
                .createdAt(LocalDateTime.now())
                .build();

        UpdateContactCommand command = new UpdateContactCommand("", null, null);

        when(contactRepository.findById(1L)).thenReturn(Mono.just(existing));

        StepVerifier.create(service.updateContact(1L, command))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("name is required"))
                .verify();
    }

    @Test
    void deleteContact_success() {
        Contact existing = Contact.builder()
                .id(1L)
                .userId(1L)
                .name("Jane")
                .phone("+15550123456")
                .initials("J")
                .color("blue")
                .createdAt(LocalDateTime.now())
                .build();

        when(contactRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(contactRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteContact(1L))
                .verifyComplete();

        verify(contactRepository).deleteById(1L);
    }

    @Test
    void deleteContact_notFound() {
        when(contactRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteContact(99L))
                .expectErrorMatches(e -> e instanceof ContactApplicationService.NotFoundException)
                .verify();
    }
}
