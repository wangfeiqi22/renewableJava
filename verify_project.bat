@echo off
echo ===================================================
echo   Smart Waste Management System - Verification
echo ===================================================

echo [1/3] Checking Backend Structure...
if exist "backend\src\main\java\com\renewable\ai\AiCustomerServiceApplication.java" (
    echo   [OK] Entry point found.
) else (
    echo   [FAIL] Backend entry point missing!
)
if exist "backend\src\main\resources\application.properties" (
    echo   [OK] Configuration found.
) else (
    echo   [FAIL] Backend config missing!
)

echo.
echo [2/3] Checking Frontend Structure...
if exist "frontend\package.json" (
    echo   [OK] package.json found.
) else (
    echo   [FAIL] package.json missing!
)
if exist "frontend\vite.config.js" (
    echo   [OK] Vite config found.
) else (
    echo   [FAIL] Vite config missing!
)
if exist "frontend\index.html" (
    echo   [OK] index.html found.
) else (
    echo   [FAIL] index.html missing!
)

echo.
echo [3/3] Checking Deployment Config...
if exist "docker-compose.yml" (
    echo   [OK] docker-compose.yml found.
) else (
    echo   [FAIL] docker-compose.yml missing!
)

echo.
echo ===================================================
echo   Verification Complete. 
echo   If all checks passed, you can run:
echo     docker-compose up --build
echo ===================================================
pause
