// Mirrors the backend's BacktestResponseObject
export type Semester = 'FALL' | 'SPRING' | 'SUMMER'
export type TestState = 'BLANK' | 'FILLED' | 'SOLUTION'

export interface Backtest {
  id: string
  courseCode: string
  semester: Semester | null
  year: number | null
  professor: string | null
  testType: string | null
  state: TestState
  uploader: string
  createdAt: string
}
