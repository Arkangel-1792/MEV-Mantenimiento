@echo off
setlocal
cd /d "%~dp0"
title MEV Mantenimiento Web

echo ========================================
echo       MEV MANTENIMIENTO WEB
echo ========================================
echo.

where node >nul 2>nul
if errorlevel 1 (
    echo ERROR: Node.js no esta instalado o no fue agregado al PATH.
    echo Instale Node.js LTS desde https://nodejs.org y vuelva a intentar.
    pause
    exit /b 1
)

echo Iniciando servidor en http://localhost:5500
start "" cmd /c "timeout /t 2 /nobreak >nul & start http://localhost:5500"
node servidor.js

if errorlevel 1 (
    echo.
    echo No se pudo iniciar el sistema. Ejecute COMPROBAR_PROYECTO.bat.
)

echo.
echo El servidor fue detenido.
pause
endlocal
