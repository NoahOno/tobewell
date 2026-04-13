# Start both backend and frontend in separate PowerShell windows.
# Usage: Right-click and run with PowerShell, or execute from the repository root.

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendPath = Join-Path $scriptRoot 'health-backend'
$frontendPath = Join-Path $scriptRoot 'health-frontend'

function Start-WindowProcess($path, $command) {
    if (-not (Test-Path $path)) {
        Write-Host "目录不存在：$path" -ForegroundColor Red
        return
    }

    $escapedCommand = "Set-Location '$path'; $command"
    Start-Process powershell -ArgumentList ('-NoExit', $escapedCommand)
}

Write-Host '准备启动后端和前端...' -ForegroundColor Cyan
Start-WindowProcess $backendPath 'mvn spring-boot:run'
Start-WindowProcess $frontendPath 'npm run dev'
Write-Host '已打开后端和前端启动窗口。请查看新窗口中的运行日志。' -ForegroundColor Green
