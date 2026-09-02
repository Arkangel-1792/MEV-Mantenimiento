@echo off
setlocal
cd /d "%~dp0"
title Comprobar MEV Mantenimiento Web

echo ========================================
echo    COMPROBACION DEL PROYECTO MEV WEB
echo ========================================
echo.

where node >nul 2>nul
if errorlevel 1 (
    echo ERROR: Node.js no esta instalado.
    pause
    exit /b 1
)

call npm.cmd run check
if errorlevel 1 goto error

call npm.cmd test
if errorlevel 1 goto error

echo.
echo RESULTADO: El proyecto supero todas las comprobaciones.
pause
exit /b 0

:error
echo.
echo RESULTADO: Se encontro un error. Revise el mensaje mostrado arriba.
pause
exit /b 1
