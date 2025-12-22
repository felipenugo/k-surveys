@echo off
REM Script para ejecutar todos los tests en Windows
REM Simula el comportamiento de gradlew jocs_proba

set JAR_FILE=FormsApp.jar
set LIB_DIR=%~dp0lib

echo Ejecutando todos los tests usando presentation.driverMain.DriverMain...
echo.

if not exist "tests" (
    echo El directorio 'tests' no existe.
    pause
    exit /b
)

REM Iterar sobre todos los subdirectorios en 'tests'
for /d %%D in (tests\*) do (
    echo.
    echo --- Procesando directorio: %%~nxD ---
    
    if exist "%%D\input" (
        if not exist "%%D\output" mkdir "%%D\output"
        
        for %%I in ("%%D\input\*.txt") do (
            echo Ejecutando test: %%~nxI
            
            REM Ejecutar DriverMain con el input actual y redirigir al output
            REM Se usa FormsApp.jar que contiene todas las dependencias (excepto JavaFX)
            REM Se incluye el module-path para JavaFX
            
            java --module-path "%LIB_DIR%" --add-modules javafx.controls,javafx.fxml -cp "%JAR_FILE%" presentation.driverMain.DriverMain < "%%I" > "%%D\output\%%~nI.txt" 2>&1
        )
    ) else (
        echo No se encontro carpeta 'input' en %%~nxD
    )
)

echo.
echo Todos los tests completados.
pause
