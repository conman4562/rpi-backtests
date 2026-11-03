# rpi-backtests

rpi-backtests is a community project for students. Our goal is to make backtests easier to find and use when studying. Students can help by uploading materials from their previous classes.

## Status

The project is early in development. Our initial page prototypes are in `initial_designs/`. We are using React for the frontend, Springboot for the backend, and an R2 bucket for storing the PDFs. Browsing, searching, and uploading are in progress.

## Project goals

- Centralize backtests
  - Put as many backtests as possible in one place so students don't have to search for them, or go in person.
- Ease of use
  - Search and sort by course, professor, semester. Embedded PDF viewer with solutions and blank exams.
- Accessibility
  - Support screen readers and other forms of disability support. Support for mobile users. No required login for viewing PDFs.
- User contributions
  - Students will be able to and encouraged to upload their own backtests, with additional organizational information (course, prof, sem) with checks for duplicate uploads. Any names will be anonymized and given answers can be stripped if necessary.
- Maintainability
  - Keep PDF files in the R2 bucket. Searchable course and backtest metadata in PostgreSQL. Use storage efficiently. Build community support for beyond our time working on this project.

## Tech stack

| Area | Tech | Purpose |
| --- | --- | --- |
| Frontend | React | Builds browser interface
| | Typescript | Write frontend code with typechecking
| | Vite | Build the frontend, runs dev server
| | npm | Manage dependencies, runs commands
| | Bootstrap | layout utilities and UI components
| Backend | Springboot (Java 21) | Build api that will handle requests from the frontend
| | Maven | Manage Java dependencies, build backend
| | PostgreSQL | Database of searchable courses and backtest metadata
| | Cloudflare R2 | PDF storage
| Code checks | ESLint | Check code style, check react mistakes
| | Typescript | Checks types

## Local setup
- [In progress]
- pull code from GitHub
```
cd frontend
npm ci
npm run dev
```

## Contributing

### Commits:
- Loosely follow: https://www.conventionalcommits.org (doesn't need to be exact)
- Summary:
```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```
- Example:
```
(title) fix: (frontend) correct search input spacing

(body) fixed by changing spacing in stylesheet
```


### Branches
- Name: \<header\>/\<your name (optional)\>/\<feature\>
    - Examples: 
    - frontend/connor/static-pages
    - backend/zach/pdf-uploads
- Commit to your branch until you are ready to merge.
- If you are working on a frontend/backend sub branch, merge into the frontend/backend branch (no PR), then make a PR into main.
### Pull Requests (PRs)
- Requires at least one LGTM from a core dev before merging.
- Format:
```
Title: Feature/fix name
Answer the following questions/prompts:
What changed? Why?

How was it tested?

```
