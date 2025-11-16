#!/bin/bash
# Script para compilar y ejecutar la aplicación CLI de K-SURVEY

# Save the original directory
ORIGINAL_DIR=$(pwd)

echo "Compilando la aplicación..."
./gradlew assembleDist

echo "Extrayendo el paquete distribuible..."
cd build/distributions
# Find the distribution tar dynamically (supports different project names)
TAR_FILE=$(ls *.tar | head -n 1)
if [ -z "$TAR_FILE" ]; then
    echo "No distribution tar found in build/distributions"
    exit 1
fi
tar -xf "$TAR_FILE"
BASE_DIR=$(basename "$TAR_FILE" .tar)
echo "Ejecutando K-SURVEY CLI..."
cd "$BASE_DIR"/bin
# Determine the project executable name (first non-.bat file in bin)
EXECUTABLE=$(ls | grep -v '\.bat$' | head -n 1)
if [ -z "$EXECUTABLE" ]; then
    echo "No executable found in $BASE_DIR/bin"
    exit 1
fi

# Check if input and output files are specified
if [ -n "$1" ] && [ -n "$2" ]; then
    # Convert to absolute paths if they are relative
    INPUT_FILE="$1"
    OUTPUT_FILE="$2"
    [[ "$INPUT_FILE" != /* ]] && INPUT_FILE="$ORIGINAL_DIR/$INPUT_FILE"
    [[ "$OUTPUT_FILE" != /* ]] && OUTPUT_FILE="$ORIGINAL_DIR/$OUTPUT_FILE"
    
    echo "Usando archivo de entrada: $INPUT_FILE y archivo de salida: $OUTPUT_FILE"
    ./"$EXECUTABLE" < "$INPUT_FILE" > "$OUTPUT_FILE"
elif [ -n "$1" ]; then
    INPUT_FILE="$1"
    [[ "$INPUT_FILE" != /* ]] && INPUT_FILE="$ORIGINAL_DIR/$INPUT_FILE"
    
    echo "Usando archivo de entrada: $INPUT_FILE"
    ./"$EXECUTABLE" < "$INPUT_FILE"
else
    ./"$EXECUTABLE"
fi
