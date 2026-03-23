/**
 * API Type Definitions
 * 
 * TypeScript interfaces for API requests and responses
 */

// User Types
export interface User {
  id: number;
  email: string;
  name: string;
  avatar?: string;
}

// Auth Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

// API Error Types
export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, unknown>;
}

// Generic API Response wrapper
export interface ApiResponse<T> {
  data: T;
  success: boolean;
  error?: ApiError;
}

// Campaign Types (for future use)
export interface Campaign {
  id: string;
  name: string;
  templateName: string;
  scheduledDate?: string;
  status: 'draft' | 'scheduled' | 'sending' | 'sent' | 'canceled' | 'failed';
  createdAt: string;
  updatedAt: string;
}

// Template Types (for future use)
export interface Template {
  id: number;
  name: string;
  category: 'Marketing' | 'Utility' | 'Authentication';
  language: string;
  status: 'APPROVED' | 'PENDING' | 'REJECTED' | 'DRAFT';
  content: string;
  createdAt: string;
  updatedAt: string;
}

// Contact Types (for future use)
export interface Contact {
  id: number;
  name: string;
  phone: string;
  email?: string;
  initials: string;
  color: string;
  createdAt: string;
}
