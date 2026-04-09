# MVP Implementation Plan

## Overview

This document outlines the implementation plan for the Base Messages MVP, focusing on the minimal set of features required to launch a functional WhatsApp Business messaging platform.

**Goal:** Enable users to create message templates, connect WhatsApp Business API credentials, manage contacts, and schedule broadcast campaigns with automated Meta template submission.

**Timeline:** 3 weeks (60-70 hours)

---

## Architecture Decisions

- **Stack:** Spring Boot 3.4, WebFlux (reactive), R2DBC/PostgreSQL
- **Pattern:** OpenAPI generator with Delegate pattern
- **Architecture:** Hexagonal (Clean Architecture)
- **Scheduling:** Quartz Scheduler with persistent job store
- **External API:** Meta Graph API for template submission and message sending

---

## Phase 1: Templates + Connection (Week 1) ✅ COMPLETED

**Objective:** Enable template creation with automated Meta submission and basic connection management.

### Templates (3 endpoints) ✅
- ✅ Create template with automated Meta submission
- ✅ List templates with filtering
- ✅ Get template details

**Key Features:**
- Template status tracking (DRAFT → PENDING → APPROVED/REJECTED)
- Automated submission to Meta Graph API upon creation
- Support for media templates (files, images)
- No real-time webhook updates (manual status refresh)

### Connection (2 endpoints) ✅
- ✅ Store WhatsApp Business API credentials (encrypted)
- ✅ Check connection status
- ✅ Test connection endpoint

**Key Features:**
- Secure storage of access tokens (encrypted at rest)
- Connection status tracking
- Auto-activation of pending campaigns when credentials are added

**Deliverables:**
- ✅ Templates API fully functional
- ✅ Connection skeleton working
- ✅ Meta API integration framework ready
- ✅ AES-256 encryption for access tokens
- ✅ R2DBC repositories with PostgreSQL
- ✅ All endpoints compile and pass Maven build

**Date Completed:** April 8, 2026

---

## Phase 2: Contacts (Week 2, Days 1-2)

**Objective:** Enable contact management for campaign audiences.

### Contacts (3 endpoints)
- Create contact
- List contacts with search
- Get contact details

**Key Features:**
- E.164 phone number format validation
- Contact deduplication by phone number
- Automatic initials and color generation
- Flat contact list (groups deferred to post-MVP)

**Deliverables:**
- Contacts API functional
- Database schema for contacts
- Integration with campaign audience selection

---

## Phase 3: Campaigns + Dashboard (Week 2-3)

**Objective:** Enable campaign creation, scheduling, and execution with rate limiting.

### Campaigns (4 endpoints)
- Create campaign with scheduling
- List campaigns
- Get campaign details
- Delete/cancel campaign

**Key Features:**
- Persistent scheduling with Quartz
- Two scheduling modes: immediate and scheduled
- Campaign state machine: DRAFT → SCHEDULED → SENDING → SENT
- Auto-activation when connection credentials are added
- Rate limiting for Meta API calls (respect Meta's 100 msg/sec limit)
- Support for media message campaigns (file attachments)

### Dashboard (1 endpoint)
- Get dashboard statistics

**Key Features:**
- Total messages sent
- Active scheduled campaigns count
- Next upcoming campaign timer

**Deliverables:**
- Full campaign lifecycle working
- Quartz jobs persistent across restarts
- Rate limiting implemented
- Dashboard metrics available

---

## Technical Considerations

### Meta Graph API Integration
- Automated template submission on creation
- Polling-based status updates (no webhooks)
- Rate limiting to respect Meta's API constraints
- Error handling for policy violations and format issues

### Security
- All access tokens encrypted at rest
- JWT authentication for all endpoints (except login)
- Input validation and sanitization
- Secure credential storage

### Scalability & Reliability
- Persistent job scheduling with Quartz
- Campaigns survive application restarts
- Rate limiting prevents API quota exhaustion
- Graceful handling of missing credentials

### Media Support
- Templates support file attachments
- Campaigns can send media messages
- File upload and storage mechanism
- Meta media message API integration

---

## Post-MVP Features (Not Included)

- Real-time Meta webhook updates
- Campaign analytics and delivery receipts
- Contact groups and segmentation
- Campaign cancellation and duplication
- Template editing and resubmission
- Bulk contact import (CSV)
- User notifications
- Advanced analytics dashboard

---

## Success Criteria

- [x] User can create a template with media and submit to Meta
- [x] Template status updates correctly (manual refresh)
- [x] User can add WhatsApp API credentials
- [ ] User can create contacts
- [ ] User can schedule a campaign for future delivery
- [ ] Campaign automatically sends at scheduled time
- [ ] Campaigns pending connection auto-activate when credentials added
- [ ] Rate limiting prevents API abuse
- [ ] Dashboard shows accurate metrics

---

## Risk Mitigation

**Risk:** Meta API changes or downtime
- **Mitigation:** Implement retry logic with exponential backoff

**Risk:** Rate limiting blocks campaigns
- **Mitigation:** Implement throttling and queue management

**Risk:** Credential storage security
- **Mitigation:** Industry-standard encryption, secure key management

**Risk:** Job scheduling failures
- **Mitigation:** Quartz persistent store, job recovery mechanisms

---

## Notes

- Media template support is required for MVP (especially files)
- No real-time updates from Meta (polling approach)
- Rate limiting is essential for production use
- Campaign analytics may be added post-MVP if time permits
- Focus on core messaging flow first, polish later

---

**Document Version:** 1.0
**Last Updated:** April 8, 2026 (Phase 1 completed)
**Owner:** Development Team
