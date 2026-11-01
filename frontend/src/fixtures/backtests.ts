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
  {
    id: 'c355fea1-b948-4ea6-ae86-000000000002',
    courseCode: 'CSCI-2300',
    title: 'Exam 2',
    professor: 'Sample instructor',
    semester: 'SPRING',
    year: 2025,
    documents: {
      blank: { available: false },
      solutions: {
        available: true,
        primaryDocumentId: 'a4fc3058-218f-45a7-8ace-000000000002',
      },
    },
  },
  {
    id: 'c355fea1-b948-4ea6-ae86-000000000003',
    courseCode: 'MATH-1010',
    title: 'Exam 1',
    professor: 'Sample instructor',
    semester: 'FALL',
    year: 2025,
    documents: {
      blank: {
        available: true,
        primaryDocumentId: 'a4fc3058-218f-45a7-8ace-000000000003',
      },
      solutions: {
        available: true,
        primaryDocumentId: 'a4fc3058-218f-45a7-8ace-000000000004',
      },
    },
  },
  {
    // Additional versions must not create additional catalog entries.
    id: 'c355fea1-b948-4ea6-ae86-000000000004',
    courseCode: 'PHYS-1100',
    title: 'Exam 2',
    professor: 'Sample instructor',
    semester: 'SPRING',
    year: 2025,
    documents: {
      blank: {
        available: true,
        primaryDocumentId: 'a4fc3058-218f-45a7-8ace-000000000005',
      },
      solutions: { available: false },
    },
  },
  {
    // Pending solutions must not enable the public Solutions tab.
    id: 'c355fea1-b948-4ea6-ae86-000000000005',
    courseCode: 'CSCI-1100',
    title: 'Exam 1',
    professor: 'Sample instructor',
    semester: 'FALL',
    year: 2025,
    documents: {
      blank: {
        available: true,
        primaryDocumentId: 'a4fc3058-218f-45a7-8ace-000000000007',
      },
      solutions: { available: false },
    },
  },
]

// Exercise an unavailable detail screen without adding it to the public catalog.
export const unavailableBacktest: Backtest = {
  id: 'c355fea1-b948-4ea6-ae86-000000000006',
  courseCode: 'CSCI-2200',
  title: 'Exam 1',
  professor: 'Sample instructor',
  semester: 'FALL',
  year: 2025,
  documents: {
    blank: { available: false },
    solutions: { available: false },
  },
}
