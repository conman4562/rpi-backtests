import type { Backtest } from '../../types/backtest'

// Stand-in for GET /admin/backtests/pending until the page is connected to the backend
function hoursAgo(hours: number) {
  return new Date(Date.now() - hours * 60 * 60 * 1000).toISOString()
}

export const mockPendingBacktests: Backtest[] = [
  { id: '1', courseCode: 'CSCI-1100', semester: 'FALL', year: 2025, professor: 'Uzma Mushtaque', testType: '1', state: 'BLANK', uploader: 'anonymous', createdAt: hoursAgo(52) },
  { id: '2', courseCode: 'CSCI-1100', semester: 'FALL', year: 2025, professor: 'Uzma Mushtaque', testType: '1', state: 'SOLUTION', uploader: 'anonymous', createdAt: hoursAgo(51) },
  { id: '3', courseCode: 'ENGR-2090', semester: 'FALL', year: 2020, professor: 'A S Shahsavari', testType: '3', state: 'SOLUTION', uploader: 'wuw7', createdAt: hoursAgo(30) },
  { id: '4', courseCode: 'MATH-2010', semester: 'SPRING', year: 2024, professor: null, testType: '2', state: 'BLANK', uploader: 'anonymous', createdAt: hoursAgo(20) },
  { id: '5', courseCode: 'PHYS-1100', semester: 'SPRING', year: 2023, professor: 'Vincent Meunier', testType: 'Final', state: 'FILLED', uploader: 'linz4', createdAt: hoursAgo(9) },
  { id: '6', courseCode: 'CSCI-1200', semester: null, year: null, professor: 'Barb Cutler', testType: null, state: 'BLANK', uploader: 'anonymous', createdAt: hoursAgo(5) },
  { id: '7', courseCode: 'MATH-2010', semester: 'SUMMER', year: 2025, professor: 'Kristen Bennett', testType: '1', state: 'BLANK', uploader: 'kostic', createdAt: hoursAgo(2) },
  { id: '8', courseCode: 'CHEM-1100', semester: 'FALL', year: 2024, professor: 'Linda McGown', testType: '2', state: 'SOLUTION', uploader: 'anonymous', createdAt: hoursAgo(0.5) },
]
