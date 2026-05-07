-- Add missing columns to templates table for soft-delete and meta error tracking.
-- Uses IF NOT EXISTS to remain idempotent if previous migrations (V2, V3) were skipped.

ALTER TABLE templates ADD COLUMN IF NOT EXISTS meta_error TEXT;

ALTER TABLE templates ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_templates_deleted_at ON templates(deleted_at);
