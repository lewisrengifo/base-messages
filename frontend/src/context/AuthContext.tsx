/**
 * Auth Context
 * 
 * Provides global authentication state and methods:
 * - user: Current authenticated user
 * - isAuthenticated: Boolean auth status
 * - isLoading: Loading state for auth operations
 * - login: Authenticate user
 * - logout: Clear auth state
 */

import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { User } from '@/api/types';
import { login as loginApi, logout as logoutApi, refreshToken as refreshTokenApi, AuthTokens } from '@/api/auth';
import { tokenStorage } from '@/services/storage';
import { ApiClientError } from '@/api/client';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: React.ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Restore session on mount using refresh token
  useEffect(() => {
    const storedUser = tokenStorage.getUser();
    const refreshToken = tokenStorage.getRefreshToken();

    if (refreshToken) {
      refreshTokenApi(refreshToken)
        .then(({ user: userData, tokens }) => {
          if (tokens.accessToken) {
            tokenStorage.setAccessToken(tokens.accessToken);
          }
          if (tokens.refreshToken) {
            tokenStorage.setRefreshToken(tokens.refreshToken);
          }
          tokenStorage.setUser(userData);
          setUser(userData);
        })
        .catch(() => {
          // Refresh failed, clear everything
          tokenStorage.clearAll();
        })
        .finally(() => {
          setIsLoading(false);
        });
    } else if (storedUser) {
      // No refresh token but user info exists - clear stale data
      tokenStorage.clearAll();
      setIsLoading(false);
    } else {
      setIsLoading(false);
    }
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    setIsLoading(true);
    setError(null);

    try {
      const { user: userData, tokens } = await loginApi({ email, password });
      
      // Store tokens
      if (tokens.accessToken) {
        tokenStorage.setAccessToken(tokens.accessToken);
      }
      if (tokens.refreshToken) {
        tokenStorage.setRefreshToken(tokens.refreshToken);
      }

      // Persist user info so session survives refresh
      tokenStorage.setUser(userData);

      // Update state
      setUser(userData);
    } catch (err) {
      let errorMessage = 'Login failed. Please try again.';
      
      if (err instanceof ApiClientError) {
        if (err.statusCode === 401) {
          errorMessage = 'Invalid email or password.';
        } else if (err.apiError?.message) {
          errorMessage = err.apiError.message;
        }
      }

      setError(errorMessage);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    setIsLoading(true);

    try {
      await logoutApi();
    } finally {
      setUser(null);
      setIsLoading(false);
    }
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    error,
    login,
    logout,
    clearError,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

/**
 * Hook to use auth context
 */
export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
