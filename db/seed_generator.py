#!/usr/bin/env python3
"""
Archetype-driven synthetic data generator for TrajectoryFit.

Creates 20 users across five behavioral profiles (4 users each). Each user is tagged with
the risk band it is expected to score into, so the seed data doubles as an integration-test
fixture: running RiskService over a seeded user's logs should classify it into that band.

Three weeks of daily logs are generated per user (ending today) so the trailing-7-day window
reproduces the documented archetype, and so trends and sustained-risk have history to work
with. The five logging tiers/behaviors are spread across archetypes to exercise the schema:
crash/cardio/brand_new use quick, doing_it_right uses precise grams, slow_and_steady uses
per-meal estimate.

All measurements are written in METRIC (kg), matching the schema; the archetype definitions
below are in pounds and converted on the way out.

Usage:
    python3 db/seed_generator.py                 # print SQL to stdout
    python3 db/seed_generator.py --apply         # apply via psql using $DB_URL
    python3 db/seed_generator.py > db/seed.sql   # save SQL to a file
"""

import argparse
import datetime as dt
import os
import subprocess
import sys
import uuid

LB_PER_KG = 2.2046
DAYS = 21                      # three weeks of history
USERS_PER_ARCHETYPE = 4
TODAY = dt.date.today()


def lb_to_kg(lb):
    return round(lb / LB_PER_KG, 2)


def sql_str(value):
    if value is None:
        return "NULL"
    return "'" + str(value).replace("'", "''") + "'"


# Each archetype: end weight (today, lb), daily loss (lb/day, = weekly drop / 6),
# a 7-slot protein pattern (index 6 == today), a 7-slot training pattern, the logging
# tier used, optional body-comp readings [(days_ago, lean_lb)], and profile fields.
ARCHETYPES = {
    "crash_dieter": {
        "expected_band": "High",
        "end_weight": 191.6, "daily_loss": 1.40,        # ~4.2%/week
        "protein": ["low", "low", "low", "low", "low", "good", "low"],
        "training": ["rest", "rest", "rest", "rest", "rest", "rest", "rest"],
        "tier": "quick",
        "body_comp": [(14, 160.6), (0, 158.0)],         # declining lean mass
        "medication": "semaglutide", "logging": "quick",
    },
    "doing_it_right": {
        "expected_band": "Low",
        "end_weight": 178.5, "daily_loss": 0.25,        # ~0.83%/week
        "protein": ["good", "good", "low", "good", "good", "good", "good"],
        "training": ["resistance", "rest", "resistance", "rest", "resistance", "rest", "rest"],
        "tier": "precise",
        "body_comp": None,
        "medication": "tirzepatide", "logging": "precise",
    },
    "cardio_only": {
        "expected_band": "Moderate",
        "end_weight": 187.4, "daily_loss": 0.4333,      # ~1.37%/week
        "protein": ["good", "good", "low", "good", "good", "low", "good"],
        "training": ["cardio", "cardio", "rest", "cardio", "cardio", "cardio", "rest"],
        "tier": "quick",
        "body_comp": None,
        "medication": "semaglutide", "logging": "quick",
    },
    "slow_and_steady": {
        "expected_band": "Low",
        "end_weight": 164.0, "daily_loss": 0.1667,      # ~0.61%/week
        "protein": ["good", "good", "good", "good", "good", "good", "good"],
        "training": ["resistance", "rest", "rest", "resistance", "rest", "rest", "rest"],
        "tier": "estimate",
        "body_comp": [(14, 150.0), (0, 150.0)],         # holding steady
        "medication": "other", "logging": "estimate",
    },
    "brand_new": {
        "expected_band": "Low",
        "end_weight": 200.0, "daily_loss": 0.05,        # barely moving, and barely logged
        "protein": None,                                 # handled specially (sparse)
        "training": None,
        "tier": "quick",
        "body_comp": None,
        "medication": "none", "logging": "quick",
    },
}

# Precise-tier gram splits that normalize to each level for a ~80 kg user
# (target ~97..130 g/day): 'good' lands inside the range, 'low' below it.
PRECISE_GRAMS = {
    "good": {"breakfast": 40, "lunch": 40, "dinner": 35},   # 115 g -> good
    "low": {"breakfast": 30, "lunch": 40},                  # 70 g  -> low
}


def generate():
    lines = ["BEGIN;"]
    for key, cfg in ARCHETYPES.items():
        for idx in range(USERS_PER_ARCHETYPE):
            lines.extend(generate_user(key, cfg, idx))
    lines.append("COMMIT;")
    return "\n".join(lines)


def generate_user(key, cfg, idx):
    uid = uuid.uuid4()
    name = f"{key.replace('_', ' ').title()} #{idx + 1}"
    # A small per-user weight offset keeps the four users distinct without crossing bands.
    offset = idx * 2.0
    goal_lb = round(cfg["end_weight"] - 15, 1)

    out = [f"-- {name} (expected {cfg['expected_band']})"]
    out.append(
        "INSERT INTO users (id, name, height_cm, goal_weight_kg, medication, "
        "logging_preference, body_comp_access, archetype, expected_band) VALUES ("
        f"'{uid}', {sql_str(name)}, 175.0, {lb_to_kg(goal_lb)}, "
        f"{sql_str(cfg['medication'])}, {sql_str(cfg['logging'])}, "
        f"'{{}}', {sql_str(key)}, {sql_str(cfg['expected_band'])});"
    )

    if key == "brand_new":
        out.extend(brand_new_logs(uid, cfg, offset))
    else:
        out.extend(full_logs(uid, key, cfg, offset))

    if cfg["body_comp"]:
        for days_ago, lean_lb in cfg["body_comp"]:
            day = TODAY - dt.timedelta(days=days_ago)
            out.append(
                "INSERT INTO body_comp_logs (user_id, logged_on, lean_mass_kg, source) VALUES ("
                f"'{uid}', '{day}', {lb_to_kg(lean_lb + offset)}, 'gym_scan');"
            )
    return out


def full_logs(uid, key, cfg, offset):
    out = []
    for k in range(DAYS):                       # k = days ago (0 == today)
        day = TODAY - dt.timedelta(days=k)
        pos = 6 - (k % 7)                        # slot within the week; today -> 6

        weight_lb = cfg["end_weight"] + cfg["daily_loss"] * k + offset
        out.append(
            "INSERT INTO weight_logs (user_id, logged_on, weight_kg) VALUES ("
            f"'{uid}', '{day}', {lb_to_kg(weight_lb)});"
        )

        level = cfg["protein"][pos]
        out.extend(nutrition_rows(uid, day, cfg["tier"], level))

        ttype = cfg["training"][pos]
        out.append(
            "INSERT INTO training_logs (user_id, logged_on, type, duration_min) VALUES ("
            f"'{uid}', '{day}', {sql_str(ttype)}, {45 if ttype != 'rest' else 0});"
        )
    return out


def brand_new_logs(uid, cfg, offset):
    """Sparse: two weights in the last week, one protein day, no training at all."""
    out = []
    for k, w in [(6, 200.0), (3, 199.7)]:
        day = TODAY - dt.timedelta(days=k)
        out.append(
            "INSERT INTO weight_logs (user_id, logged_on, weight_kg) VALUES ("
            f"'{uid}', '{day}', {lb_to_kg(w + offset)});"
        )
    protein_day = TODAY - dt.timedelta(days=6)
    out.extend(nutrition_rows(uid, protein_day, "quick", "good"))
    return out


def nutrition_rows(uid, day, tier, level):
    if level is None:
        return []
    if tier == "quick":
        return ["INSERT INTO nutrition_logs (user_id, logged_on, tier, meal, level) VALUES ("
                f"'{uid}', '{day}', 'quick', 'whole_day', {sql_str(level)});"]
    if tier == "estimate":
        rows = []
        for meal in ("breakfast", "lunch", "dinner", "snacks"):
            rows.append("INSERT INTO nutrition_logs (user_id, logged_on, tier, meal, level) VALUES ("
                        f"'{uid}', '{day}', 'estimate', {sql_str(meal)}, {sql_str(level)});")
        return rows
    if tier == "precise":
        rows = []
        for meal, grams in PRECISE_GRAMS[level].items():
            rows.append("INSERT INTO nutrition_logs (user_id, logged_on, tier, meal, grams) VALUES ("
                        f"'{uid}', '{day}', 'precise', {sql_str(meal)}, {grams});")
        return rows
    return []


def main():
    parser = argparse.ArgumentParser(description="Generate synthetic TrajectoryFit seed data.")
    parser.add_argument("--apply", action="store_true", help="apply the SQL via psql using $DB_URL")
    args = parser.parse_args()

    sql = generate()

    if args.apply:
        db_url = os.environ.get("DB_URL", "postgresql://trajectoryfit:trajectoryfit@localhost:5432/trajectoryfit")
        proc = subprocess.run(["psql", db_url, "-v", "ON_ERROR_STOP=1"],
                              input=sql, text=True)
        sys.exit(proc.returncode)
    else:
        print(sql)


if __name__ == "__main__":
    main()
