.PHONY: build run

build:
	@docker build -f Dockerfile.dev -t rpi-backtests-backend .

run:
	@docker run --rm --env-file .env -p 8080:8080 --name rpi-backtests-backend-container rpi-backtests-backend

stop:
	@docker stop rpi-backtests-backend

remove:
	@docker rm rpi-backtests-backend