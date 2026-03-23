-- ============================================================================
-- Base Messaging Platform - PostgreSQL Database Schema
-- ============================================================================
-- This script creates the complete database schema for the Base Messaging Platform
-- including all tables, indexes, constraints, and seed data.
-- ============================================================================

-- Drop existing objects if they exist (for clean installation)
DROP TABLE IF EXISTS campaign_analytics_timeline CASCADE;
DROP TABLE IF EXISTS campaign_analytics CASCADE;
DROP TABLE IF EXISTS campaign_recipients CASCADE;
DROP TABLE IF EXISTS campaigns CASCADE;
DROP TABLE IF EXISTS template_variables CASCADE;
DROP TABLE IF EXISTS templates CASCADE;
DROP TABLE IF EXISTS contact_group_members CASCADE;
DROP TABLE IF EXISTS contact_groups CASCADE;
DROP TABLE IF EXISTS contacts CASCADE;
DROP TABLE IF EXISTS import_jobs CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS whatsapp_connections CASCADE;
DROP TABLE IF EXISTS users CASCADE;

DROP TYPE IF EXISTS campaign_status CASCADE;
DROP TYPE IF EXISTS template_status CASCADE;
DROP TYPE IF EXISTS template_category CASCADE;
DROP TYPE IF EXISTS notification_type CASCADE;
DROP TYPE IF EXISTS import_job_status CASCADE;
DROP TYPE IF EXISTS connection_status CASCADE;
DROP TYPE IF EXISTS audience_type CASCADE;
DROP TYPE IF EXISTS schedule_type CASCADE;

-- ============================================================================
-- ENUM TYPES
-- ============================================================================

CREATE TYPE campaign_status AS ENUM ('draft', 'scheduled', 'sending', 'sent', 'canceled', 'failed');
CREATE TYPE template_status AS ENUM ('APPROVED', 'PENDING', 'REJECTED', 'DRAFT');
CREATE TYPE template_category AS ENUM ('Marketing', 'Utility', 'Authentication');
CREATE TYPE notification_type AS ENUM ('campaign', 'template', 'system', 'billing');
CREATE TYPE import_job_status AS ENUM ('pending', 'processing', 'completed', 'failed');
CREATE TYPE connection_status AS ENUM ('active', 'inactive', 'error');
CREATE TYPE audience_type AS ENUM ('all', 'group', 'segment');
CREATE TYPE schedule_type AS ENUM ('immediate', 'scheduled');
CREATE TYPE device_type AS ENUM ('Mobile', 'Desktop', 'Tablet');
CREATE TYPE message_status AS ENUM ('pending', 'sent', 'delivered', 'opened', 'clicked', 'bounced', 'failed');

-- ============================================================================
-- CORE TABLES
-- ============================================================================

-- Users table for authentication
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE
);

-- WhatsApp API Connection configuration
CREATE TABLE whatsapp_connections (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    phone_number_id VARCHAR(50) NOT NULL,
    waba_id VARCHAR(50) NOT NULL,  -- WhatsApp Business Account ID
    access_token_encrypted TEXT NOT NULL,  -- Encrypted access token
    status connection_status DEFAULT 'inactive',
    last_heartbeat_at TIMESTAMP WITH TIME ZONE,
    endpoint_connectivity VARCHAR(20) DEFAULT 'failed',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id)
);

-- ============================================================================
-- TEMPLATE TABLES
-- ============================================================================

-- Message templates
CREATE TABLE templates (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,  -- e.g., "welcome_series_q4"
    category template_category NOT NULL,
    language VARCHAR(10) NOT NULL DEFAULT 'EN_US',
    status template_status DEFAULT 'DRAFT',
    content TEXT NOT NULL,  -- Template content with {{variables}}
    rejection_reason TEXT,  -- Only populated if status is REJECTED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, name)
);

-- Template variables ({{1}}, {{2}}, etc.)
CREATE TABLE template_variables (
    id SERIAL PRIMARY KEY,
    template_id INTEGER NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    position INTEGER NOT NULL,  -- Variable position (1, 2, 3, etc.)
    example_value VARCHAR(255),  -- Example value for the variable
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(template_id, position)
);

-- ============================================================================
-- CONTACT TABLES
-- ============================================================================

-- Contacts (customers/recipients)
CREATE TABLE contacts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,  -- E.164 format
    email VARCHAR(255),
    initials VARCHAR(10),  -- Auto-generated from name
    color VARCHAR(50),  -- UI color class e.g., "bg-blue-100 text-blue-700"
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, phone)
);

-- Contact groups/segments
CREATE TABLE contact_groups (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, name)
);

-- Many-to-many relationship between contacts and groups
CREATE TABLE contact_group_members (
    id SERIAL PRIMARY KEY,
    contact_id INTEGER NOT NULL REFERENCES contacts(id) ON DELETE CASCADE,
    group_id INTEGER NOT NULL REFERENCES contact_groups(id) ON DELETE CASCADE,
    added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(contact_id, group_id)
);

-- Import jobs for tracking bulk contact imports
CREATE TABLE import_jobs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    status import_job_status DEFAULT 'pending',
    total_rows INTEGER DEFAULT 0,
    processed_rows INTEGER DEFAULT 0,
    successful_rows INTEGER DEFAULT 0,
    failed_rows INTEGER DEFAULT 0,
    error_message TEXT,
    group_id INTEGER REFERENCES contact_groups(id) ON DELETE SET NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- CAMPAIGN TABLES
-- ============================================================================

-- Campaigns (broadcast messages)
CREATE TABLE campaigns (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    campaign_code VARCHAR(20) NOT NULL UNIQUE,  -- e.g., "CAMP-9283"
    name VARCHAR(100) NOT NULL,
    template_id INTEGER NOT NULL REFERENCES templates(id),
    status campaign_status DEFAULT 'draft',
    audience_type audience_type NOT NULL,
    audience_group_id INTEGER REFERENCES contact_groups(id) ON DELETE SET NULL,
    recipient_count INTEGER DEFAULT 0,
    schedule_type schedule_type NOT NULL,
    scheduled_date DATE,
    scheduled_time TIME,
    scheduled_at TIMESTAMP WITH TIME ZONE,  -- Combined date/time
    sent_at TIMESTAMP WITH TIME ZONE,
    estimated_cost DECIMAL(10, 2) DEFAULT 0.00,  -- In USD
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    canceled_at TIMESTAMP WITH TIME ZONE,
    canceled_reason TEXT
);

-- Individual campaign recipients and their message status
CREATE TABLE campaign_recipients (
    id SERIAL PRIMARY KEY,
    campaign_id INTEGER NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    contact_id INTEGER NOT NULL REFERENCES contacts(id) ON DELETE CASCADE,
    status message_status DEFAULT 'pending',
    sent_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    opened_at TIMESTAMP WITH TIME ZONE,
    clicked_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    failure_reason TEXT,
    device_type device_type,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(campaign_id, contact_id)
);

-- Campaign analytics summary
CREATE TABLE campaign_analytics (
    id SERIAL PRIMARY KEY,
    campaign_id INTEGER NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    total_sent INTEGER DEFAULT 0,
    total_delivered INTEGER DEFAULT 0,
    total_opened INTEGER DEFAULT 0,
    total_clicked INTEGER DEFAULT 0,
    total_bounced INTEGER DEFAULT 0,
    total_failed INTEGER DEFAULT 0,
    open_rate DECIMAL(5, 2) DEFAULT 0.00,  -- Percentage
    click_rate DECIMAL(5, 2) DEFAULT 0.00,  -- Percentage
    bounce_rate DECIMAL(5, 2) DEFAULT 0.00,  -- Percentage
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(campaign_id)
);

-- Campaign analytics timeline (hourly breakdown)
CREATE TABLE campaign_analytics_timeline (
    id SERIAL PRIMARY KEY,
    campaign_id INTEGER NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    time_bucket TIMESTAMP WITH TIME ZONE NOT NULL,  -- Hourly bucket
    sent_count INTEGER DEFAULT 0,
    delivered_count INTEGER DEFAULT 0,
    opened_count INTEGER DEFAULT 0,
    UNIQUE(campaign_id, time_bucket)
);

-- Top performing links within a campaign
CREATE TABLE campaign_top_links (
    id SERIAL PRIMARY KEY,
    campaign_id INTEGER NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    label VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    clicks INTEGER DEFAULT 0,
    ctr DECIMAL(5, 2) DEFAULT 0.00,  -- Click-through rate percentage
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(campaign_id, url)
);

-- ============================================================================
-- NOTIFICATION TABLES
-- ============================================================================

-- User notifications
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    type notification_type NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    reference_id INTEGER,  -- ID of related entity (campaign_id, template_id, etc.)
    reference_type VARCHAR(50),  -- Type of referenced entity
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP WITH TIME ZONE
);

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

-- User indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);

-- Template indexes
CREATE INDEX idx_templates_user_id ON templates(user_id);
CREATE INDEX idx_templates_status ON templates(status);
CREATE INDEX idx_templates_category ON templates(category);
CREATE INDEX idx_templates_user_status ON templates(user_id, status);

-- Contact indexes
CREATE INDEX idx_contacts_user_id ON contacts(user_id);
CREATE INDEX idx_contacts_phone ON contacts(phone);
CREATE INDEX idx_contacts_user_phone ON contacts(user_id, phone);

-- Contact group indexes
CREATE INDEX idx_contact_groups_user_id ON contact_groups(user_id);
CREATE INDEX idx_contact_group_members_contact ON contact_group_members(contact_id);
CREATE INDEX idx_contact_group_members_group ON contact_group_members(group_id);

-- Campaign indexes
CREATE INDEX idx_campaigns_user_id ON campaigns(user_id);
CREATE INDEX idx_campaigns_status ON campaigns(status);
CREATE INDEX idx_campaigns_template ON campaigns(template_id);
CREATE INDEX idx_campaigns_user_status ON campaigns(user_id, status);
CREATE INDEX idx_campaigns_scheduled_at ON campaigns(scheduled_at);
CREATE INDEX idx_campaigns_sent_at ON campaigns(sent_at);

-- Campaign recipient indexes
CREATE INDEX idx_campaign_recipients_campaign ON campaign_recipients(campaign_id);
CREATE INDEX idx_campaign_recipients_contact ON campaign_recipients(contact_id);
CREATE INDEX idx_campaign_recipients_status ON campaign_recipients(status);

-- Analytics indexes
CREATE INDEX idx_campaign_analytics_campaign ON campaign_analytics(campaign_id);
CREATE INDEX idx_analytics_timeline_campaign ON campaign_analytics_timeline(campaign_id);
CREATE INDEX idx_analytics_timeline_time ON campaign_analytics_timeline(time_bucket);

-- Notification indexes
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- ============================================================================
-- FUNCTIONS AND TRIGGERS
-- ============================================================================
-- NOTE: All business logic is handled in the backend application.
-- The following operations should be managed by the backend:
-- 
-- 1. Auto-generate contact initials from name
-- 2. Update 'updated_at' timestamps on record changes
-- 3. Generate campaign codes (CAMP-0001, CAMP-0002, etc.)
-- 4. Calculate campaign analytics metrics
-- 5. Update analytics on recipient status changes
--
-- This approach provides:
-- - Better testability
-- - Easier debugging
-- - More control over business logic
-- - Consistency across different database vendors

-- ============================================================================
-- SEED DATA
-- ============================================================================

-- Insert default admin user (password: admin123 - hashed with bcrypt)
INSERT INTO users (email, password_hash, name, avatar_url) VALUES
('admin@basemessaging.com', '$2a$10$LK1cwmYoRbrn1/Qd/AqfqOBDgVdM8ZAc1Hlki8figQnijc2T48Jv.', 'System Admin', 'https://ui-avatars.com/api/?name=System+Admin&background=random');

-- Insert sample contact groups
INSERT INTO contact_groups (user_id, name, description) VALUES
(1, 'All Contacts', 'All contacts in the system'),
(1, 'VIP Customers', 'High-value customers'),
(1, 'New Customers', 'Recently acquired customers');

-- Insert sample contacts
INSERT INTO contacts (user_id, name, phone, email, color) VALUES
(1, 'Jane Doe', '+15550123456', 'jane@example.com', 'bg-blue-100 text-blue-700'),
(1, 'Michael Ross', '+442079460958', 'michael@example.com', 'bg-indigo-100 text-indigo-700'),
(1, 'Sarah Chen', '+6581234567', 'sarah@example.com', 'bg-sky-100 text-sky-700'),
(1, 'Amara Kante', '+2348031234567', 'amara@example.com', 'bg-emerald-100 text-emerald-700'),
(1, 'John Smith', '+15550987654', 'john@example.com', 'bg-purple-100 text-purple-700'),
(1, 'Emma Wilson', '+442071234567', 'emma@example.com', 'bg-rose-100 text-rose-700'),
(1, 'Carlos Rodriguez', '+34912345678', 'carlos@example.com', 'bg-amber-100 text-amber-700'),
(1, 'Yuki Tanaka', '+819012345678', 'yuki@example.com', 'bg-cyan-100 text-cyan-700');

-- Add contacts to groups
INSERT INTO contact_group_members (contact_id, group_id) VALUES
(1, 1), (2, 1), (3, 1), (4, 1), (5, 1), (6, 1), (7, 1), (8, 1),  -- All in "All Contacts"
(1, 2), (5, 2),  -- VIP Customers
(6, 3), (7, 3), (8, 3);  -- New Customers

-- Insert sample templates
INSERT INTO templates (user_id, name, category, language, status, content) VALUES
(1, 'order_confirmation', 'Utility', 'EN_US', 'APPROVED', 'Hello {{1}}, thank you for your order #{{2}}. We are preparing it for shipment and will notify you soon!'),
(1, 'marketing_sale', 'Marketing', 'EN_US', 'PENDING', 'Flash Sale! Use code {{1}} to get 40% OFF on all sapphire collections. Valid until {{2}}.'),
(1, 'two_factor_auth', 'Authentication', 'EN_US', 'APPROVED', '{{1}} is your verification code. For security, do not share this code with anyone.'),
(1, 'product_feedback', 'Utility', 'EN_GB', 'REJECTED', 'How was your experience today? Rate us here {{1}} and get a special coupon.'),
(1, 'welcome_onboard', 'Marketing', 'EN_US', 'APPROVED', 'Welcome {{1}}! 👋 Thank you for joining us. Your journey starts here. Order #{{2}} confirmed!');

-- Add template variables
INSERT INTO template_variables (template_id, position, example_value) VALUES
(1, 1, 'John'), (1, 2, 'ORD-12345'),
(2, 1, 'FLASH40'), (2, 2, 'Dec 31, 2024'),
(3, 1, '123456'),
(4, 1, 'https://feedback.example.com/r/abc123'),
(5, 1, 'Sarah'), (5, 2, 'SL-2024-001');

-- Update rejection reason for rejected template
UPDATE templates SET rejection_reason = 'Policy violation detected' WHERE id = 4;

-- Insert sample campaigns (campaign_code must be provided since we removed the trigger)
INSERT INTO campaigns (
    user_id, campaign_code, name, template_id, status, audience_type, audience_group_id, 
    recipient_count, schedule_type, scheduled_date, scheduled_time, scheduled_at,
    estimated_cost, sent_at
) VALUES
(1, 'CAMP-9283', 'Q3 Product Reveal', 1, 'sent', 'group', 1, 2450, 'scheduled', '2023-10-12', '09:00', '2023-10-12 09:00:00+00', 12.25, '2023-10-12 09:00:00+00'),
(1, 'CAMP-9455', 'Summer Flash Sale', 2, 'scheduled', 'group', 2, 124, 'scheduled', '2023-10-24', '14:30', '2023-10-24 14:30:00+00', 0.62, NULL),
(1, 'CAMP-8112', 'System Maintenance Alert', 1, 'canceled', 'all', NULL, 8000, 'scheduled', '2023-10-05', '11:15', '2023-10-05 11:15:00+00', 40.00, NULL),
(1, 'CAMP-9002', 'Monthly Newsletter #42', 5, 'sent', 'group', 1, 15000, 'scheduled', '2023-09-28', '10:00', '2023-09-28 10:00:00+00', 75.00, '2023-09-28 10:00:00+00'),
(1, 'CAMP-9050', 'Holiday Special', 5, 'draft', 'group', 3, 450, 'immediate', NULL, NULL, NULL, 2.25, NULL);

-- Add campaign recipients for sent campaigns (simplified for demo)
INSERT INTO campaign_recipients (campaign_id, contact_id, status, sent_at, delivered_at, device_type)
SELECT 
    1 as campaign_id,
    id as contact_id,
    'delivered'::message_status,
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '2 days' + INTERVAL '30 seconds',
    CASE WHEN random() < 0.74 THEN 'Mobile'::device_type 
         WHEN random() < 0.96 THEN 'Desktop'::device_type 
         ELSE 'Tablet'::device_type 
    END
FROM contacts WHERE user_id = 1;

-- Mark some as opened
UPDATE campaign_recipients 
SET status = 'opened', opened_at = delivered_at + INTERVAL '5 minutes'
WHERE campaign_id = 1 AND random() < 0.642;

-- Mark some as clicked
UPDATE campaign_recipients 
SET status = 'clicked', clicked_at = opened_at + INTERVAL '2 minutes'
WHERE campaign_id = 1 AND status = 'opened' AND random() < 0.287;

-- Insert sample notifications
INSERT INTO notifications (user_id, title, description, type, is_read, created_at) VALUES
(1, 'Campaign Approved', 'Your "Q3 Product Reveal" template has been approved by Meta.', 'template', TRUE, NOW() - INTERVAL '2 hours'),
(1, 'Low Balance Warning', 'Your API credit balance is below $50. Please recharge.', 'billing', TRUE, NOW() - INTERVAL '5 hours'),
(1, 'New Contact List', 'Import of "Summer Leads 2024" completed successfully.', 'system', FALSE, NOW() - INTERVAL '1 day'),
(1, 'System Update', 'Base API v2.4 is now live with enhanced analytics.', 'system', FALSE, NOW() - INTERVAL '2 days'),
(1, 'Campaign Sent', 'Your "Monthly Newsletter #42" campaign has been successfully sent to 15,000 recipients.', 'campaign', FALSE, NOW() - INTERVAL '3 days'),
(1, 'Template Rejected', 'Your "Product Feedback" template was rejected. Reason: Policy violation detected.', 'template', FALSE, NOW() - INTERVAL '4 days');

-- ============================================================================
-- VIEWS FOR COMMON QUERIES
-- ============================================================================

-- View for template performance metrics
CREATE VIEW template_performance_view AS
SELECT 
    t.id,
    t.name,
    t.category,
    COUNT(DISTINCT c.id) FILTER (WHERE c.status = 'sent') as times_used,
    COUNT(cr.id) FILTER (WHERE cr.status IN ('opened', 'clicked')) as total_opens,
    COUNT(cr.id) FILTER (WHERE cr.status = 'clicked') as total_clicks,
    COUNT(cr.id) as total_sent,
    CASE 
        WHEN COUNT(cr.id) > 0 
        THEN ROUND(COUNT(cr.id) FILTER (WHERE cr.status IN ('opened', 'clicked'))::DECIMAL / COUNT(cr.id) * 100, 1)
        ELSE 0 
    END as open_rate,
    CASE 
        WHEN COUNT(cr.id) > 0 
        THEN ROUND(COUNT(cr.id) FILTER (WHERE cr.status = 'clicked')::DECIMAL / COUNT(cr.id) * 100, 1)
        ELSE 0 
    END as ctr,
    MAX(c.sent_at) as last_sent
FROM templates t
LEFT JOIN campaigns c ON t.id = c.template_id AND c.status = 'sent'
LEFT JOIN campaign_recipients cr ON c.id = cr.campaign_id
GROUP BY t.id, t.name, t.category;

-- View for dashboard statistics
CREATE VIEW dashboard_stats_view AS
SELECT 
    u.id as user_id,
    COALESCE(SUM(ca.total_sent), 0) as total_sent,
    COALESCE(AVG(ca.open_rate), 0) as avg_open_rate,
    COUNT(c.id) FILTER (WHERE c.status = 'scheduled') as active_scheduled,
    MIN(c.scheduled_at) FILTER (WHERE c.status = 'scheduled') as next_campaign_at
FROM users u
LEFT JOIN campaigns c ON u.id = c.user_id
LEFT JOIN campaign_analytics ca ON c.id = ca.campaign_id
GROUP BY u.id;

-- View for contact group counts
CREATE VIEW contact_group_counts_view AS
SELECT 
    cg.id,
    cg.user_id,
    cg.name,
    COUNT(cgm.id) as contact_count
FROM contact_groups cg
LEFT JOIN contact_group_members cgm ON cg.id = cgm.group_id
GROUP BY cg.id, cg.user_id, cg.name;

-- ============================================================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================================================

COMMENT ON TABLE users IS 'Application users for authentication and ownership';
COMMENT ON TABLE templates IS 'WhatsApp message templates with Meta approval status';
COMMENT ON TABLE contacts IS 'Customer/recipient contact information';
COMMENT ON TABLE contact_groups IS 'Segments for organizing contacts';
COMMENT ON TABLE campaigns IS 'Broadcast message campaigns';
COMMENT ON TABLE campaign_recipients IS 'Individual message tracking per recipient';
COMMENT ON TABLE campaign_analytics IS 'Aggregated campaign performance metrics';
COMMENT ON TABLE notifications IS 'User notification inbox';
COMMENT ON TABLE whatsapp_connections IS 'Encrypted WhatsApp Business API credentials';
COMMENT ON TABLE import_jobs IS 'Track bulk contact import operations';

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
