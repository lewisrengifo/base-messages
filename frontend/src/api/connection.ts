/**
 * Connection API Service
 *
 * API operations for managing WhatsApp Business API connection.
 * Includes secure credential storage and connection testing.
 */

import { apiClient } from './client';
import type {
  ConnectionStatus,
  ConnectionRequest,
  ConnectionTestResponse
} from './types';

const CONNECTION_ENDPOINT = '/connection';

/**
 * Get current connection status
 */
export async function getConnectionStatus(): Promise<ConnectionStatus> {
  return apiClient.get<ConnectionStatus>(CONNECTION_ENDPOINT);
}

/**
 * Save or update WhatsApp Business API credentials
 * Credentials are encrypted at rest on the server
 */
export async function saveConnection(data: ConnectionRequest): Promise<ConnectionStatus> {
  return apiClient.post<ConnectionStatus>(CONNECTION_ENDPOINT, data);
}

/**
 * Delete connection and remove stored credentials
 */
export async function deleteConnection(): Promise<void> {
  return apiClient.delete<void>(CONNECTION_ENDPOINT);
}

/**
 * Test connection to Meta Graph API
 */
export async function testConnection(): Promise<ConnectionTestResponse> {
  return apiClient.post<ConnectionTestResponse>(`${CONNECTION_ENDPOINT}/test`, {});
}
