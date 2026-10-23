import { useRef } from 'react'
import Header from '../components/Header'
import { DiscordIcon, XIcon } from '../components/Icons'
import logo from '../assets/rpi-backtests-logo.png'
import { useBouncingBodies } from '../hooks/useBouncingBodies'
import './HomePage.css'

// TODO: replace with real LinkedIn URLs
const developers = [
  { initials: 'WW', name: 'Weihao Wu', linkedin: '#' },
  { initials: 'EL', name: 'Eric Lin', linkedin: '#' },
  { initials: 'CK', name: 'Connor Kostiew', linkedin: '#' },
  { initials: 'ZL', name: 'Zachary Lin', linkedin: '#' },
]

// Repeat the list within each marquee group so a group is always wider than the screen
const marqueeCards = [...developers, ...developers]

const features = [
  'Actively Maintained',
  'No Account Required',
  'Regularly Moderated',
  'Free And Open Source',
]

// Like the DVD logo, the bouncing logo's frame changes color every time it hits a wall.
const logoColors = ['#d6001c', '#2563eb', '#16a34a', '#d97706', '#9333ea', '#0891b2']
let logoColorIndex = 0

function cycleLogoColor(el: HTMLElement) {
  logoColorIndex = (logoColorIndex + 1) % logoColors.length
  el.style.setProperty('--logo-color', logoColors[logoColorIndex])
}

function HomePage() {
  const heroRef = useRef<HTMLDivElement>(null)
  const featureFieldRef = useRef<HTMLUListElement>(null)

  useBouncingBodies(heroRef, { speed: 120, onWallHit: cycleLogoColor })
  useBouncingBodies(featureFieldRef, { speed: 90, collide: true })

  return (
    <>
      <Header>
        <a href="#" className="btn btn-primary">
          Login
        </a>
      </Header>

      <main className="home">
        <section className="hero">
          <div className="bounce-layer" ref={heroRef} aria-hidden="true">
            <img src={logo} alt="" className="bouncing-logo" data-bounce />
          </div>

          <div className="hero-stage">
            <h1 className="hero-title">
              RPI <span>backtests</span>
            </h1>
            <p className="hero-tagline">skip the trip to APO</p>

            <nav className="hero-links" aria-label="Community links">
              <a href="#" className="btn btn-outline">
                <DiscordIcon />
                Discord
              </a>
              <a href="#" className="btn btn-outline">
                Privacy Policy
              </a>
              <a href="#" className="btn btn-outline">
                <XIcon />
                X.com
              </a>
            </nav>
          </div>
        </section>

        <section className="developers" aria-labelledby="dev-heading">
          <h2 id="dev-heading" className="section-label">
            Built by
          </h2>
          {/* Two identical groups scroll left by exactly one group width, so the loop is seamless */}
          <div className="marquee">
            <div className="marquee-track">
              {[0, 1].map((group) => (
                <ul key={group} className="marquee-group">
                  {marqueeCards.map((dev, i) => {
                    // Only the first copy of each developer is exposed to screen readers / tabbing
                    const isCopy = group > 0 || i >= developers.length
                    return (
                      <li key={i} className="marquee-item" aria-hidden={isCopy || undefined}>
                        <a
                          href={dev.linkedin}
                          className="dev-card"
                          tabIndex={isCopy ? -1 : undefined}
                        >
                          <span className="dev-avatar">{dev.initials}</span>
                          <span className="dev-info">
                            <span className="dev-name">{dev.name}</span>
                            <span className="dev-link">LinkedIn &rarr;</span>
                          </span>
                        </a>
                      </li>
                    )
                  })}
                </ul>
              ))}
            </div>
          </div>
        </section>

        <section className="features" aria-labelledby="features-heading">
          <h2 id="features-heading" className="section-label">
            Why use it
          </h2>
          <ul className="feature-field" ref={featureFieldRef}>
            {features.map((label) => (
              <li key={label} className="feature-pill" data-bounce>
                {label}
              </li>
            ))}
          </ul>
        </section>
      </main>

      <footer className="site-footer">
        <div className="footer-logos">
          <a href="#" className="footer-logo" aria-label="RCOS">
            RCOS
          </a>
          <a href="#" className="footer-logo" aria-label="Discord">
            <DiscordIcon />
          </a>
        </div>
        <a href="https://github.com/conman4562/rpi-backtests" className="footer-repo">
          github.com/conman4562/rpi-backtests
        </a>
        <p className="footer-copy">&copy; 2026 RPI-Backtests Inc.</p>
      </footer>
    </>
  )
}

export default HomePage
