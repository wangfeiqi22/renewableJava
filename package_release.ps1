$ErrorActionPreference = "Stop"

$version = "v4.1.0"
$releaseDir = "release_smart_waste_$version"
$zipFile = "Smart_Waste_System_$version.zip"

Write-Host "1. Cleaning up previous release artifacts..."
if (Test-Path $releaseDir) { Remove-Item -Path $releaseDir -Recurse -Force }
if (Test-Path $zipFile) { Remove-Item -Path $zipFile -Force }

Write-Host "2. Creating release directory structure..."
New-Item -ItemType Directory -Path "$releaseDir\backend" | Out-Null
New-Item -ItemType Directory -Path "$releaseDir\frontend" | Out-Null

Write-Host "3. Copying Backend source..."
# Copy backend excluding target, .mvn, .idea, etc.
Get-ChildItem -Path "backend" -Exclude "target",".mvn",".idea",".git","*.iml" | Copy-Item -Destination "$releaseDir\backend" -Recurse

Write-Host "4. Copying Frontend source..."
# Copy frontend excluding node_modules, dist, .idea
Get-ChildItem -Path "frontend" -Exclude "node_modules","dist",".idea",".git" | Copy-Item -Destination "$releaseDir\frontend" -Recurse

Write-Host "5. Copying root configuration files..."
Copy-Item "docker-compose.yml" -Destination $releaseDir
Copy-Item "schema.sql" -Destination $releaseDir
Copy-Item "README.md" -Destination $releaseDir
Copy-Item "RELEASE.md" -Destination $releaseDir
Copy-Item "verify_project.bat" -Destination $releaseDir

Write-Host "6. Zipping release package..."
Compress-Archive -Path $releaseDir -DestinationPath $zipFile

Write-Host "=========================================="
Write-Host "  Release Package Created Successfully!"
Write-Host "  File: $zipFile"
Write-Host "=========================================="
