/**
 * Campaigns API Service
 *
 * API operations for managing broadcast campaigns.
 */

import { apiClient } from './client';
import type { Campaign, CampaignDetail, CampaignListResponse, CreateCampaignRequest } from './types';

const CAMPAIGNS_ENDPOINT = '/campaigns';

/**
 * List campaigns with optional filtering
 */
export async function listCampaigns(params?: {
  page?: number;
  limit?: number;
  status?: string;
  search?: string;
}): Promise<CampaignListResponse> {
  const queryParams = new URLSearchParams();

  if (params?.page) queryParams.append('page', params.page.toString());
  if (params?.limit) queryParams.append('limit', params.limit.toString());
  if (params?.status) queryParams.append('status', params.status);
  if (params?.search) queryParams.append('search', params.search);

  const queryString = queryParams.toString();
  const endpoint = queryString ? `${CAMPAIGNS_ENDPOINT}?${queryString}` : CAMPAIGNS_ENDPOINT;

  return apiClient.get<CampaignListResponse>(endpoint);
}

/**
 * Get a single campaign by code
 */
export async function getCampaign(id: string): Promise<CampaignDetail> {
  return apiClient.get<CampaignDetail>(`${CAMPAIGNS_ENDPOINT}/${id}`);
}

/**
 * Create a new campaign
 */
export async function createCampaign(data: CreateCampaignRequest): Promise<Campaign> {
  return apiClient.post<Campaign>(CAMPAIGNS_ENDPOINT, data);
}

/**
 * Cancel a campaign
 */
export async function cancelCampaign(id: string): Promise<Campaign> {
  return apiClient.post<Campaign>(`${CAMPAIGNS_ENDPOINT}/${id}/cancel`, {});
}

/**
 * Duplicate a campaign
 */
export async function duplicateCampaign(id: string): Promise<Campaign> {
  return apiClient.post<Campaign>(`${CAMPAIGNS_ENDPOINT}/${id}/duplicate`, {});
}

/**
 * Delete a campaign
 */
export async function deleteCampaign(id: string): Promise<void> {
  return apiClient.delete<void>(`${CAMPAIGNS_ENDPOINT}/${id}`);
}
