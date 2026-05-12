-- ============================================================================
-- Add 'manual' value to audience_type enum for manual contact selection
-- ============================================================================

DO $$
BEGIN
    -- Add 'manual' value to audience_type enum if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum
        WHERE enumtypid = 'audience_type'::regtype
        AND enumlabel = 'manual'
    ) THEN
        ALTER TYPE audience_type ADD VALUE 'manual';
    END IF;
END $$;
