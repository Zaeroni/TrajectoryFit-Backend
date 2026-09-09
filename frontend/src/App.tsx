import { useState } from 'react'
import { HomeIcon, LogIcon, TrendsIcon, TrainIcon } from './components/Icons'
import { Dashboard } from './screens/Dashboard'
import { FactorDetail } from './screens/FactorDetail'
import { HowItWorks } from './screens/HowItWorks'
import { Log } from './screens/Log'
import { Train } from './screens/Train'
import { Trends } from './screens/Trends'
import { useApp } from './state/AppContext'
import type { FactorKey, Nav, Screen } from './nav'

const NAV_ITEMS: { screen: Screen; label: string; Icon: typeof HomeIcon }[] = [
  { screen: 'home', label: 'Home', Icon: HomeIcon },
  { screen: 'log', label: 'Log', Icon: LogIcon },
  { screen: 'trends', label: 'Trends', Icon: TrendsIcon },
  { screen: 'train', label: 'Train', Icon: TrainIcon },
]

export default function App() {
  const { users, currentUser, setCurrentUserId, loadingUsers, usersError, toastMessage } = useApp()
  const [screen, setScreen] = useState<Screen>('home')
  const [prevScreen, setPrevScreen] = useState<Screen>('home')
  const [factorKey, setFactorKey] = useState<FactorKey>('protein')

  const nav: Nav = {
    go: (s) => { setPrevScreen(screen); setScreen(s) },
    openFactor: (k) => { setPrevScreen(screen); setFactorKey(k); setScreen('factor') },
    back: () => setScreen(prevScreen === 'factor' ? 'home' : prevScreen),
  }

  return (
    <div className="app">
      <header className="frame-header">
        <span className="brand">TrajectoryFit</span>
        <div className="user-switch">
          <select
            className="user-select"
            value={currentUser?.id ?? ''}
            onChange={(e) => setCurrentUserId(e.target.value)}
            aria-label="Select user"
          >
            {users.map((u) => (
              <option key={u.id} value={u.id}>
                {u.name}{u.expectedBand ? ` · ${u.expectedBand}` : ''}
              </option>
            ))}
          </select>
        </div>
      </header>

      <main className="content">
        {loadingUsers && <div className="spinner" />}
        {usersError && <div className="center-note">Couldn’t reach the API: {usersError}</div>}
        {!loadingUsers && !usersError && (
          <>
            {screen === 'home' && <Dashboard nav={nav} />}
            {screen === 'factor' && <FactorDetail factorKey={factorKey} nav={nav} />}
            {screen === 'log' && <Log />}
            {screen === 'trends' && <Trends />}
            {screen === 'train' && <Train nav={nav} />}
            {screen === 'howitworks' && <HowItWorks nav={nav} />}
          </>
        )}
      </main>

      <nav className="bottom-nav">
        {NAV_ITEMS.map(({ screen: s, label, Icon }) => {
          const active = screen === s || (s === 'home' && (screen === 'factor' || screen === 'howitworks'))
          return (
            <button key={s} className={`nav-item${active ? ' active' : ''}`} onClick={() => nav.go(s)}>
              <Icon style={{ width: 20, height: 20 }} />
              {label}
            </button>
          )
        })}
      </nav>

      <div className={`toast${toastMessage ? ' show' : ''}`}>{toastMessage}</div>
    </div>
  )
}
