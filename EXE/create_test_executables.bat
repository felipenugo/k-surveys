@echo off
REM Generador de ejecutables de pruebas para cada driver
REM Los scripts generados ejecutaran todos los inputs definidos en tests/DRIVER/input

set DRIVERS=SessionDriver ClusteringDriver SurveyDriver ResponseDriver MySurveysDriver AppDriver
set LIB_DIR=..\lib

if not exist "executables" mkdir "executables"

for %%D in (%DRIVERS%) do (
    echo Creando lanzador de tests para %%D...
    (
        echo @echo off
        echo title Tests de %%D
        echo echo --- Ejecutando Juegos de Prueba para: %%D ---
        echo.
        echo set "INPUT_DIR=..\tests\%%D\input"
        echo set "OUTPUT_DIR=..\tests\%%D\output"
        echo.
        echo if not exist "%%OUTPUT_DIR%%" mkdir "%%OUTPUT_DIR%%"
        echo.
        echo if exist "%%INPUT_DIR%%" ^(
        echo     for %%%%f in ^("%%INPUT_DIR%%\*.txt"^) do ^(
        echo         echo Ejecutando test: %%%%~nxf
        echo         java --module-path "..\lib" --add-modules javafx.controls,javafx.fxml -cp "..\FormsApp.jar" presentation.driverMain.DriverMain ^< "%%%%f" ^> "%%OUTPUT_DIR%%\%%%%~nf.txt" 2^>^&1
        echo     ^)
        echo     echo.
        echo     echo Resultados guardados en %%OUTPUT_DIR%%
        echo ^) else ^(
        echo     echo No se encontro el directorio de input: %%INPUT_DIR%%
        echo ^)
        echo.
        echo pause
    ) > "executables\Run_%%D.bat"
)

echo.
echo Ejecutables de tests actualizados en 'executables'.
