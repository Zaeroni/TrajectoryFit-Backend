import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { TrendPoint } from '../api/types'
import { weekdayShort } from '../lib/dates'

interface WeightChartProps {
  points: TrendPoint[]
  showAxes?: boolean
  showSmoothed?: boolean
  height?: number
}

/** Weight over time: a blue actual line, optionally a grey smoothed line. */
export function WeightChart({ points, showAxes = false, showSmoothed = false, height = 100 }: WeightChartProps) {
  const data = points.map((p) => ({
    label: weekdayShort(p.date),
    date: p.date,
    weight: p.weightLb,
    smoothed: p.weightSmoothedLb,
  }))
  const hasAny = data.some((d) => d.weight != null)

  if (!hasAny) {
    return <div className="muted" style={{ padding: '28px 0', textAlign: 'center' }}>No weight logged in this range</div>
  }

  return (
    <div style={{ position: 'relative', height }}>
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 6, right: 6, bottom: 0, left: showAxes ? 0 : 6 }}>
          {showAxes && <CartesianGrid stroke="#E5E5EA" vertical={false} />}
          <XAxis
            dataKey="label"
            hide={!showAxes}
            tick={{ fill: '#AEAEB2', fontSize: 10 }}
            axisLine={false}
            tickLine={false}
          />
          <YAxis
            hide={!showAxes}
            domain={['dataMin - 1', 'dataMax + 1']}
            tick={{ fill: '#AEAEB2', fontSize: 10 }}
            axisLine={false}
            tickLine={false}
            width={34}
          />
          {showAxes && <Tooltip formatter={(v: number) => `${v} lb`} labelStyle={{ color: '#6E6E73' }} />}
          {showSmoothed && (
            <Line type="monotone" dataKey="smoothed" stroke="#AEAEB2" strokeWidth={2} dot={false} connectNulls isAnimationActive={false} />
          )}
          <Line type="monotone" dataKey="weight" stroke="#0A84FF" strokeWidth={2} dot={{ r: 2 }} connectNulls isAnimationActive={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}

interface LevelBarsProps {
  /** One entry per day: a fill color and a short label. */
  segments: { color: string; label: string }[]
}

/** A row of equal-height colored bars — used for protein/training level history. */
export function LevelBars({ segments }: LevelBarsProps) {
  return (
    <div style={{ display: 'flex', gap: 4, alignItems: 'flex-end', height: 90 }}>
      {segments.map((s, i) => (
        <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
          <div style={{ width: '100%', height: 56, background: s.color, borderRadius: 3 }} />
          <span style={{ fontSize: 9, color: '#AEAEB2' }}>{s.label}</span>
        </div>
      ))}
    </div>
  )
}
