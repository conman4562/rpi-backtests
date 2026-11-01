type PdfAvailability =
  | { available: false }
  | { available: true; primaryDocumentId: string }

export type Backtest = {
  id: string
  courseCode: string
  title: string
  professor: string
  semester: 'FALL' | 'SPRING'
  year: number
  documents: {
    blank: PdfAvailability
    solutions: PdfAvailability
  }
}
