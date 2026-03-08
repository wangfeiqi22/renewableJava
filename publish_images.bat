@echo off
setlocal

REM Configuration
set REGISTRY_URL=myregistry.example.com
set VERSION=v4.1.0
set PROJECT_NAME=smart-waste

echo ========================================================
echo   Docker Image Publisher - %PROJECT_NAME% %VERSION%
echo ========================================================
echo.

REM 1. Build Images
echo [1/3] Building Images...
docker-compose build
if %ERRORLEVEL% NEQ 0 goto :error

REM 2. Tag Images
echo.
echo [2/3] Tagging Images...
docker tag renewable_backend:latest %REGISTRY_URL%/%PROJECT_NAME%/backend:%VERSION%
docker tag renewable_backend:latest %REGISTRY_URL%/%PROJECT_NAME%/backend:latest

docker tag renewable_frontend:latest %REGISTRY_URL%/%PROJECT_NAME%/frontend:%VERSION%
docker tag renewable_frontend:latest %REGISTRY_URL%/%PROJECT_NAME%/frontend:latest

REM 3. Push Images (Optional - requires login)
echo.
echo [3/3] Pushing Images to %REGISTRY_URL%...
echo (Ensure you have run 'docker login %REGISTRY_URL%' first)
echo.
choice /M "Do you want to push images now?"
if %ERRORLEVEL% NEQ 1 goto :done

docker push %REGISTRY_URL%/%PROJECT_NAME%/backend:%VERSION%
docker push %REGISTRY_URL%/%PROJECT_NAME%/backend:latest
docker push %REGISTRY_URL%/%PROJECT_NAME%/frontend:%VERSION%
docker push %REGISTRY_URL%/%PROJECT_NAME%/frontend:latest

if %ERRORLEVEL% NEQ 0 goto :error

:done
echo.
echo ========================================================
echo   Success! Images are ready.
echo ========================================================
goto :eof

:error
echo.
echo [ERROR] Operation failed.
exit /b 1
