#!/bin/bash
# Script para compilar y ejecutar la aplicación CLI de K-SURVEY

echo "Compilando la aplicación..."
./gradlew assembleDist

echo "Extrayendo el paquete distribuible..."
cd build/distributions
tar -xf gradlepoc-1.0-SNAPSHOT.tar

echo "Ejecutando K-SURVEY CLI..."
cd gradlepoc-1.0-SNAPSHOT/bin
./gradlepoc
