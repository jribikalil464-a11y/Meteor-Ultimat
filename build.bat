@echo off
setlocal
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build.ps1"
if errorlevel 1 (
  echo.
  echo Build failed. Read BUILD-STATUS.md and copy the complete error output.
  pause
  exit /b 1
)
echo.
echo Build finished. Check release\Meteor-Ultimate-1.21.4.jar
pause
