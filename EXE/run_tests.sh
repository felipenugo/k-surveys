#!/bin/bash
# Script para ejecutar todos los tests en Linux/macOS
# Simula el comportamiento de gradlew jocs_proba

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_FILE="$SCRIPT_DIR/K-SURVEYS.jar"
LIB_DIR="$SCRIPT_DIR/lib"

# Definir explícitamente los JARs de JavaFX para Linux
JAVAFX_PATH="$LIB_DIR/javafx-base-21.0.2-linux.jar:$LIB_DIR/javafx-controls-21.0.2-linux.jar:$LIB_DIR/javafx-fxml-21.0.2-linux.jar:$LIB_DIR/javafx-graphics-21.0.2-linux.jar"

echo "Ejecutando todos los tests usando presentation.driverMain.DriverMain..."
echo ""

TESTS_DIR="$SCRIPT_DIR/tests"

if [ ! -d "$TESTS_DIR" ]; then
    echo "El directorio 'tests' no existe."
    exit 1
fi

# Iterar sobre todos los subdirectorios en 'tests'
for driver_dir in "$TESTS_DIR"/*; do
    if [ -d "$driver_dir" ]; then
        driver_name=$(basename "$driver_dir")
        echo ""
        echo "--- Procesando directorio: $driver_name ---"
        
        input_dir="$driver_dir/input"
        output_dir="$driver_dir/output"
        
        if [ -d "$input_dir" ]; then
            if [ ! -d "$output_dir" ]; then
                mkdir -p "$output_dir"
            fi
            
            # Buscar archivos .txt en input
            find "$input_dir" -maxdepth 1 -name "*.txt" | while read input_file; do
                test_name=$(basename "$input_file" .txt)
                echo "Ejecutando test: $test_name.txt"
                
                # Ejecutar DriverMain
                java --module-path "$JAVAFX_PATH" \
                     --add-modules javafx.controls,javafx.fxml \
                     -cp "$JAR_FILE" \
                     presentation.driverMain.DriverMain < "$input_file" > "$output_dir/$test_name.txt" 2>&1
            done
        else
            echo "No se encontró carpeta 'input' en $driver_name"
        fi
    fi
done

echo ""
echo "Todos los tests completados."
