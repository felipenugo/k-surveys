@echo off
REM Script para ejecutar FormsApp.jar en Windows

set JAR_FILE=FormsApp.jar

REM Verificar si el JAR existe
if not exist %JAR_FILE% (
    echo ERROR: %JAR_FILE% no encontrado.
    echo Por favor, compila primero el proyecto:
    echo   cd ..\FONT
    echo   gradlew.bat jar
    exit /b 1
)

REM Ejecutar el JAR
echo Ejecutando FormsApp...
java -jar %JAR_FILE%
