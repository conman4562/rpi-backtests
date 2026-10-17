import type { ReactNode } from 'react'
import logo from '../assets/rpi-backtests-logo.png'
import './Header.css'

// Shared top bar. The right side is passed in so logged-in pages can
// swap the Login button for Upload / AI Practice / Public archive / avatar.
function Header({ children }: { children?: ReactNode }) {
  return (
    <header className="site-header">
      <a href="#" className="brand">
        <img src={logo} alt="RPI Backtests" className="brand-logo" />
      </a>
      <nav className="header-actions">{children}</nav>
    </header>
  )
}

export default Header
