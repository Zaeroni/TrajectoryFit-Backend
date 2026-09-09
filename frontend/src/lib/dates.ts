export const MONTH_ABBR = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
export const WEEKDAY_ABBR = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

const pad2 = (n: number) => (n < 10 ? `0${n}` : `${n}`)

export function toISO(d: Date): string {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`
}

export function todayISO(): string {
  return toISO(new Date())
}

export function fromISO(iso: string): Date {
  const [y, m, d] = iso.split('-').map(Number)
  return new Date(y, m - 1, d)
}

export function addDaysISO(iso: string, n: number): string {
  const d = fromISO(iso)
  d.setDate(d.getDate() + n)
  return toISO(d)
}

/** The seven trailing dates ending on (and including) the anchor, oldest first. */
export function trailingWeek(anchorISO: string): string[] {
  const out: string[] = []
  for (let i = 6; i >= 0; i--) out.push(addDaysISO(anchorISO, -i))
  return out
}

export function shortLabel(iso: string): string {
  const d = fromISO(iso)
  return `${MONTH_ABBR[d.getMonth()]} ${d.getDate()}`
}

export function weekdayShort(iso: string): string {
  return WEEKDAY_ABBR[fromISO(iso).getDay()]
}
