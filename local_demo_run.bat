@echo off
setlocal
echo ========================================================
echo   Smart Waste System - Local Demo Mode (No Docker)
echo ========================================================
echo.
echo This script will start the Backend (H2 Database) and Frontend locally.
echo Prerequisites: Java 17+ and Node.js 18+ must be installed.
echo.

REM Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH.
    pause
    exit /b 1
)

REM Check Node
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is not installed or not in PATH.
    pause
    exit /b 1
)

echo [1/2] Starting Backend (Spring Boot with H2)...
if not exist logs mkdir logs
start "SmartWaste-Backend" cmd /c "cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local > ..\logs\backend_console.log 2>&1"

echo Waiting for Backend to initialize...
timeout /t 15

echo.
echo [2/2] Starting Frontend (Vite)...
cd frontend
if not exist "node_modules" (
    echo Installing frontend dependencies...
    call npm install
)
start "SmartWaste-Frontend" cmd /c "npm run dev"

echo.
echo ========================================================
echo   System is starting locally!
echo   - Frontend: http://localhost:5173
echo   - Backend API: http://localhost:8080
echo   - H2 Console: http://localhost:8080/h2-console
echo ========================================================
pause
