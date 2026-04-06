# Graduation Project One-Click Start Script
# This script starts both the Spring Boot backend and the Vite frontend.

$BACKEND_DIR = "d:\Coding\graduationProj002\health-backend"
$FRONTEND_DIR = "d:\Coding\graduationProj002\health-frontend"

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "   Health Platform - One-Click Starter" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan

# Step 1: Cleanup (Optional but recommended for schema updates)
Write-Host "Cleaning up old database and stopping existing Java processes..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name java, javaw -ErrorAction SilentlyContinue
if ($javaProcesses) {
    $javaProcesses | Stop-Process -Force
    # Wait for the processes to fully exit and release file locks
    $javaProcesses | Wait-Process -Timeout 5 -ErrorAction SilentlyContinue
}

if (Test-Path "$BACKEND_DIR\health.db") {
    $retry = 0
    while ($retry -lt 5) {
        try {
            Remove-Item "$BACKEND_DIR\health.db" -Force -ErrorAction Stop
            Write-Host "Database reset successfully." -ForegroundColor Green
            break
        } catch {
            Start-Sleep -Seconds 1
            $retry++
        }
    }
    if (Test-Path "$BACKEND_DIR\health.db") {
        Write-Host "Warning: Could not delete health.db. Startup might fail." -ForegroundColor Red
    }
}

# Step 2: Start Backend
Write-Host "Starting Backend (Spring Boot)..." -ForegroundColor Blue
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$BACKEND_DIR'; mvn spring-boot:run" -WindowStyle Normal

# Wait a few seconds for backend to initialize
Write-Host "Waiting for backend to warm up..." -ForegroundColor Gray
Start-Sleep -Seconds 5

# Step 3: Start Frontend
Write-Host "Starting Frontend (Vite)..." -ForegroundColor Blue
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$FRONTEND_DIR'; npm run dev" -WindowStyle Normal

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "   Backend: http://localhost:8080" -ForegroundColor Green
Write-Host "   Frontend: http://localhost:3000" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "Both services are starting in separate windows. Close those windows to stop."
