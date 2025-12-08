#!/bin/bash
# Script para ejecutar FormsApp en Linux/macOS

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_FILE="$SCRIPT_DIR/FormsApp.jar"
LIB_DIR="$SCRIPT_DIR/lib"

# Verificar si el JAR existe
if [ ! -f "$JAR_FILE" ]; then
    echo "ERROR: FormsApp.jar no encontrado."
    echo "Por favor, compila primero el proyecto:"
    echo "  cd ../FONT"
    echo "  ./gradlew jar"
    exit 1
fi

# Verificar si existe el directorio lib con JavaFX
if [ ! -d "$LIB_DIR" ]; then
    echo "ERROR: Directorio lib/ con JavaFX no encontrado."
    echo "Por favor, recompila el proyecto:"
    echo "  cd ../FONT"
    echo "  ./gradlew jar"
    exit 1
fi

# Ejecutar con JavaFX en el module-path
echo "Ejecutando FormsApp..."
java --module-path "$LIB_DIR" \
     --add-modules javafx.controls,javafx.fxml \
     -jar "$JAR_FILE"
