# TrajectoryFit

A muscle-preservation monitoring backend for people on GLP-1 weight-loss medications. It
tracks a few behavioral signals (weight-loss rate, protein intake, resistance training, and
optional body composition) and computes a **lean-mass-loss risk score** — flagging when a
weight-loss trajectory suggests muscle is being lost along with fat.

The product isn't the logging, it's the interpretation: *where is your body heading, and are
you at risk right now.* It runs on synthetic data only (no real users), which keeps the full
engineering story without the health-data legal exposure.

> Design rationale — why rules before ML, why tiered logging, why body comp is a confirming
> signal — lives in the project design docs. Read those before changing `RiskService`; several
> decisions were made deliberately against the more obvious alternative.

## Stack

- Java 21, Spring Boot 3.3, Gradle (wrapper pinned to 8.10.2)
- PostgreSQL 16
- Forecasting: stubbed (`/forecast`), planned as a separate model service in Phase 2

## What's built

- **`RiskService`** — the transparent, literature-sourced rules engine (`src/main/java/com/trajectoryfit/risk/`).
  Framework-free, so it compiles and validates with plain `javac`, independent of Spring or a database.
- **Archetype validation** — the scoring logic is validated against five synthetic behavioral
  profiles, reproducing the reference scores exactly (see below).
- **Schema** (`db/schema.sql`) — star schema, tiered protein logging, `(user_id, logged_on)`
  indexes, JSONB `risk_scores`.
- **Seed generator** (`db/seed_generator.py`) — 20 users across the five archetypes, each tagged
  with its expected band, doubling as an integration fixture.
- **Endpoints** — CRUD for users and each log type, plus the core `/risk`, `/trends`, and the
  `/forecast` stub.

## Scoring, in one paragraph

Over the last 7 calendar days: weight-loss rate vs. a ~1%/week threshold (strongest signal, cap 40),
protein adherence vs. a 1.2–1.6 g/kg target (cap 40), resistance-session count (0→30, 1→15, ≥2→0;
**cardio does not count**), and — only when two lean-mass readings exist — a body-composition
modifier (−10 holding / +15 declining). These roll into a 0–100 score and a Low/Moderate/High band,
with per-factor sub-scores so the app can explain *why*. Every threshold is configurable
(`trajectoryfit.risk.*`); defaults reproduce the validated reference.

## Validate the scoring (no database needed)

```bash
find src/main/java/com/trajectoryfit/risk -name '*.java' > /tmp/core.args
javac -d build/classes-core @/tmp/core.args
java -cp build/classes-core com.trajectoryfit.risk.archetype.ArchetypeValidationRunner
```

Expected:

| archetype | band | score | note |
|---|---|---|---|
| crash_dieter | High | 100 | |
| doing_it_right | Low | 6 | |
| cardio_only | Moderate | 56 | training named as the driver |
| slow_and_steady | Low | 0 | |
| brand_new | Low | 0 | partial-data flagged, training not penalized |

The same checks run as JUnit tests: `./gradlew test`.

## Run it

Start Postgres (host port 5433), apply the schema, and seed:

```bash
docker compose up -d db
psql "postgresql://trajectoryfit:trajectoryfit@localhost:5433/trajectoryfit" -f db/schema.sql
DB_URL="postgresql://trajectoryfit:trajectoryfit@localhost:5433/trajectoryfit" python3 db/seed_generator.py --apply
```

Run the app:

```bash
./gradlew bootRun
```

Or build and run the whole stack in containers (`docker compose up` — then seed as above against
localhost:5433).

## Endpoints

CRUD:

- `POST /api/users`, `GET /api/users/{id}`
- `POST /api/weigh-ins`, `GET /api/users/{id}/weigh-ins`
- `POST /api/nutrition`, `GET /api/users/{id}/nutrition`
- `POST /api/workouts`, `GET /api/users/{id}/workouts`
- `POST /api/body-comp`, `GET /api/users/{id}/body-comp`

Core:

- `GET /api/users/{id}/risk` — score, band, per-factor breakdown, sustained-risk note (`?date=` to
  preview a specific week)
- `GET /api/users/{id}/trends?range=30` — charted time series (weight + smoothed, protein, training)
- `GET /api/users/{id}/forecast` — Phase-2 stub returning the stable response contract

The API faces imperial units (weight/lean mass in lb, height in inches); values are stored in
metric and converted at the boundary. Input bounds: weight 60–700 lb, body fat 3–70%, lean mass
20–500 lb, protein 0–500 g/meal.

Example:

```bash
curl "http://localhost:8080/api/users/<id>/risk"
```
