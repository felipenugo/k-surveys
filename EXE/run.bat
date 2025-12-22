@echo off
REM Script para ejecutar FormsApp en Windows

set SCRIPT_DIR=%~dp0
set JAR_FILE=%SCRIPT_DIR%FormsApp.jar
set LIB_DIR=%SCRIPT_DIR%lib

REM Verificar si el JAR existe
if not exist "%JAR_FILE%" (
    echo ERROR: FormsApp.jar no encontrado.
    echo Por favor, compila primero el proyecto:
    echo   cd ..\FONT
    echo   gradlew.bat jar
    exit /b 1
)

REM Verificar si existe el directorio lib con JavaFX
if not exist "%LIB_DIR%" (
    echo ERROR: Directorio lib\ con JavaFX no encontrado.
    echo Por favor, recompila el proyecto:
    echo   cd ..\FONT
    echo   gradlew.bat jar
    exit /b 1
)

REM Definir explicitamente los JARs de JavaFX para Windows para evitar conflictos con los de Linux
set JAVAFX_PATH=%LIB_DIR%\javafx-base-21.0.2-win.jar;%LIB_DIR%\javafx-controls-21.0.2-win.jar;%LIB_DIR%\javafx-fxml-21.0.2-win.jar;%LIB_DIR%\javafx-graphics-21.0.2-win.jar

REM Ejecutar con JavaFX en el module-path
echo Ejecutando FormsApp...
java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -jar "%JAR_FILE%"
