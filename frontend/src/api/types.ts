// Response/request shapes mirrored from the Spring Boot API.

export type Band = 'Low' | 'Moderate' | 'High'
export type Level = 'low' | 'good' | 'high'
export type Tier = 'quick' | 'estimate' | 'precise'
export type TrainingKind = 'resistance' | 'cardio' | 'rest'
export type Meal = 'breakfast' | 'lunch' | 'dinner' | 'snacks'

export interface User {
  id: string
  name: string
  heightIn: number | null
  goalWeightLb: number | null
  medication: string
  loggingPreference: Tier
  bodyCompAccess: string[]
  archetype: string | null
  expectedBand: Band | null
  createdAt: string | null
}

export interface FactorDto {
  factor: 'weight' | 'protein' | 'training' | 'body_comp'
  label: string
  points: number
  status: string
  detail: number | null
}

export interface RiskResponse {
  hasData: boolean
  score: number | null
  band: Band | null
  summary: string
  partialData: boolean
  biggestDriver: string | null
  sustainedRisk: boolean
  sustainedRiskNote: string | null
  anchor: string
  factors: FactorDto[]
}

export interface TrendPoint {
  date: string
  weightLb: number | null
  weightSmoothedLb: number | null
  proteinLevel: Level | null
  training: TrainingKind | null
}

export interface TrendsResult {
  rangeDays: number
  from: string
  to: string
  points: TrendPoint[]
  avgProteinAdherencePct: number | null
  resistanceSessionsLast7Days: number
}

export interface WeighIn {
  id: number
  loggedOn: string
  weightLb: number
}

export interface NutritionDay {
  loggedOn: string
  tier: Tier
  meals: { meal: string; level: Level | null; grams: number | null }[]
}

export interface Workout {
  id: number
  loggedOn: string
  type: TrainingKind
  durationMin: number | null
  muscles: string[]
  effort: string | null
  cardioType: string | null
}

export interface BodyComp {
  id: number
  loggedOn: string
  bodyFatPct: number | null
  leanMassLb: number | null
  source: string | null
}
