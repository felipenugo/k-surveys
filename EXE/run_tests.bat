@echo off
REM Script para ejecutar todos los tests en Windows

set JAR_FILE=FormsApp.jar
set JAVA=java

REM Verificar si el JAR existe
if not exist %JAR_FILE% (
    echo ERROR: %JAR_FILE% no encontrado.
    echo Por favor, compila primero el proyecto:
    echo   cd ..\FONT
    echo   gradlew.bat jar
    exit /b 1
)

echo Ejecutando todos los tests:
echo.

REM Lista de drivers
set DRIVERS=SessionDriver ClusteringDriver SurveyDriver ResponseDriver MySurveysDriver AppDriver

for %%D in (%DRIVERS%) do (
    echo.
    echo --- Tests de %%D ---
    
    if exist tests\%%D\input (
        for %%I in (tests\%%D\input\*.txt) do (
            set INPUT_FILE=%%I
            set TEST_NAME=%%~nI
            set OUTPUT_FILE=tests\%%D\output\%%~nI_output.txt
            
            echo Ejecutando %%~nI...
            %JAVA% -jar %JAR_FILE% < "%%I" > "tests\%%D\output\%%~nI_output.txt" 2>&1
        )
    )
)

echo.
echo Tests completados.
pause
