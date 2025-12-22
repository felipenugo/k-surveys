@echo off
title Tests de SurveyDriver
echo --- Ejecutando Juegos de Prueba para: SurveyDriver ---

set "INPUT_DIR=..\tests\SurveyDriver\input"
set "OUTPUT_DIR=..\tests\SurveyDriver\output"

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

if exist "%INPUT_DIR%" (
    for %%f in ("%INPUT_DIR%\*.txt") do (
        echo Ejecutando test: %%~nxf
        java --module-path "..\lib" --add-modules javafx.controls,javafx.fxml -cp "..\FormsApp.jar" presentation.driverMain.DriverMain < "%%f" > "%OUTPUT_DIR%\%%~nf.txt" 2>&1
    )
    echo.
    echo Resultados guardados en %OUTPUT_DIR%
) else (
    echo No se encontro el directorio de input: %INPUT_DIR%
)

pause
