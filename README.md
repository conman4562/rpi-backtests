# rpi-backtests

rpi-backtests aims to give RPI students a central place to find past tests. It is community sourced meaning students are welcome and encouraged to contribute their own backtests to help out future students.

## Status

Currently early in development.
Planned features
- Back test viewer, organized by class, year, and professor
- Back test uploader: students can upload the tests they took to help out the community

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
fix: (frontend) correct search input spacing
```


### Branches
- Name: \<header\>/\<your name (optional)\>/\<feature\>
    - Examples: 
    - frontend/connor/static-pages
    - backend/zach/django
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