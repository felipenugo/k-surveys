# K-SURVEYS

PROP Grupo 32.1. Proyecto de Programación.

Profesor: Ignasi Gómez-Sebastià.

## Miembros del grupo

- Huertes Montes, Víctor
- Llagostera Garcia, Yeray
- Nuñez Gomez, Felipe Arturo
- Pons Cardet, Arnau
- Rodriguez Uribe, Kiara Kristin

## Descripción del proyecto

Sistema de gestión de encuestas con funcionalidades avanzadas de clustering y procesamiento de datos.

## Estructura del directorio

- **DATA**: Archivos de datos de prueba
- **DOC**: Documentación y diagramas
- **EXE**: Scripts de ejecución y juegos de prueba. Los binarios se generan en local y no se versionan
- **FONT**: Código fuente y configuración de Gradle

## Compilar y ejecutar

### Compilación
```bash
cd FONT
./gradlew jar    # Compilar el proyecto y generar K-SURVEYS.jar y K-SURVEYS.exe
```

### Ejecutar la aplicación principal

Una vez compilado el proyecto, puedes ejecutar `K-SURVEYS.jar` desde el directorio `EXE` utilizando los siguientes scripts:

**En Windows:**
```bash
cd EXE
.\run.bat        # Ejecutar la aplicación
```

**En Linux/macOS:**
```bash
cd EXE
./run.sh         # Ejecutar la aplicación
```

### Ejecutar con Gradle (desde FONT)
```bash
cd FONT
./gradlew run    # Ejecutar la aplicación directamente con Gradle
./gradlew test   # Ejecutar los tests con Gradle
```

### Juegos de prueba

```bash
cd EXE
make run    # Ejecutar un juego de prueba (definido en Makefile)
make test   # Ejecutar todos los tests definidos en Makefile
.\run_tests.bat # Ejecutar todos los tests (solo Windows)
```
