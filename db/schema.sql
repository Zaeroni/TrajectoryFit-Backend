-- TrajectoryFit schema
-- Star schema: a central users table with time-series log tables hanging off it.
--
-- Design decisions preserved from the brief:
--   * All measurements stored in METRIC (kg, cm). Imperial conversion happens only at
--     the presentation / scoring-input boundary, never in the database.
--   * Tiered protein logging (quick / estimate / precise) is modeled in one table via a
--     tier discriminator plus a CHECK that enforces the right columns per tier.
--   * Every log table is indexed on (user_id, logged_on) for the trailing-window queries
--     the risk engine runs.
--   * risk_scores stores contributing factors as JSONB and is a time series, so a user's
--     trajectory can be charted over time rather than recomputed on demand.
--
-- Apply with:  psql "$DB_URL" -f db/schema.sql

BEGIN;

DROP TABLE IF EXISTS risk_scores      CASCADE;
DROP TABLE IF EXISTS body_comp_logs   CASCADE;
DROP TABLE IF EXISTS training_logs    CASCADE;
DROP TABLE IF EXISTS nutrition_logs   CASCADE;
DROP TABLE IF EXISTS weight_logs      CASCADE;
DROP TABLE IF EXISTS users            CASCADE;

-- ---------------------------------------------------------------------------
-- users : the center of the star
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                TEXT        NOT NULL,
    height_cm           NUMERIC(5,1),
    goal_weight_kg      NUMERIC(5,2),
    -- GLP-1 medication is descriptive for now; appetite-suppression profiles are not yet
    -- reflected in scoring (a deliberate placeholder pending outcome data).
    medication          TEXT        NOT NULL DEFAULT 'none'
                          CHECK (medication IN ('semaglutide','tirzepatide','other','none')),
    logging_preference  TEXT        NOT NULL DEFAULT 'quick'
                          CHECK (logging_preference IN ('quick','estimate','precise')),
    -- Which body-comp sources the user has access to (may be empty).
    body_comp_access    TEXT[]      NOT NULL DEFAULT '{}',
    -- Synthetic-data bookkeeping: the archetype a seeded user was generated from and the
    -- risk band it is expected to score into. NULL for real users.
    archetype           TEXT,
    expected_band       TEXT        CHECK (expected_band IN ('Low','Moderate','High')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- weight_logs : one weight reading per day
-- ---------------------------------------------------------------------------
CREATE TABLE weight_logs (
    id          BIGSERIAL,
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    logged_on   DATE        NOT NULL,
    weight_kg   NUMERIC(5,2) NOT NULL CHECK (weight_kg BETWEEN 27 AND 318),  -- ~60..700 lb
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id),
    UNIQUE (user_id, logged_on)
);
CREATE INDEX idx_weight_logs_user_date ON weight_logs (user_id, logged_on);

-- ---------------------------------------------------------------------------
-- nutrition_logs : tiered protein logging
--   quick    -> one row, meal='whole_day', level set, grams NULL
--   estimate -> up to 4 rows (per meal), level set, grams NULL
--   precise  -> up to 4 rows (per meal), grams set, level NULL
-- ---------------------------------------------------------------------------
CREATE TABLE nutrition_logs (
    id          BIGSERIAL,
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    logged_on   DATE        NOT NULL,
    tier        TEXT        NOT NULL CHECK (tier IN ('quick','estimate','precise')),
    meal        TEXT        NOT NULL CHECK (meal IN ('whole_day','breakfast','lunch','dinner','snacks')),
    level       TEXT        CHECK (level IN ('low','good','high')),
    grams       NUMERIC(5,1) CHECK (grams BETWEEN 0 AND 500),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id),
    UNIQUE (user_id, logged_on, meal),
    -- Precise carries grams (no level); quick/estimate carry a level (no grams).
    CONSTRAINT nutrition_tier_shape CHECK (
        (tier = 'precise'  AND grams IS NOT NULL AND level IS NULL) OR
        (tier IN ('quick','estimate') AND level IS NOT NULL AND grams IS NULL)
    ),
    -- Quick is a whole-day rating; the per-meal tiers never use the whole_day slot.
    CONSTRAINT nutrition_meal_shape CHECK (
        (tier = 'quick' AND meal = 'whole_day') OR
        (tier IN ('estimate','precise') AND meal <> 'whole_day')
    )
);
CREATE INDEX idx_nutrition_logs_user_date ON nutrition_logs (user_id, logged_on);

-- ---------------------------------------------------------------------------
-- training_logs : one session (or explicit rest day) per day
--   Only 'resistance' protects muscle mass in this model; cardio does not count.
--   An explicit 'rest' row still means "training was logged that day".
-- ---------------------------------------------------------------------------
CREATE TABLE training_logs (
    id            BIGSERIAL,
    user_id       UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    logged_on     DATE        NOT NULL,
    type          TEXT        NOT NULL CHECK (type IN ('resistance','cardio','rest')),
    duration_min  INTEGER     CHECK (duration_min BETWEEN 0 AND 600),
    muscles       TEXT[],
    effort        TEXT        CHECK (effort IN ('light','moderate','hard')),
    cardio_type   TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id),
    UNIQUE (user_id, logged_on)
);
CREATE INDEX idx_training_logs_user_date ON training_logs (user_id, logged_on);

-- ---------------------------------------------------------------------------
-- body_comp_logs : optional, higher-engagement users
-- ---------------------------------------------------------------------------
CREATE TABLE body_comp_logs (
    id            BIGSERIAL,
    user_id       UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    logged_on     DATE        NOT NULL,
    body_fat_pct  NUMERIC(4,1) CHECK (body_fat_pct BETWEEN 3 AND 70),
    lean_mass_kg  NUMERIC(5,2) CHECK (lean_mass_kg BETWEEN 9 AND 227),  -- ~20..500 lb
    source        TEXT        CHECK (source IN ('gym_scan','smart_scale','paid_scan')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id),
    UNIQUE (user_id, logged_on),
    -- At least one of the two measurements must be present.
    CONSTRAINT body_comp_nonempty CHECK (body_fat_pct IS NOT NULL OR lean_mass_kg IS NOT NULL)
);
CREATE INDEX idx_body_comp_logs_user_date ON body_comp_logs (user_id, logged_on);

-- ---------------------------------------------------------------------------
-- risk_scores : derived time series, one snapshot per computed date
--   factors JSONB holds the per-signal sub-scores and statuses that explain the number.
-- ---------------------------------------------------------------------------
CREATE TABLE risk_scores (
    id            BIGSERIAL,
    user_id       UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    computed_for  DATE        NOT NULL,
    score         INTEGER     NOT NULL CHECK (score BETWEEN 0 AND 100),
    band          TEXT        NOT NULL CHECK (band IN ('Low','Moderate','High')),
    factors       JSONB       NOT NULL DEFAULT '{}'::jsonb,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id),
    UNIQUE (user_id, computed_for)
);
CREATE INDEX idx_risk_scores_user_date ON risk_scores (user_id, computed_for);

COMMIT;
