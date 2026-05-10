/**
 * Templates API Service
 * 
 * API operations for managing WhatsApp message templates.
 * Includes automated Meta submission upon creation.
 */

import { apiClient } from './client';
import type { Template, TemplateListResponse, CreateTemplateRequest, TemplateDetail, UpdateTemplateRequest } from './types';

const TEMPLATES_ENDPOINT = '/templates';

/**
 * List templates with optional filtering
 */
export async function listTemplates(params?: {
  page?: number;
  limit?: number;
  status?: string;
  category?: string;
  search?: string;
}): Promise<TemplateListResponse> {
  const queryParams = new URLSearchParams();
  
  if (params?.page) queryParams.append('page', params.page.toString());
  if (params?.limit) queryParams.append('limit', params.limit.toString());
  if (params?.status) queryParams.append('status', params.status);
  if (params?.category) queryParams.append('category', params.category);
  if (params?.search) queryParams.append('search', params.search);

  const queryString = queryParams.toString();
  const endpoint = queryString ? `${TEMPLATES_ENDPOINT}?${queryString}` : TEMPLATES_ENDPOINT;
  
  return apiClient.get<TemplateListResponse>(endpoint);
}

/**
 * Get a single template by ID
 */
export async function getTemplate(id: number): Promise<TemplateDetail> {
  return apiClient.get<TemplateDetail>(`${TEMPLATES_ENDPOINT}/${id}`);
}

/**
 * Create a new template
 * Automatically submits to Meta for approval
 */
export async function createTemplate(data: CreateTemplateRequest): Promise<Template> {
  const formData = new FormData();
  formData.append('name', data.name);
  formData.append('category', data.category);
  formData.append('content', data.content);
  formData.append('language', data.language);
  
  if (data.headerType) {
    formData.append('headerType', data.headerType);
  }
  
  if (data.headerDocument) {
    formData.append('headerDocument', data.headerDocument);
  }
  
  if (data.variables && data.variables.length > 0) {
    formData.append('variables', JSON.stringify(data.variables));
  }

  return apiClient.post<Template>(TEMPLATES_ENDPOINT, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

/**
 * Update an existing template
 */
export async function updateTemplate(id: number, data: UpdateTemplateRequest): Promise<Template> {
  return apiClient.put<Template>(`${TEMPLATES_ENDPOINT}/${id}`, data);
}

/**
 * Resubmit a template for Meta approval
 */
export async function resubmitTemplate(id: number): Promise<Template> {
  return apiClient.post<Template>(`${TEMPLATES_ENDPOINT}/${id}/resubmit`, {});
}

/**
 * Refresh template status from Meta API
 */
export async function refreshTemplateStatus(id: number): Promise<Template> {
  return apiClient.post<Template>(`${TEMPLATES_ENDPOINT}/${id}/refresh-status`, {});
}

/**
 * Delete a template
 */
export async function deleteTemplate(id: number): Promise<void> {
  return apiClient.delete<void>(`${TEMPLATES_ENDPOINT}/${id}`);
}
