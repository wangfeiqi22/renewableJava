# Smart Waste Management System - Quick Start (PowerShell)

param (
    [switch]$Version
)

# 1. Handle Version Check
if ($Version) {
    Write-Output "v4.1.0"
    exit 0
}

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  Smart Waste Management System - Quick Start"
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

# 2. Check Docker
Write-Host "Checking Docker status..." -NoNewline
try {
    $dockerInfo = docker info 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Docker returned error code $LASTEXITCODE"
    }
    Write-Host " [OK]" -ForegroundColor Green
} catch {
    Write-Host " [FAILED]" -ForegroundColor Red
    Write-Host ""
    Write-Host "ERROR: Docker is not running or not installed." -ForegroundColor Red
    Write-Host "Please start 'Docker Desktop' application and wait for it to initialize." -ForegroundColor Yellow
    Write-Host "Once Docker is running, try this script again."
    exit 1
}

Write-Host "[1/3] Deploying services..." -ForegroundColor Green

# Try docker-compose first, then docker compose
if (Get-Command docker-compose -ErrorAction SilentlyContinue) {
    docker-compose up --build -d
} else {
    docker compose up --build -d
}

if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker Compose failed."
    exit 1
}

Write-Host ""
Write-Host "[2/3] Waiting for services to initialize..." -ForegroundColor Green

# 3. Health Check Loop (Max 75s)
$retries = 0
$max_retries = 15
$url = "http://localhost:8080/health"

do {
    $retries++
    Start-Sleep -Seconds 5
    
    try {
        $response = Invoke-WebRequest -Uri $url -Method Get -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            $success = $true
            break
        }
    } catch {
        # Ignore errors and retry
    }
    
    Write-Host "  ... waiting for backend ($retries/$max_retries)" -NoNewline
    Write-Host "`r" -NoNewline
} while ($retries -lt $max_retries)

if (-not $success) {
    Write-Error "`nHealth check timed out."
    exit 1
}

# Success
Write-Host ""
Write-Host "[3/3] Verification Passed." -ForegroundColor Green
Write-Host ""
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  System is running!"
Write-Host "  - Frontend: http://localhost"
Write-Host "  - API Docs: http://localhost:8080/swagger-ui/index.html"
Write-Host ""
Write-Host "  Service started successfully, listening on port: 8080"
Write-Host "========================================================" -ForegroundColor Cyan
exit 0
