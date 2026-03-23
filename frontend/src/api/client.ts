/**
 * API Client
 * 
 * Fetch wrapper with:
 * - Base URL configuration
 * - Automatic JSON parsing
 * - Bearer token injection
 * - Error handling
 * - Request logging (dev only)
 */

import { ApiError, ApiResponse } from './types';
import { tokenStorage } from '@/services/storage';

// API Configuration
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

// Custom error class for API errors
export class ApiClientError extends Error {
  constructor(
    message: string,
    public statusCode?: number,
    public apiError?: ApiError
  ) {
    super(message);
    this.name = 'ApiClientError';
  }
}

// Request options interface
interface RequestOptions extends RequestInit {
  skipAuth?: boolean;
}

/**
 * Make an API request
 */
async function request<T>(
  endpoint: string,
  options: RequestOptions = {}
): Promise<T> {
  const { skipAuth = false, ...fetchOptions } = options;

  // Build full URL
  const url = `${API_BASE_URL}${endpoint}`;

  // Default headers
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    Accept: 'application/json',
    ...((fetchOptions.headers as Record<string, string>) || {}),
  };

  // Add auth token if available and not skipped
  if (!skipAuth) {
    const token = tokenStorage.getAccessToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
  }

  // Request configuration
  const config: RequestInit = {
    ...fetchOptions,
    headers,
  };

  // Log request in development
  if (import.meta.env.DEV) {
    console.log(`[API] ${config.method || 'GET'} ${url}`);
  }

  try {
    const response = await fetch(url, config);

    // Handle 204 No Content
    if (response.status === 204) {
      return undefined as T;
    }

    // Parse response
    let data: unknown;
    const contentType = response.headers.get('content-type');
    
    if (contentType?.includes('application/json')) {
      data = await response.json();
    } else {
      data = await response.text();
    }

    // Log response in development
    if (import.meta.env.DEV) {
      console.log(`[API] ${response.status} ${url}`, data);
    }

    // Handle errors
    if (!response.ok) {
      const apiError: ApiError = 
        typeof data === 'object' && data !== null
          ? (data as ApiError)
          : { code: 'UNKNOWN_ERROR', message: 'An unknown error occurred' };

      throw new ApiClientError(
        apiError.message || `HTTP ${response.status}`,
        response.status,
        apiError
      );
    }

    return data as T;
  } catch (error) {
    // Handle network errors
    if (error instanceof ApiClientError) {
      throw error;
    }

    if (error instanceof TypeError && error.message.includes('fetch')) {
      throw new ApiClientError(
        'Network error. Please check your connection.',
        undefined,
        { code: 'NETWORK_ERROR', message: 'Network error' }
      );
    }

    throw new ApiClientError(
      error instanceof Error ? error.message : 'Unknown error',
      undefined,
      { code: 'UNKNOWN_ERROR', message: 'Unknown error' }
    );
  }
}

/**
 * API Client object with HTTP methods
 */
export const apiClient = {
  /**
   * GET request
   */
  get: <T>(endpoint: string, options?: RequestOptions): Promise<T> =>
    request<T>(endpoint, { ...options, method: 'GET' }),

  /**
   * POST request
   */
  post: <T>(endpoint: string, body: unknown, options?: RequestOptions): Promise<T> =>
    request<T>(endpoint, {
      ...options,
      method: 'POST',
      body: JSON.stringify(body),
    }),

  /**
   * PUT request
   */
  put: <T>(endpoint: string, body: unknown, options?: RequestOptions): Promise<T> =>
    request<T>(endpoint, {
      ...options,
      method: 'PUT',
      body: JSON.stringify(body),
    }),

  /**
   * DELETE request
   */
  delete: <T>(endpoint: string, options?: RequestOptions): Promise<T> =>
    request<T>(endpoint, { ...options, method: 'DELETE' }),

  /**
   * PATCH request
   */
  patch: <T>(endpoint: string, body: unknown, options?: RequestOptions): Promise<T> =>
    request<T>(endpoint, {
      ...options,
      method: 'PATCH',
      body: JSON.stringify(body),
    }),
};
