@echo off
setlocal
set SCRIPT_DIR=%~dp0
if not exist "%SCRIPT_DIR%health-backend" (
  echo 后端目录不存在: %SCRIPT_DIR%health-backend
  exit /b 1
)
if not exist "%SCRIPT_DIR%health-frontend" (
  echo 前端目录不存在: %SCRIPT_DIR%health-frontend
  exit /b 1
)
start cmd /k "cd /d "%SCRIPT_DIR%health-backend" && mvn spring-boot:run"
start cmd /k "cd /d "%SCRIPT_DIR%health-frontend" && npm run dev"
echo 已启动后端和前端窗口。
endlocal
