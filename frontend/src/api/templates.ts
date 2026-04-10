/**
 * Templates API Service
 * 
 * API operations for managing WhatsApp message templates.
 * Includes automated Meta submission upon creation.
 */

import { apiClient } from './client';
import type { Template, TemplateListResponse, CreateTemplateRequest, TemplateDetail, PaginationParams } from './types';

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
  return apiClient.post<Template>(TEMPLATES_ENDPOINT, data);
}

/**
 * Delete a template
 */
export async function deleteTemplate(id: number): Promise<void> {
  return apiClient.delete<void>(`${TEMPLATES_ENDPOINT}/${id}`);
}
