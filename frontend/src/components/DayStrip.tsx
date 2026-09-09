import { useEffect, useRef } from 'react'
import { fromISO, MONTH_ABBR } from '../lib/dates'

interface Props {
  dates: string[]
  selected: string
  onSelect: (iso: string) => void
  datesWithData: Set<string>
}

/** Horizontal, scrollable day picker; keeps the selected day centered. */
export function DayStrip({ dates, selected, onSelect, datesWithData }: Props) {
  const wrapRef = useRef<HTMLDivElement>(null)
  const selectedRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    const wrap = wrapRef.current
    const cell = selectedRef.current
    if (wrap && cell) {
      wrap.scrollLeft = cell.offsetLeft - wrap.clientWidth / 2 + cell.clientWidth / 2
    }
  }, [selected, dates])

  return (
    <div className="date-strip" ref={wrapRef}>
      {dates.map((iso) => {
        const d = fromISO(iso)
        const isSel = iso === selected
        return (
          <button
            key={iso}
            ref={isSel ? selectedRef : undefined}
            className={`date-cell${isSel ? ' selected' : ''}`}
            onClick={() => onSelect(iso)}
            aria-pressed={isSel}
          >
            <span className="dm">{MONTH_ABBR[d.getMonth()]}</span>
            <span className="dd">{d.getDate()}</span>
            {datesWithData.has(iso) && <span className="dot" />}
          </button>
        )
      })}
    </div>
  )
}
