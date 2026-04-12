/**
 * Contacts API Service
 *
 * API operations for managing contacts and audience data.
 */

import { apiClient } from './client';
import type { Contact, ContactDetail, ContactListResponse, CreateContactRequest } from './types';

const CONTACTS_ENDPOINT = '/contacts';

export async function listContacts(params?: {
  page?: number;
  limit?: number;
  search?: string;
}): Promise<ContactListResponse> {
  const queryParams = new URLSearchParams();

  if (params?.page) queryParams.append('page', params.page.toString());
  if (params?.limit) queryParams.append('limit', params.limit.toString());
  if (params?.search) queryParams.append('search', params.search);

  const queryString = queryParams.toString();
  const endpoint = queryString ? `${CONTACTS_ENDPOINT}?${queryString}` : CONTACTS_ENDPOINT;

  return apiClient.get<ContactListResponse>(endpoint);
}

export async function getContact(id: number): Promise<ContactDetail> {
  return apiClient.get<ContactDetail>(`${CONTACTS_ENDPOINT}/${id}`);
}

export async function createContact(data: CreateContactRequest): Promise<Contact> {
  return apiClient.post<Contact>(CONTACTS_ENDPOINT, data);
}
