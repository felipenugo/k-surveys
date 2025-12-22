#!/bin/bash
# Generador de ejecutables de pruebas para Linux (archivos .sh)

DRIVERS="SessionDriver ClusteringDriver SurveyDriver ResponseDriver MySurveysDriver AppDriver"

# Crear carpeta si no existe
if [ ! -d "executables" ]; then
    mkdir "executables"
fi

for driver in $DRIVERS; do
    echo "Creando lanzador de tests para $driver (Linux)..."
    FILE="executables/Run_${driver}.sh"
    
    # Escribir el contenido del script
    cat << EOF > "$FILE"
#!/bin/bash
# Script generado para ejecutar tests de $driver

SCRIPT_DIR="
$(cd "$(dirname "$0")" && pwd)"
# Rutas relativas desde 'executables/'
INPUT_DIR="$SCRIPT_DIR/../tests/$driver/input"
OUTPUT_DIR="$SCRIPT_DIR/../tests/$driver/output"
JAR_FILE="$SCRIPT_DIR/../FormsApp.jar"
LIB_DIR="$SCRIPT_DIR/../lib"

# Definir explícitamente los JARs de JavaFX para Linux
JAVAFX_PATH="$LIB_DIR/javafx-base-21.0.2-linux.jar:$LIB_DIR/javafx-controls-21.0.2-linux.jar:$LIB_DIR/javafx-fxml-21.0.2-linux.jar:$LIB_DIR/javafx-graphics-21.0.2-linux.jar"

echo "--- Ejecutando Juegos de Prueba para: $driver ---"

if [ ! -d "$OUTPUT_DIR" ]; then
    mkdir -p "$OUTPUT_DIR"
fi

if [ -d "$INPUT_DIR" ]; then
    # Buscar archivos .txt y ejecutarlos
    for input_file in "$INPUT_DIR"/*.txt; do
        # Verificar si existen archivos para evitar errores con wildcards vacíos
        [ -e "$input_file" ] || continue
        
        test_name=$(basename "$input_file" .txt)
        echo "Ejecutando test: $test_name.txt"
        
        java --module-path "$JAVAFX_PATH" \
             --add-modules javafx.controls,javafx.fxml \
             -cp "$JAR_FILE" \
             presentation.driverMain.DriverMain < "$input_file" > "$OUTPUT_DIR/$test_name.txt" 2>&1
    done
    echo ""
    echo "Resultados guardados en ../tests/$driver/output"
else
    echo "No se encontró el directorio de input: $INPUT_DIR"
fi

echo ""
read -p "Presiona Enter para cerrar..."
EOF

    # Intentar dar permisos de ejecución (si el sistema de archivos lo soporta)
    chmod +x "$FILE" 2>/dev/null
done

echo ""
echo "Ejecutables .sh creados en la carpeta 'executables'."
