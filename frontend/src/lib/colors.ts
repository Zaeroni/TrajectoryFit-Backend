import type { Band, Level } from '../api/types'

/** CSS custom-property name for a band's accent color. */
export function bandColorVar(band: Band | null): string {
  switch (band) {
    case 'Low': return '--green'
    case 'Moderate': return '--yellow'
    case 'High': return '--red'
    default: return '--ink3'
  }
}

/** Resolved hex for a band (for chart fills, which can't read CSS vars). */
export function bandHex(band: Band | null): string {
  switch (band) {
    case 'Low': return '#34C759'
    case 'Moderate': return '#B8860B'
    case 'High': return '#FF3B30'
    default: return '#AEAEB2'
  }
}

export function levelHex(level: Level | null): string {
  switch (level) {
    case 'low': return '#FF3B30'
    case 'good': return '#34C759'
    case 'high': return '#B8860B'
    default: return '#E5E5EA'
  }
}

/** Badge class from a factor status string, matching the prototype's color logic. */
export function statusBadgeClass(status: string): string {
  const green = ['On track', 'Lean mass holding']
  const yellow = ['Slightly fast', 'Inconsistent', 'Slight lean mass dip']
  const red = ['Losing too fast', 'Below target', 'Losing lean mass']
  if (green.includes(status)) return 'badge-green'
  if (yellow.includes(status)) return 'badge-yellow'
  if (red.includes(status)) return 'badge-red'
  // "Nx this week" — green for >=2, yellow for 1, red for 0
  const m = status.match(/^(\d+)x this week$/)
  if (m) {
    const n = Number(m[1])
    return n >= 2 ? 'badge-green' : n === 1 ? 'badge-yellow' : 'badge-red'
  }
  return 'badge-neutral'
}
