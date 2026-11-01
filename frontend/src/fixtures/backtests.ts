import type { Backtest } from '../types/backtest'

export const sampleBacktests: Backtest[] = [
  {
    id: '5f0527a6-f82b-4f0f-a515-5c319a547ff6',
    courseCode: 'CSCI-1200',
    title: 'Exam 1',
    professor: 'Sample instructor',
    semester: 'FALL',
    year: 2025,
    documents: {
      blank: {
        available: true,
        primaryDocumentId: '38ecfa7e-cc6c-46c7-9dc8-08f094e8471c',
      },
      solutions: { available: false },
    },
  },
]
