import { useLayoutEffect, type RefObject } from 'react'

type Body = {
  el: HTMLElement
  x: number
  y: number
  w: number
  h: number
  vx: number
  vy: number
  /** Pointer currently dragging this body, if any */
  pointerId: number | null
  /** Where inside the body it was grabbed, so it doesn't jump to the cursor */
  grabX: number
  grabY: number
  /** Recent pointer positions, used to work out the flick velocity on release */
  samples: { t: number; x: number; y: number }[]
}

type Options = {
  /** Speed in px/second */
  speed: number
  /** Whether bodies bounce off each other (not just the walls) */
  collide?: boolean
  /** Whether bodies can be grabbed, dragged and flicked with the mouse or a finger */
  draggable?: boolean
  /** Called whenever a body hits a wall of the container */
  onWallHit?: (el: HTMLElement) => void
}

// A flick is measured over the last ~100ms of movement and capped so a wild throw stays on screen.
const FLICK_WINDOW_MS = 100
const MAX_FLICK_SPEED = 1600
// How quickly a flicked body eases back to its cruising speed (higher = faster).
const SPEED_RELAX_RATE = 1.2

/**
 * Bounces every `[data-bounce]` element inside the container around its bounds,
 * DVD-screensaver style. Positions are written straight to `transform` each frame
 * so React never re-renders during the animation.
 */
export function useBouncingBodies(
  containerRef: RefObject<HTMLElement | null>,
  { speed, collide = false, draggable = false, onWallHit }: Options,
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
        pointerId: null,
        grabX: 0,
        grabY: 0,
        samples: [],
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
    // Dragging still works, but a released body stays where it was dropped.
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    const stopDragging = draggable
      ? enableDragging(container, bodies, () => ({ width, height }))
      : null

    if (reduceMotion) {
      return () => {
        stopDragging?.()
        resize.disconnect()
      }
    }

    let frame = 0
    let last = performance.now()

    const step = (now: number) => {
      // Cap dt so a backgrounded tab doesn't teleport everything on return.
      const dt = Math.min((now - last) / 1000, 0.05)
      last = now

      for (const b of bodies) {
        // A held body is positioned by the pointer, not by its velocity.
        if (b.pointerId !== null) continue
        relaxSpeed(b, speed, dt)
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
      stopDragging?.()
      resize.disconnect()
    }
  }, [containerRef, speed, collide, draggable, onWallHit])
}

/**
 * Lets each body be grabbed with a pointer and moved around the container. While held,
 * the body's velocity tracks the pointer (so it shoves others in collisions); on release
 * it keeps the pointer's recent velocity, which is what makes a flick throw it.
 * Returns a cleanup function that removes the listeners.
 */
function enableDragging(
  container: HTMLElement,
  bodies: Body[],
  getSize: () => { width: number; height: number },
) {
  const toLocal = (e: PointerEvent) => {
    const rect = container.getBoundingClientRect()
    // clientLeft/Top skip the container's border, since bodies are positioned inside it
    return {
      x: e.clientX - rect.left - container.clientLeft,
      y: e.clientY - rect.top - container.clientTop,
    }
  }

  const cleanups = bodies.map((b) => {
    const onDown = (e: PointerEvent) => {
      if (b.pointerId !== null || (e.pointerType === 'mouse' && e.button !== 0)) return
      e.preventDefault()
      const p = toLocal(e)
      b.pointerId = e.pointerId
      b.grabX = p.x - b.x
      b.grabY = p.y - b.y
      b.samples = [{ t: e.timeStamp, x: p.x, y: p.y }]
      b.vx = 0
      b.vy = 0
      b.el.setPointerCapture(e.pointerId)
      b.el.classList.add('is-held')
    }

    const onMove = (e: PointerEvent) => {
      if (e.pointerId !== b.pointerId) return
      const p = toLocal(e)
      const { width, height } = getSize()
      b.x = clamp(p.x - b.grabX, 0, Math.max(0, width - b.w))
      b.y = clamp(p.y - b.grabY, 0, Math.max(0, height - b.h))

      b.samples.push({ t: e.timeStamp, x: p.x, y: p.y })
      while (b.samples.length > 2 && e.timeStamp - b.samples[0].t > FLICK_WINDOW_MS) b.samples.shift()
      const first = b.samples[0]
      const elapsed = (e.timeStamp - first.t) / 1000
      if (elapsed > 0) {
        b.vx = (p.x - first.x) / elapsed
        b.vy = (p.y - first.y) / elapsed
      }
      render(b)
    }

    const onUp = (e: PointerEvent) => {
      if (e.pointerId !== b.pointerId) return
      b.pointerId = null
      b.el.classList.remove('is-held')
      // Holding still before letting go means a gentle drop, not a throw.
      const lastSample = b.samples[b.samples.length - 1]
      if (e.timeStamp - lastSample.t > FLICK_WINDOW_MS) {
        b.vx = 0
        b.vy = 0
      }
      const flick = Math.hypot(b.vx, b.vy)
      if (flick > MAX_FLICK_SPEED) {
        b.vx *= MAX_FLICK_SPEED / flick
        b.vy *= MAX_FLICK_SPEED / flick
      }
    }

    b.el.addEventListener('pointerdown', onDown)
    b.el.addEventListener('pointermove', onMove)
    b.el.addEventListener('pointerup', onUp)
    b.el.addEventListener('pointercancel', onUp)
    return () => {
      b.el.removeEventListener('pointerdown', onDown)
      b.el.removeEventListener('pointermove', onMove)
      b.el.removeEventListener('pointerup', onUp)
      b.el.removeEventListener('pointercancel', onUp)
    }
  })

  return () => cleanups.forEach((cleanup) => cleanup())
}

/** Eases a body's speed back toward `speed`, keeping its direction (a dropped body picks a random one). */
function relaxSpeed(b: Body, speed: number, dt: number) {
  const current = Math.hypot(b.vx, b.vy)
  if (current < 1) {
    const angle = Math.random() * Math.PI * 2
    b.vx = Math.cos(angle) * 1
    b.vy = Math.sin(angle) * 1
    return
  }
  const next = current + (speed - current) * (1 - Math.exp(-SPEED_RELAX_RATE * dt))
  b.vx *= next / current
  b.vy *= next / current
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
 * A body held by the pointer acts as immovable: the other body takes the whole
 * push and is knocked away at least as fast as the held one is moving.
 */
function collideAll(bodies: Body[]) {
  for (let i = 0; i < bodies.length; i++) {
    for (let j = i + 1; j < bodies.length; j++) {
      const a = bodies[i]
      const b = bodies[j]
      const aHeld = a.pointerId !== null
      const bHeld = b.pointerId !== null
      if ((aHeld && bHeld) || !overlaps(a, b)) continue

      // Share of the overlap each body moves to separate them
      const aShare = aHeld ? 0 : bHeld ? 1 : 0.5
      const bShare = 1 - aShare

      const overlapX = Math.min(a.x + a.w, b.x + b.w) - Math.max(a.x, b.x)
      const overlapY = Math.min(a.y + a.h, b.y + b.h) - Math.max(a.y, b.y)
      const axis = overlapX < overlapY ? 'x' : 'y'
      const overlap = axis === 'x' ? overlapX : overlapY
      const aCenter = axis === 'x' ? a.x + a.w / 2 : a.y + a.h / 2
      const bCenter = axis === 'x' ? b.x + b.w / 2 : b.y + b.h / 2
      const dir = aCenter < bCenter ? 1 : -1
      const v = axis === 'x' ? 'vx' : 'vy'

      a[axis] -= dir * overlap * aShare
      b[axis] += dir * overlap * bShare

      if (aHeld) {
        b[v] = dir * Math.max(Math.abs(b[v]), a[v] * dir)
      } else if (bHeld) {
        a[v] = -dir * Math.max(Math.abs(a[v]), -b[v] * dir)
      } else if ((a[v] - b[v]) * dir > 0) {
        // Only swap if they're moving toward each other, so they don't stick together.
        ;[a[v], b[v]] = [b[v], a[v]]
      }
    }
  }
}
