@echo off
title Tests de SessionDriver
echo --- Ejecutando Juegos de Prueba para: SessionDriver ---

set "INPUT_DIR=..\tests\SessionDriver\input"
set "OUTPUT_DIR=..\tests\SessionDriver\output"

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

if exist "%INPUT_DIR%" (
    for %%f in ("%INPUT_DIR%\*.txt") do (
        echo Ejecutando test: %%~nxf
        java --module-path "..\lib\javafx-base-21.0.2-win.jar;..\lib\javafx-controls-21.0.2-win.jar;..\lib\javafx-fxml-21.0.2-win.jar;..\lib\javafx-graphics-21.0.2-win.jar" --add-modules javafx.controls,javafx.fxml -cp "..\K-SURVEYS.jar" presentation.driverMain.DriverMain < "%%f" > "%OUTPUT_DIR%\%%~nf.txt" 2>&1
    )
    echo.
    echo Resultados guardados en %OUTPUT_DIR%
) else (
    echo No se encontro el directorio de input: %INPUT_DIR%
)

pause
