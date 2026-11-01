// Fixture-only review data; these fields are not part of the public Backtest DTO.
type DocumentVersionFixture = {
  id: string
  backtestId: string
  kind: 'BLANK' | 'SOLUTIONS'
  reviewState: 'APPROVED' | 'PENDING'
}

export const sampleDocumentVersions: DocumentVersionFixture[] = [
  {
    id: '38ecfa7e-cc6c-46c7-9dc8-08f094e8471c',
    backtestId: '5f0527a6-f82b-4f0f-a515-5c319a547ff6',
    kind: 'BLANK',
    reviewState: 'APPROVED',
  },
  {
    id: 'a4fc3058-218f-45a7-8ace-000000000002',
    backtestId: 'c355fea1-b948-4ea6-ae86-000000000002',
    kind: 'SOLUTIONS',
    reviewState: 'APPROVED',
  },
  {
    id: 'a4fc3058-218f-45a7-8ace-000000000003',
    backtestId: 'c355fea1-b948-4ea6-ae86-000000000003',
    kind: 'BLANK',
    reviewState: 'APPROVED',
  },
  {
    id: 'a4fc3058-218f-45a7-8ace-000000000004',
    backtestId: 'c355fea1-b948-4ea6-ae86-000000000003',
    kind: 'SOLUTIONS',
    reviewState: 'APPROVED',
  },
  {
    id: 'a4fc3058-218f-45a7-8ace-000000000005',
    backtestId: 'c355fea1-b948-4ea6-ae86-000000000004',
    kind: 'BLANK',
    reviewState: 'APPROVED',
  },
  {
    id: 'a4fc3058-218f-45a7-8ace-000000000006',
    backtestId: 'c355fea1-b948-4ea6-ae86-000000000004',
    kind: 'BLANK',
    reviewState: 'APPROVED',
  },
  {
    id: 'a4fc3058-218f-45a7-8ace-000000000007',
    backtestId: 'c355fea1-b948-4ea6-ae86-000000000005',
    kind: 'BLANK',
    reviewState: 'APPROVED',
  },
  {
    id: 'a4fc3058-218f-45a7-8ace-000000000008',
    backtestId: 'c355fea1-b948-4ea6-ae86-000000000005',
    kind: 'SOLUTIONS',
    reviewState: 'PENDING',
  },
  {
    id: 'a4fc3058-218f-45a7-8ace-000000000009',
    backtestId: 'c355fea1-b948-4ea6-ae86-000000000004',
    kind: 'BLANK',
    reviewState: 'PENDING',
  },
]
