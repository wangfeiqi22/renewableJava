@echo off
setlocal

if "%1"=="--version" (
    echo v4.1.0
    exit /b 0
)

echo ========================================================
echo   Smart Waste Management System - Quick Start
echo ========================================================
echo.

docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is NOT running or not installed.
    echo Please start Docker Desktop and try again.
    exit /b 1
)

echo [1/3] Deploying services...
if not exist logs mkdir logs
echo Starting services... > logs\startup.log
docker-compose up --build -d >> logs\startup.log 2>&1
if %errorlevel% neq 0 (
    echo [RETRY] Trying 'docker compose'...
    docker compose up --build -d >> logs\startup.log 2>&1
)

if %errorlevel% neq 0 (
    echo [ERROR] Docker Compose failed. Check logs\startup.log for details.
    type logs\startup.log
    exit /b 1
)

echo.
echo [2/3] Waiting for services to initialize...

set retries=0

:loop
set /a retries+=1
if %retries% geq 15 (
    echo [ERROR] Health check timed out.
    exit /b 1
)

timeout /t 5 >nul
curl -s -f http://localhost:8080/health >nul
if %errorlevel% equ 0 goto :success

echo ... waiting for backend (%retries%/15)
goto :loop

:success
echo.
echo [3/3] Verification Passed.
echo.
echo ========================================================
echo   System is running!
echo   - Frontend: http://localhost
echo   - API Docs: http://localhost:8080/swagger-ui/index.html
echo.
echo   Service started successfully, listening on port: 8080
echo ========================================================
exit /b 0
