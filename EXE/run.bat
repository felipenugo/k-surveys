@echo off
:: Cross-platform run helper (Windows batch).
:: Usage: run.bat [INPUT] [OUTPUT]
setlocal
set JAR=FormsApp.jar
:: Find jar in current folder
if not exist "%JAR%" (
  echo ERROR: JAR no encontrado. Compila primero: cd ..\FONT && gradlew.bat jar
  endlocal
  exit /b 1
)

:: Args: 1=INPUT 2=OUTPUT
if "%~1"=="" (
  if "%~2"=="" (
    java -jar "%JAR%"
  ) else (
    java -jar "%JAR%" > "%~2"
  )
) else (
  if "%~2"=="" (
    java -jar "%JAR%" < "%~1"
  ) else (
    java -jar "%JAR%" < "%~1" > "%~2"
  )
)
endlocal
