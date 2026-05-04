ALTER TABLE templates ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_templates_deleted_at ON templates(deleted_at);
