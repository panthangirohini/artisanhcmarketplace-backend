@echo off
REM One-click launcher: starts the backend, waits until it's ready, then starts the frontend.
REM Double-click this file, or run .\start-app.cmd from a terminal.
title Artisan Handcraft launcher

set "ROOT=%~dp0"
set "FRONTEND=%ROOT%..\artisanhcmarketplace-frontend-main"
set "PATH=%PATH%;C:\Program Files\nodejs"

if not exist "%FRONTEND%\package.json" (
  echo Frontend folder not found next to the backend: %FRONTEND%
  pause
  exit /b 1
)

if not exist "%FRONTEND%\node_modules" (
  echo Installing frontend packages - first run only, takes a few minutes...
  pushd "%FRONTEND%"
  call npm install
  popd
)

call :ping 8080
if %errorlevel%==0 (
  echo Backend is already running.
) else (
  echo Starting backend in a new window...
  start "Artisan backend" /d "%ROOT%" cmd /k run-local.cmd
)

echo Waiting for the backend at http://localhost:8080 ...
set /a tries=0
:wait
call :ping 8080
if %errorlevel%==0 goto backend_ready
set /a tries+=1
if %tries% gtr 90 (
  echo The backend did not start. Check the "Artisan backend" window for errors.
  pause
  exit /b 1
)
timeout /t 2 /nobreak >nul
goto wait

:backend_ready
echo Backend is ready.

call :ping 3000
if %errorlevel%==0 (
  echo Frontend is already running - opening the browser.
  start "" http://localhost:3000
) else (
  echo Starting frontend in a new window - the browser will open automatically...
  start "Artisan frontend" /d "%FRONTEND%" cmd /k npm start
)

echo.
echo All set. Keep the "Artisan backend" and "Artisan frontend" windows open while you use the app.
echo Close them to stop everything.
timeout /t 8 >nul
exit /b 0

REM Returns errorlevel 0 if something answers on localhost:<port>
:ping
powershell -NoProfile -Command "try { Invoke-WebRequest -UseBasicParsing http://localhost:%1/ -TimeoutSec 2 | Out-Null; exit 0 } catch { if ($_.Exception.Response) { exit 0 } else { exit 1 } }"
exit /b %errorlevel%
