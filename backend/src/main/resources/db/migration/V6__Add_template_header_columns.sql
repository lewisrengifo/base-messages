-- Add header support for document templates
ALTER TABLE templates ADD COLUMN IF NOT EXISTS header_type VARCHAR(20) NOT NULL DEFAULT 'TEXT' CHECK (header_type IN ('TEXT', 'DOCUMENT'));
ALTER TABLE templates ADD COLUMN IF NOT EXISTS header_handle VARCHAR(255);
ALTER TABLE templates ADD COLUMN IF NOT EXISTS header_document_url VARCHAR(500);
ALTER TABLE templates ADD COLUMN IF NOT EXISTS header_document_key VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_templates_header_type ON templates(header_type);
