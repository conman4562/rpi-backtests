import { useLayoutEffect, type RefObject } from 'react'

type Body = {
  el: HTMLElement
  x: number
  y: number
  w: number
  h: number
  vx: number
  vy: number
}

type Options = {
  /** Speed in px/second */
  speed: number
  /** Whether bodies bounce off each other (not just the walls) */
  collide?: boolean
  /** Called whenever a body hits a wall of the container */
  onWallHit?: (el: HTMLElement) => void
}

/**
 * Bounces every `[data-bounce]` element inside the container around its bounds,
 * DVD-screensaver style. Positions are written straight to `transform` each frame
 * so React never re-renders during the animation.
 */
export function useBouncingBodies(
  containerRef: RefObject<HTMLElement | null>,
  { speed, collide = false, onWallHit }: Options,
) {
  useLayoutEffect(() => {
    const container = containerRef.current
    if (!container) return

    const els = Array.from(container.querySelectorAll<HTMLElement>('[data-bounce]'))
    let width = container.clientWidth
    let height = container.clientHeight

    const bodies: Body[] = els.map((el) => {
      const angle = Math.random() * Math.PI * 2
      return {
        el,
        x: 0,
        y: 0,
        w: el.offsetWidth,
        h: el.offsetHeight,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
      }
    })

    placeWithoutOverlap(bodies, width, height)
    bodies.forEach(render)

    const resize = new ResizeObserver(() => {
      width = container.clientWidth
      height = container.clientHeight
      for (const b of bodies) {
        b.w = b.el.offsetWidth
        b.h = b.el.offsetHeight
        b.x = clamp(b.x, 0, Math.max(0, width - b.w))
        b.y = clamp(b.y, 0, Math.max(0, height - b.h))
        render(b)
      }
    })
    resize.observe(container)

    // Respect the OS "reduce motion" setting: keep the scattered layout, skip the animation.
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      return () => resize.disconnect()
    }

    let frame = 0
    let last = performance.now()

    const step = (now: number) => {
      // Cap dt so a backgrounded tab doesn't teleport everything on return.
      const dt = Math.min((now - last) / 1000, 0.05)
      last = now

      for (const b of bodies) {
        b.x += b.vx * dt
        b.y += b.vy * dt
        bounceOffWalls(b, width, height, onWallHit)
      }
      if (collide) collideAll(bodies)
      bodies.forEach(render)

      frame = requestAnimationFrame(step)
    }
    frame = requestAnimationFrame(step)

    return () => {
      cancelAnimationFrame(frame)
      resize.disconnect()
    }
  }, [containerRef, speed, collide, onWallHit])
}

function render(b: Body) {
  b.el.style.transform = `translate(${b.x}px, ${b.y}px)`
}

function clamp(v: number, min: number, max: number) {
  return Math.min(Math.max(v, min), max)
}

function overlaps(a: Body, b: Body) {
  return a.x < b.x + b.w && b.x < a.x + a.w && a.y < b.y + b.h && b.y < a.y + a.h
}

/** Random starting spots, retrying a few times per body to avoid starting overlapped. */
function placeWithoutOverlap(bodies: Body[], width: number, height: number) {
  bodies.forEach((b, i) => {
    for (let attempt = 0; attempt < 50; attempt++) {
      b.x = Math.random() * Math.max(0, width - b.w)
      b.y = Math.random() * Math.max(0, height - b.h)
      if (!bodies.slice(0, i).some((other) => overlaps(b, other))) break
    }
  })
}

function bounceOffWalls(
  b: Body,
  width: number,
  height: number,
  onWallHit?: (el: HTMLElement) => void,
) {
  const maxX = Math.max(0, width - b.w)
  const maxY = Math.max(0, height - b.h)
  let hit = false

  if (b.x < 0) {
    b.x = 0
    b.vx = Math.abs(b.vx)
    hit = true
  } else if (b.x > maxX) {
    b.x = maxX
    b.vx = -Math.abs(b.vx)
    hit = true
  }

  if (b.y < 0) {
    b.y = 0
    b.vy = Math.abs(b.vy)
    hit = true
  } else if (b.y > maxY) {
    b.y = maxY
    b.vy = -Math.abs(b.vy)
    hit = true
  }

  if (hit) onWallHit?.(b.el)
}

/**
 * Box-vs-box collisions between equal-mass bodies: push them apart along the
 * axis of least overlap, then swap velocities on that axis (elastic bounce).
 */
function collideAll(bodies: Body[]) {
  for (let i = 0; i < bodies.length; i++) {
    for (let j = i + 1; j < bodies.length; j++) {
      const a = bodies[i]
      const b = bodies[j]
      if (!overlaps(a, b)) continue

      const overlapX = Math.min(a.x + a.w, b.x + b.w) - Math.max(a.x, b.x)
      const overlapY = Math.min(a.y + a.h, b.y + b.h) - Math.max(a.y, b.y)

      if (overlapX < overlapY) {
        const dir = a.x + a.w / 2 < b.x + b.w / 2 ? 1 : -1
        a.x -= (dir * overlapX) / 2
        b.x += (dir * overlapX) / 2
        // Only swap if they're moving toward each other, so they don't stick together.
        if ((a.vx - b.vx) * dir > 0) [a.vx, b.vx] = [b.vx, a.vx]
      } else {
        const dir = a.y + a.h / 2 < b.y + b.h / 2 ? 1 : -1
        a.y -= (dir * overlapY) / 2
        b.y += (dir * overlapY) / 2
        if ((a.vy - b.vy) * dir > 0) [a.vy, b.vy] = [b.vy, a.vy]
      }
    }
  }
}
