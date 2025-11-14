#!/bin/bash
# Script para compilar y ejecutar la aplicación CLI de K-SURVEY

# Save the original directory
ORIGINAL_DIR=$(pwd)

echo "Compilando la aplicación..."
./gradlew assembleDist

echo "Extrayendo el paquete distribuible..."
cd build/distributions
tar -xf gradlepoc-1.0-SNAPSHOT.tar

echo "Ejecutando K-SURVEY CLI..."
cd gradlepoc-1.0-SNAPSHOT/bin

# Check if input and output files are specified
if [ -n "$1" ] && [ -n "$2" ]; then
    # Convert to absolute paths if they are relative
    INPUT_FILE="$1"
    OUTPUT_FILE="$2"
    [[ "$INPUT_FILE" != /* ]] && INPUT_FILE="$ORIGINAL_DIR/$INPUT_FILE"
    [[ "$OUTPUT_FILE" != /* ]] && OUTPUT_FILE="$ORIGINAL_DIR/$OUTPUT_FILE"
    
    echo "Usando archivo de entrada: $INPUT_FILE y archivo de salida: $OUTPUT_FILE"
    ./gradlepoc < "$INPUT_FILE" > "$OUTPUT_FILE"
elif [ -n "$1" ]; then
    INPUT_FILE="$1"
    [[ "$INPUT_FILE" != /* ]] && INPUT_FILE="$ORIGINAL_DIR/$INPUT_FILE"
    
    echo "Usando archivo de entrada: $INPUT_FILE"
    ./gradlepoc < "$INPUT_FILE"
else
    ./gradlepoc
fi
