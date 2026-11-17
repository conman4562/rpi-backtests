.PHONY: build dev run stop remove download-dependencies

build:
	@docker build -f Dockerfile.dev -t rpi-backtests-frontend .

run:
	@docker run --rm --env-file .env -p 5173:5173 --name rpi-backtests-frontend-container rpi-backtests-frontend

dev:
	@npm run dev

stop:
	@docker stop rpi-backtests-frontend-container

remove:
	@docker rm rpi-backtests-frontend-container

download-dependencies:
	@npm ci 
