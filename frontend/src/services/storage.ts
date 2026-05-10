/**
 * Token Storage Service
 * 
 * Manages access token (memory) and refresh token (localStorage)
 * Following backend JWT strategy:
 * - Access token: Short-lived (15 min), stored in memory
 * - Refresh token: Long-lived (7 days), stored in localStorage
 * - User info: Stored in localStorage to survive page refreshes
 */

const REFRESH_TOKEN_KEY = 'bm_refresh_token';
const USER_KEY = 'bm_user';

export interface StoredUser {
  id: number;
  email: string;
  name: string;
  avatar?: string;
}

class TokenStorage {
  private accessToken: string | null = null;

  /**
   * Get the current access token from memory
   */
  getAccessToken(): string | null {
    return this.accessToken;
  }

  /**
   * Set the access token in memory
   */
  setAccessToken(token: string): void {
    this.accessToken = token;
  }

  /**
   * Clear the access token from memory
   */
  clearAccessToken(): void {
    this.accessToken = null;
  }

  /**
   * Get the refresh token from localStorage
   */
  getRefreshToken(): string | null {
    try {
      return localStorage.getItem(REFRESH_TOKEN_KEY);
    } catch (e) {
      console.warn('localStorage not available');
      return null;
    }
  }

  /**
   * Set the refresh token in localStorage
   */
  setRefreshToken(token: string): void {
    try {
      localStorage.setItem(REFRESH_TOKEN_KEY, token);
    } catch (e) {
      console.warn('localStorage not available');
    }
  }

  /**
   * Clear the refresh token from localStorage
   */
  clearRefreshToken(): void {
    try {
      localStorage.removeItem(REFRESH_TOKEN_KEY);
    } catch (e) {
      console.warn('localStorage not available');
    }
  }

  /**
   * Check if user has tokens (might be authenticated)
   */
  hasTokens(): boolean {
    return this.accessToken !== null || this.getRefreshToken() !== null;
  }

  /**
   * Store user info in localStorage
   */
  setUser(user: StoredUser): void {
    try {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    } catch (e) {
      console.warn('localStorage not available');
    }
  }

  /**
   * Get user info from localStorage
   */
  getUser(): StoredUser | null {
    try {
      const data = localStorage.getItem(USER_KEY);
      return data ? JSON.parse(data) : null;
    } catch (e) {
      console.warn('localStorage not available');
      return null;
    }
  }

  /**
   * Clear user info from localStorage
   */
  clearUser(): void {
    try {
      localStorage.removeItem(USER_KEY);
    } catch (e) {
      console.warn('localStorage not available');
    }
  }

  /**
   * Clear all auth data (logout)
   */
  clearAll(): void {
    this.clearAccessToken();
    this.clearRefreshToken();
    this.clearUser();
  }
}

// Export singleton instance
export const tokenStorage = new TokenStorage();
