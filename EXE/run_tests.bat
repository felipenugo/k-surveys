@echo off
setlocal

set JAR_FILE=FormsApp.jar

if not exist "%JAR_FILE%" (
  echo ERROR: JAR no encontrado. Compila primero: cd ..\FONT ^&^& gradlew.bat jar
  exit /b 1
)

set DRIVERS=SessionDriver ClusteringDriver SurveyDriver ResponseDriver MySurveysDriver AppDriver

echo "Ejecutando todos los tests:"

for %%d in (%DRIVERS%) do (
  echo.
  echo "--- Tests de %%d ---"
  if exist "tests\%%d\input\*.txt" (
    for %%f in (tests\%%d\input\*.txt) do (
      echo "Ejecutando %%~nf..."
      java -jar %JAR_FILE% < "%%f" > "tests\%%d\output\%%~nf_output.txt" 2>&1
    )
  ) else (
    echo "No se encontraron tests para %%d"
  )
)

echo.
echo "Tests completados."

endlocal
