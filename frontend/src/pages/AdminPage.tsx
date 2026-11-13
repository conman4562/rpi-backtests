import { useEffect, useMemo, useRef, useState } from 'react'
import type { KeyboardEvent, ReactNode } from 'react'
import Header from '../components/Header'
import { CheckIcon, CloseIcon, FileIcon, SearchIcon } from '../components/Icons'
import type { Backtest, TestState } from '../types/backtest'
import { mockPendingBacktests } from './admin/mockPendingBacktests'
import './AdminPage.css'

type SortOrder = 'oldest' | 'newest'

// Not stored by the backend yet; shown so the reviewer has to make a deliberate choice
const rejectReasons = [
  'Wrong course or details in the file name',
  'Unreadable or incomplete scan',
  'Duplicate of an existing test',
  'Not an exam',
  'Other',
]

const stateLabels: Record<TestState, string> = {
  BLANK: 'Blank',
  FILLED: 'Filled in',
  SOLUTION: 'Solutions',
}

function formatTerm(test: Backtest) {
  if (!test.semester || !test.year) return 'Unknown term'
  return `${test.semester.charAt(0)}${test.semester.slice(1).toLowerCase()} ${test.year}`
}

function formatTestType(test: Backtest) {
  if (!test.testType) return 'Unknown test'
  return /^\d+$/.test(test.testType) ? `Test ${test.testType}` : test.testType
}

function timeAgo(iso: string) {
  const minutes = Math.max(1, Math.round((Date.now() - Date.parse(iso)) / 60000))
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.round(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  return `${Math.round(hours / 24)}d ago`
}

function plural(count: number, word: string) {
  return `${count} ${word}${count === 1 ? '' : 's'}`
}

// Opens a <dialog> as a modal, which gives focus trapping and Esc-to-close for free
function useModal(open: boolean) {
  const ref = useRef<HTMLDialogElement>(null)
  useEffect(() => {
    const dialog = ref.current
    if (!dialog) return
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])
  return ref
}

function Modal({
  open,
  onClose,
  className,
  label,
  children,
  onKeyDown,
}: {
  open: boolean
  onClose: () => void
  className: string
  label: string
  children: ReactNode
  onKeyDown?: (e: KeyboardEvent<HTMLDialogElement>) => void
}) {
  const ref = useModal(open)
  return (
    <dialog
      ref={ref}
      className={className}
      aria-label={label}
      onClose={onClose}
      onKeyDown={onKeyDown}
      // A click on the dialog element itself (not its content) is a click on the backdrop
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      {open && <div className="modal-content">{children}</div>}
    </dialog>
  )
}

function AdminPage() {
  const [queue, setQueue] = useState<Backtest[]>(mockPendingBacktests)
  const [query, setQuery] = useState('')
  const [course, setCourse] = useState('all')
  const [sort, setSort] = useState<SortOrder>('oldest')
  const [selected, setSelected] = useState<Set<string>>(new Set())
  const [previewId, setPreviewId] = useState<string | null>(null)
  const [rejectIds, setRejectIds] = useState<string[] | null>(null)
  const [rejectReason, setRejectReason] = useState(rejectReasons[0])
  const [reviewed, setReviewed] = useState({ approved: 0, rejected: 0 })
  const [toast, setToast] = useState<string | null>(null)

  const courses = useMemo(() => [...new Set(queue.map((t) => t.courseCode))].sort(), [queue])

  const visible = useMemo(() => {
    const q = query.trim().toLowerCase()
    return queue
      .filter((t) => course === 'all' || t.courseCode === course)
      .filter(
        (t) =>
          !q ||
          t.courseCode.toLowerCase().includes(q) ||
          (t.professor ?? '').toLowerCase().includes(q) ||
          t.uploader.toLowerCase().includes(q),
      )
      .sort((a, b) => {
        const diff = Date.parse(a.createdAt) - Date.parse(b.createdAt)
        return sort === 'oldest' ? diff : -diff
      })
  }, [queue, query, course, sort])

  const previewTest = queue.find((t) => t.id === previewId)
  const isFiltered = query.trim() !== '' || course !== 'all'
  const allVisibleSelected = visible.length > 0 && visible.every((t) => selected.has(t.id))
  const someVisibleSelected = visible.some((t) => selected.has(t.id))

  useEffect(() => {
    if (!toast) return
    const timer = setTimeout(() => setToast(null), 3500)
    return () => clearTimeout(timer)
  }, [toast])

  function removeFromQueue(ids: string[]) {
    setQueue((q) => q.filter((t) => !ids.includes(t.id)))
    setSelected((s) => new Set([...s].filter((id) => !ids.includes(id))))
  }

  // TODO: POST /admin/backtests/{id}/approve for each id
  function approve(ids: string[]) {
    removeFromQueue(ids)
    setReviewed((r) => ({ ...r, approved: r.approved + ids.length }))
    setToast(`Approved ${plural(ids.length, 'upload')}`)
  }

  function startReject(ids: string[]) {
    setRejectReason(rejectReasons[0])
    setRejectIds(ids)
  }

  // TODO: POST /admin/backtests/{id}/reject for each id
  function confirmReject() {
    if (!rejectIds) return
    removeFromQueue(rejectIds)
    setReviewed((r) => ({ ...r, rejected: r.rejected + rejectIds.length }))
    setToast(`Rejected and deleted ${plural(rejectIds.length, 'upload')}`)
    setRejectIds(null)
  }

  function toggleSelected(id: string) {
    setSelected((s) => {
      const next = new Set(s)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  function toggleAllVisible() {
    setSelected((s) => {
      const next = new Set(s)
      visible.forEach((t) => (allVisibleSelected ? next.delete(t.id) : next.add(t.id)))
      return next
    })
  }

  function clearFilters() {
    setQuery('')
    setCourse('all')
  }

  // A / R act on the test open in the preview, like most moderation tools
  function onPreviewKeyDown(e: KeyboardEvent<HTMLDialogElement>) {
    if (!previewTest || rejectIds || e.metaKey || e.ctrlKey || e.altKey) return
    if (e.key === 'a') approve([previewTest.id])
    if (e.key === 'r') startReject([previewTest.id])
  }

  return (
    <>
      <Header>
        <span className="admin-badge">Admin</span>
        <a href="#" className="btn btn-outline">
          Back to site
        </a>
      </Header>

      <main className="admin">
        <div className="admin-head">
          <div>
            <h1 className="admin-title">Review queue</h1>
            <p className="admin-subtitle">
              Uploads stay hidden from students until they're approved. Rejecting deletes the file.
            </p>
          </div>

          <dl className="admin-stats">
            <div className="stat">
              <dt>Pending</dt>
              <dd>{queue.length}</dd>
            </div>
            <div className="stat">
              <dt>Approved</dt>
              <dd>{reviewed.approved}</dd>
            </div>
            <div className="stat">
              <dt>Rejected</dt>
              <dd>{reviewed.rejected}</dd>
            </div>
          </dl>
        </div>

        <div className="admin-toolbar">
          <label className="search-field">
            <SearchIcon />
            <span className="visually-hidden">Search uploads</span>
            <input
              type="search"
              placeholder="Search course, professor or uploader"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
          </label>
          <label className="select-field">
            <span className="visually-hidden">Course</span>
            <select value={course} onChange={(e) => setCourse(e.target.value)}>
              <option value="all">All courses</option>
              {courses.map((code) => (
                <option key={code} value={code}>
                  {code}
                </option>
              ))}
            </select>
          </label>
          <label className="select-field">
            <span className="visually-hidden">Sort</span>
            <select value={sort} onChange={(e) => setSort(e.target.value as SortOrder)}>
              <option value="oldest">Oldest first</option>
              <option value="newest">Newest first</option>
            </select>
          </label>
        </div>

        <section className="queue" aria-label="Pending uploads">
          {visible.length > 0 && (
            <div className="queue-header">
              <label className="row-check">
                <input
                  type="checkbox"
                  checked={allVisibleSelected}
                  ref={(el) => {
                    if (el) el.indeterminate = someVisibleSelected && !allVisibleSelected
                  }}
                  onChange={toggleAllVisible}
                />
                <span className="visually-hidden">Select all shown uploads</span>
              </label>
              <span>
                {isFiltered
                  ? `Showing ${visible.length} of ${queue.length}`
                  : plural(queue.length, 'upload')}
              </span>
              <span className="queue-header-uploader">Uploaded by</span>
              <span className="visually-hidden">Actions</span>
            </div>
          )}

          {visible.length > 0 ? (
            <ul className="queue-list">
              {visible.map((test) => {
                const title = `${test.courseCode} ${formatTestType(test)}`
                return (
                  <li key={test.id} className={`queue-row${selected.has(test.id) ? ' is-selected' : ''}`}>
                    <label className="row-check">
                      <input
                        type="checkbox"
                        checked={selected.has(test.id)}
                        onChange={() => toggleSelected(test.id)}
                      />
                      <span className="visually-hidden">Select {title}</span>
                    </label>

                    <button type="button" className="row-main" onClick={() => setPreviewId(test.id)}>
                      <span className="row-file">
                        <FileIcon />
                      </span>
                      <span className="row-text">
                        <span className="row-title">
                          <strong>{test.courseCode}</strong>
                          <span>{formatTestType(test)}</span>
                          <span className={`state-badge state-${test.state.toLowerCase()}`}>
                            {stateLabels[test.state]}
                          </span>
                        </span>
                        <span className="row-meta">
                          {test.professor ?? 'Unknown professor'} · {formatTerm(test)}
                        </span>
                      </span>
                    </button>

                    <span className="row-uploader">
                      <span>{test.uploader}</span>
                      <time dateTime={test.createdAt}>{timeAgo(test.createdAt)}</time>
                    </span>

                    <div className="row-actions">
                      <button
                        type="button"
                        className="icon-btn icon-btn-approve"
                        aria-label={`Approve ${title}`}
                        title="Approve"
                        onClick={() => approve([test.id])}
                      >
                        <CheckIcon />
                      </button>
                      <button
                        type="button"
                        className="icon-btn icon-btn-reject"
                        aria-label={`Reject ${title}`}
                        title="Reject"
                        onClick={() => startReject([test.id])}
                      >
                        <CloseIcon />
                      </button>
                    </div>
                  </li>
                )
              })}
            </ul>
          ) : queue.length === 0 ? (
            <div className="queue-empty">
              <span className="queue-empty-icon">
                <CheckIcon />
              </span>
              <h2>All caught up</h2>
              <p>New uploads will show up here for review.</p>
            </div>
          ) : (
            <div className="queue-empty">
              <h2>No uploads match</h2>
              <p>Try a different search or course.</p>
              <button type="button" className="btn btn-outline" onClick={clearFilters}>
                Clear filters
              </button>
            </div>
          )}
        </section>
      </main>

      {selected.size > 0 && (
        <div className="bulk-bar" role="region" aria-label="Bulk actions">
          <span className="bulk-count">{selected.size} selected</span>
          <button type="button" className="btn-link" onClick={() => setSelected(new Set())}>
            Clear
          </button>
          <span className="bulk-spacer" />
          <button type="button" className="btn btn-outline btn-sm" onClick={() => startReject([...selected])}>
            <CloseIcon />
            Reject
          </button>
          <button type="button" className="btn btn-approve btn-sm" onClick={() => approve([...selected])}>
            <CheckIcon />
            Approve
          </button>
        </div>
      )}

      <Modal
        open={!!previewTest}
        onClose={() => setPreviewId(null)}
        className="drawer"
        label="Upload preview"
        onKeyDown={onPreviewKeyDown}
      >
        {previewTest && (
          <>
            <div className="drawer-head">
              <div>
                <p className="drawer-eyebrow">{previewTest.courseCode}</p>
                <h2>
                  {formatTestType(previewTest)} · {stateLabels[previewTest.state]}
                </h2>
              </div>
              <button
                type="button"
                className="icon-btn"
                aria-label="Close preview"
                onClick={() => setPreviewId(null)}
              >
                <CloseIcon />
              </button>
            </div>

            {/* TODO: embed the PDF from GET /backtests/files?testId= */}
            <div className="pdf-placeholder">
              <FileIcon />
              <span>PDF preview appears here once the page is connected</span>
            </div>

            <dl className="drawer-details">
              <div>
                <dt>Professor</dt>
                <dd>{previewTest.professor ?? 'Unknown'}</dd>
              </div>
              <div>
                <dt>Term</dt>
                <dd>{formatTerm(previewTest)}</dd>
              </div>
              <div>
                <dt>Uploaded by</dt>
                <dd>{previewTest.uploader}</dd>
              </div>
              <div>
                <dt>Uploaded</dt>
                <dd>{new Date(previewTest.createdAt).toLocaleString()}</dd>
              </div>
            </dl>

            <div className="drawer-actions">
              <p className="drawer-keys">
                <kbd>A</kbd> approve <kbd>R</kbd> reject <kbd>Esc</kbd> close
              </p>
              <button type="button" className="btn btn-outline" onClick={() => startReject([previewTest.id])}>
                <CloseIcon />
                Reject
              </button>
              <button type="button" className="btn btn-approve" onClick={() => approve([previewTest.id])}>
                <CheckIcon />
                Approve
              </button>
            </div>
          </>
        )}
      </Modal>

      <Modal
        open={!!rejectIds}
        onClose={() => setRejectIds(null)}
        className="confirm-dialog"
        label="Confirm rejection"
      >
        {rejectIds && (
          <form
            method="dialog"
            onSubmit={(e) => {
              e.preventDefault()
              confirmReject()
            }}
          >
            <h2>Reject {rejectIds.length === 1 ? 'this upload' : plural(rejectIds.length, 'upload')}?</h2>
            <p className="confirm-warning">
              The {rejectIds.length === 1 ? 'file is' : 'files are'} permanently deleted from storage.
            </p>

            <fieldset className="reason-list">
              <legend>Reason</legend>
              {rejectReasons.map((reason) => (
                <label key={reason} className="reason-option">
                  <input
                    type="radio"
                    name="reason"
                    value={reason}
                    checked={rejectReason === reason}
                    onChange={() => setRejectReason(reason)}
                  />
                  {reason}
                </label>
              ))}
            </fieldset>

            <div className="confirm-actions">
              <button type="button" className="btn btn-outline" onClick={() => setRejectIds(null)}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary">
                Reject and delete
              </button>
            </div>
          </form>
        )}
      </Modal>

      <div className="toast-region" aria-live="polite">
        {toast && <div className="toast">{toast}</div>}
      </div>
    </>
  )
}

export default AdminPage
