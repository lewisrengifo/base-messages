/**
 * Auth API Service
 * 
 * Authentication-related API calls
 */

import { apiClient, ApiClientError } from './client';
import { LoginRequest, LoginResponse } from './types';
import { tokenStorage } from '@/services/storage';

export interface AuthTokens {
  accessToken: string;
  refreshToken?: string;
}

/**
 * Login with email and password
 */
export async function login(credentials: LoginRequest): Promise<{ user: LoginResponse['user']; tokens: AuthTokens }> {
  const response = await apiClient.post<LoginResponse>('/auth/login', credentials, {
    skipAuth: true, // Don't send auth header for login
  });

  return {
    user: response.user,
    tokens: {
      accessToken: response.token,
      // Note: Backend sends only access token in LoginResponse
      // Refresh token handling depends on backend implementation
    },
  };
}

/**
 * Logout - invalidate session
 */
export async function logout(): Promise<void> {
  try {
    await apiClient.post<void>('/auth/logout', {});
  } catch (error) {
    // Even if logout fails on server, clear local tokens
    console.warn('Logout API call failed:', error);
  } finally {
    tokenStorage.clearAll();
  }
}

/**
 * Check if user is authenticated (has tokens)
 */
export function isAuthenticated(): boolean {
  return tokenStorage.hasTokens();
}

/**
 * Handle auth errors
 */
export function isAuthError(error: unknown): boolean {
  if (error instanceof ApiClientError) {
    return error.statusCode === 401 || error.statusCode === 403;
  }
  return false;
}
