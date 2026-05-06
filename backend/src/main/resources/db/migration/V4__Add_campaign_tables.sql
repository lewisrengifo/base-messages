-- ============================================================================
-- Campaign tables for Base Messages MVP (idempotent)
-- Safe to run on existing databases where tables may already exist.
-- ============================================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'campaign_status') THEN
        CREATE TYPE campaign_status AS ENUM ('draft', 'scheduled', 'sending', 'sent', 'canceled', 'failed');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'message_status') THEN
        CREATE TYPE message_status AS ENUM ('pending', 'sent', 'delivered', 'opened', 'clicked', 'bounced', 'failed');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'audience_type') THEN
        CREATE TYPE audience_type AS ENUM ('all', 'group', 'segment');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'schedule_type') THEN
        CREATE TYPE schedule_type AS ENUM ('immediate', 'scheduled');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'device_type') THEN
        CREATE TYPE device_type AS ENUM ('Mobile', 'Desktop', 'Tablet');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS campaigns (
    id serial4 PRIMARY KEY,
    user_id int4 NOT NULL,
    campaign_code varchar(20) NOT NULL UNIQUE,
    name varchar(100) NOT NULL,
    template_id int4 NOT NULL,
    status campaign_status DEFAULT 'draft'::campaign_status,
    audience_type audience_type NOT NULL,
    audience_group_id int4,
    recipient_count int4 DEFAULT 0,
    schedule_type schedule_type NOT NULL,
    scheduled_date date,
    scheduled_time time,
    scheduled_at timestamptz,
    sent_at timestamptz,
    estimated_cost numeric(10, 2) DEFAULT 0.00,
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    canceled_at timestamptz,
    canceled_reason text
);

CREATE TABLE IF NOT EXISTS campaign_recipients (
    id serial4 PRIMARY KEY,
    campaign_id int4 NOT NULL,
    contact_id int4 NOT NULL,
    status message_status DEFAULT 'pending'::message_status,
    sent_at timestamptz,
    delivered_at timestamptz,
    opened_at timestamptz,
    clicked_at timestamptz,
    failed_at timestamptz,
    failure_reason text,
    device_type device_type,
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(campaign_id, contact_id)
);

CREATE TABLE IF NOT EXISTS campaign_analytics (
    id serial4 PRIMARY KEY,
    campaign_id int4 NOT NULL UNIQUE,
    total_sent int4 DEFAULT 0,
    total_delivered int4 DEFAULT 0,
    total_opened int4 DEFAULT 0,
    total_clicked int4 DEFAULT 0,
    total_bounced int4 DEFAULT 0,
    total_failed int4 DEFAULT 0,
    open_rate numeric(5, 2) DEFAULT 0.00,
    click_rate numeric(5, 2) DEFAULT 0.00,
    bounce_rate numeric(5, 2) DEFAULT 0.00,
    last_updated_at timestamptz DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS campaign_analytics_timeline (
    id serial4 PRIMARY KEY,
    campaign_id int4 NOT NULL,
    time_bucket timestamptz NOT NULL,
    sent_count int4 DEFAULT 0,
    delivered_count int4 DEFAULT 0,
    opened_count int4 DEFAULT 0,
    UNIQUE(campaign_id, time_bucket)
);

CREATE TABLE IF NOT EXISTS campaign_top_links (
    id serial4 PRIMARY KEY,
    campaign_id int4 NOT NULL,
    label varchar(255) NOT NULL,
    url text NOT NULL,
    clicks int4 DEFAULT 0,
    ctr numeric(5, 2) DEFAULT 0.00,
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(campaign_id, url)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_campaigns_user_id ON campaigns(user_id);
CREATE INDEX IF NOT EXISTS idx_campaigns_status ON campaigns(status);
CREATE INDEX IF NOT EXISTS idx_campaigns_scheduled_at ON campaigns(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_campaigns_sent_at ON campaigns(sent_at);
CREATE INDEX IF NOT EXISTS idx_campaigns_template ON campaigns(template_id);
CREATE INDEX IF NOT EXISTS idx_campaigns_user_status ON campaigns(user_id, status);

CREATE INDEX IF NOT EXISTS idx_campaign_recipients_campaign ON campaign_recipients(campaign_id);
CREATE INDEX IF NOT EXISTS idx_campaign_recipients_contact ON campaign_recipients(contact_id);
CREATE INDEX IF NOT EXISTS idx_campaign_recipients_status ON campaign_recipients(status);

CREATE INDEX IF NOT EXISTS idx_campaign_analytics_campaign ON campaign_analytics(campaign_id);
CREATE INDEX IF NOT EXISTS idx_analytics_timeline_campaign ON campaign_analytics_timeline(campaign_id);
CREATE INDEX IF NOT EXISTS idx_analytics_timeline_time ON campaign_analytics_timeline(time_bucket);

-- ============================================================================
-- Foreign Keys (idempotent)
-- ============================================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'campaigns_template_id_fkey' AND table_name = 'campaigns') THEN
        ALTER TABLE campaigns ADD CONSTRAINT campaigns_template_id_fkey FOREIGN KEY (template_id) REFERENCES templates(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'campaigns_user_id_fkey' AND table_name = 'campaigns') THEN
        ALTER TABLE campaigns ADD CONSTRAINT campaigns_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'campaigns_audience_group_id_fkey' AND table_name = 'campaigns') THEN
        ALTER TABLE campaigns ADD CONSTRAINT campaigns_audience_group_id_fkey FOREIGN KEY (audience_group_id) REFERENCES contact_groups(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'campaign_recipients_campaign_id_fkey' AND table_name = 'campaign_recipients') THEN
        ALTER TABLE campaign_recipients ADD CONSTRAINT campaign_recipients_campaign_id_fkey FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'campaign_recipients_contact_id_fkey' AND table_name = 'campaign_recipients') THEN
        ALTER TABLE campaign_recipients ADD CONSTRAINT campaign_recipients_contact_id_fkey FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'campaign_analytics_campaign_id_fkey' AND table_name = 'campaign_analytics') THEN
        ALTER TABLE campaign_analytics ADD CONSTRAINT campaign_analytics_campaign_id_fkey FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'campaign_analytics_timeline_campaign_id_fkey' AND table_name = 'campaign_analytics_timeline') THEN
        ALTER TABLE campaign_analytics_timeline ADD CONSTRAINT campaign_analytics_timeline_campaign_id_fkey FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'campaign_top_links_campaign_id_fkey' AND table_name = 'campaign_top_links') THEN
        ALTER TABLE campaign_top_links ADD CONSTRAINT campaign_top_links_campaign_id_fkey FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE;
    END IF;
END $$;
