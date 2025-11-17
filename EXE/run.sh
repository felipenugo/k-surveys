#!/bin/bash
# Script para ejecutar FormsApp.jar en Linux/macOS

JAR_FILE="FormsApp.jar"

# Verificar si el JAR existe
if [ ! -f "$JAR_FILE" ]; then
    echo "ERROR: $JAR_FILE no encontrado."
    echo "Por favor, compila primero el proyecto:"
    echo "  cd ../FONT"
    echo "  ./gradlew jar"
    exit 1
fi

# Ejecutar el JAR
echo "Ejecutando FormsApp..."
java -jar "$JAR_FILE"
