/**
 * API Type Definitions
 *
 * TypeScript interfaces for API requests and responses
 * Aligned with backend DTOs and OpenAPI specification
 */

// ============================================================================
// Common Types
// ============================================================================

export interface PaginationParams {
  page?: number;
  limit?: number;
}

export interface Pagination {
  page: number;
  limit: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrev: boolean;
}

// ============================================================================
// User Types
// ============================================================================

export interface User {
  id: number;
  email: string;
  name: string;
  avatar?: string;
}

// ============================================================================
// Auth Types
// ============================================================================

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

// ============================================================================
// API Error Types
// ============================================================================

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, unknown>;
}

export interface ApiResponse<T> {
  data: T;
  success: boolean;
  error?: ApiError;
}

// ============================================================================
// Template Types
// ============================================================================

export type TemplateStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED';
export type TemplateCategory = 'MARKETING' | 'UTILITY' | 'AUTHENTICATION';

export interface TemplateVariable {
  position: number;
  example: string;
}

export interface Template {
  id: number;
  name: string;
  category: TemplateCategory;
  language: string;
  status: TemplateStatus;
  content: string;
  variables?: TemplateVariable[];
  createdAt: string;
  updatedAt: string;
}

export interface TemplateDetail extends Template {
  rejectionReason?: string;
}

export interface TemplateListResponse {
  data: Template[];
  pagination: Pagination;
}

export interface CreateTemplateRequest {
  name: string;
  category: TemplateCategory;
  content: string;
  language: string;
  variables?: CreateTemplateVariable[];
}

export interface CreateTemplateVariable {
  example: string;
}

// ============================================================================
// Connection Types
// ============================================================================

export type ConnectionStatusEnum = 'ACTIVE' | 'INACTIVE' | 'ERROR';
export type EndpointConnectivityEnum = 'CONNECTED' | 'FAILED';

export interface ConnectionStatus {
  status: ConnectionStatusEnum;
  phoneNumberId?: string;
  wabaId?: string;
  lastHeartbeat?: string;
  endpointConnectivity: EndpointConnectivityEnum;
}

export interface ConnectionRequest {
  phoneNumberId: string;
  wabaId: string;
  accessToken: string;
}

export interface ConnectionTestResponse {
  success: boolean;
  message: string;
  latency: number;
}

// ============================================================================
// Campaign Types (Phase 3)
// ============================================================================

export type CampaignStatus = 'DRAFT' | 'SCHEDULED' | 'SENDING' | 'SENT' | 'CANCELED' | 'FAILED';

export interface Campaign {
  id: string;
  name: string;
  templateId: number;
  templateName: string;
  scheduledDate?: string;
  status: CampaignStatus;
  audienceSize: number;
  createdAt: string;
  updatedAt: string;
}

export interface CampaignListResponse {
  data: Campaign[];
  pagination: Pagination;
}

export interface CreateCampaignRequest {
  name: string;
  templateId: number;
  scheduledDate?: string;
  contactIds: number[];
}

// ============================================================================
// Contact Types (Phase 2)
// ============================================================================

export interface Contact {
  id: number;
  name: string;
  phone: string;
  email?: string;
  initials: string;
  color: string;
  createdAt: string;
  updatedAt: string;
}

export interface ContactListResponse {
  data: Contact[];
  pagination: Pagination;
}

export interface CreateContactRequest {
  name: string;
  phone: string;
  email?: string;
}

// ============================================================================
// Dashboard Types (Phase 3)
// ============================================================================

export interface DashboardStats {
  totalMessagesSent: number;
  activeScheduledCampaigns: number;
  nextCampaignDate?: string;
  totalTemplates: number;
  approvedTemplates: number;
  totalContacts: number;
}
