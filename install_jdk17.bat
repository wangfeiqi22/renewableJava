@echo off
setlocal

echo ========================================================
echo   Installing OpenJDK 17 (Microsoft Build)
echo ========================================================
echo.

set "DOWNLOAD_URL=https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.msi"
set "INSTALLER_PATH=%TEMP%\openjdk-17.msi"

echo [1/3] Downloading OpenJDK 17...
powershell -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%INSTALLER_PATH%'"
if %errorlevel% neq 0 (
    echo [ERROR] Download failed. Please check your internet connection.
    exit /b 1
)

echo.
echo [2/3] Installing...
echo Please follow the installation wizard (Click Next -> Next -> Install).
echo IMPORTANT: Make sure to select "Set JAVA_HOME variable" if available.
msiexec /i "%INSTALLER_PATH%"

echo.
echo [3/3] Verifying installation...
java -version
if %errorlevel% equ 0 (
    echo.
    echo [SUCCESS] Java 17 installed successfully!
    echo You may need to restart your terminal for changes to take effect.
) else (
    echo.
    echo [WARNING] Java command not found yet. Please restart your terminal/computer.
)

pause
