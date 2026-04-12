package com.lewisrp.basemessages.backend.infrastructure.adapter.web;

import com.lewisrp.basemessages.backend.application.dto.ContactDto;
import com.lewisrp.basemessages.backend.application.dto.ContactPageDto;
import com.lewisrp.basemessages.backend.application.dto.CreateContactCommand;
import com.lewisrp.basemessages.backend.application.service.ContactApplicationService;
import org.openapitools.api.ContactsApiDelegate;
import org.openapitools.model.Contact;
import org.openapitools.model.ContactDetail;
import org.openapitools.model.ContactListResponse;
import org.openapitools.model.CreateContactRequest;
import org.openapitools.model.Pagination;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the OpenAPI generated ContactsApiDelegate interface.
 */
@Component
public class ContactsApiDelegateImpl implements ContactsApiDelegate {

    private final ContactApplicationService contactService;

    public ContactsApiDelegateImpl(ContactApplicationService contactService) {
        this.contactService = contactService;
    }

    @Override
    public Mono<ResponseEntity<ContactListResponse>> contactsGet(
            Integer page,
            Integer limit,
            String search,
            String group,
            ServerWebExchange exchange) {

        int pageNum = page != null ? page - 1 : 0;
        int pageSize = limit != null ? limit : 20;

        return contactService.listContacts(search, PageRequest.of(pageNum, pageSize))
                .map(pageDto -> {
                    List<ContactDto> contacts = pageDto.getData();
                    long total = pageDto.getTotal();
                    long totalPages = pageSize > 0 ? (long) Math.ceil((double) total / pageSize) : 1;
                    boolean hasPrev = pageNum > 0;
                    boolean hasNext = (long) (pageNum + 1) * pageSize < total;

                    ContactListResponse response = new ContactListResponse();
                    response.setData(contacts.stream()
                            .map(this::toApiModel)
                            .collect(Collectors.toList()));

                    Pagination pagination = new Pagination();
                    pagination.setPage(page != null ? page : 1);
                    pagination.setLimit(pageSize);
                    pagination.setTotal((int) total);
                    pagination.setTotalPages((int) Math.max(1, totalPages));
                    pagination.setHasNext(hasNext);
                    pagination.setHasPrev(hasPrev);
                    response.setPagination(pagination);

                    return ResponseEntity.ok(response);
                });
    }

    @Override
    public Mono<ResponseEntity<Contact>> contactsPost(
            Mono<CreateContactRequest> createContactRequest,
            ServerWebExchange exchange) {
        return createContactRequest
                .map(this::toCommand)
                .flatMap(contactService::createContact)
                .map(this::toApiModel)
                .map(contact -> ResponseEntity.status(HttpStatus.CREATED).body(contact))
                .onErrorResume(ContactApplicationService.NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(IllegalStateException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()))
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Override
    public Mono<ResponseEntity<ContactDetail>> contactsIdGet(Integer id, ServerWebExchange exchange) {
        return contactService.getContact(Long.valueOf(id))
                .map(this::toApiDetailModel)
                .map(ResponseEntity::ok)
                .onErrorResume(ContactApplicationService.NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }

    private CreateContactCommand toCommand(CreateContactRequest request) {
        return new CreateContactCommand(request.getName(), request.getPhone(), request.getEmail());
    }

    private Contact toApiModel(ContactDto dto) {
        Contact contact = new Contact();
        contact.setId(dto.getId() != null ? dto.getId().intValue() : null);
        contact.setName(dto.getName());
        contact.setPhone(dto.getPhone());
        contact.setInitials(dto.getInitials());
        contact.setColor(dto.getColor());
        if (dto.getCreatedAt() != null) {
            contact.setCreatedAt(dto.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        return contact;
    }

    private ContactDetail toApiDetailModel(ContactDto dto) {
        ContactDetail detail = new ContactDetail();
        detail.setId(dto.getId() != null ? dto.getId().intValue() : null);
        detail.setName(dto.getName());
        detail.setPhone(dto.getPhone());
        detail.setEmail(dto.getEmail());
        detail.setInitials(dto.getInitials());
        detail.setColor(dto.getColor());
        if (dto.getCreatedAt() != null) {
            detail.setCreatedAt(dto.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        detail.setGroups(Collections.emptyList());
        detail.setMessageHistory(Collections.emptyList());
        return detail;
    }
}
