./
# PROP Grupo 32.1
Proyecto de Programación, Grupo 32.1.

Profesor: Ignasi Gómez-Sebastià.

## Miembros del grupo

- Huertes Montes, Víctor ([victor.huertes@estudiantat.upc.edu]())
- Llagostera Garcia, Yeray ([yeray.llagostera@estudiantat.upc.edu]())
- Nuñez Gomez, Felipe Arturo ([felipe.arturo.nunez@estudiantat.upc.edu]())
- Pons Cardet, Arnau ([arnau.pons.cardet@estudiantat.upc.edu]())
- Rodriguez Uribe, Kiara Kristin ([kiara.kristin.rodriguez@estudiantat.upc.edu]())

## Descripción del proyecto

Sistema de gestión de encuestas con funcionalidades avanzadas de clustering y procesamiento de datos.

## Estructura del directorio

- **DATA**: Archivos de datos de prueba
- **DOC**: Documentación y diagramas
- **EXE**: Archivos ejecutables y Makefile
- **FONT**: Código fuente y configuración de Gradle

## Compilar y ejecutar

### Compilación
```bash
cd FONT
./gradlew jar    # Compilar el proyecto y generar FormsApp.jar
```

### Ejecutar la aplicación principal

Una vez compilado el proyecto, puedes ejecutar la aplicación principal `FormsApp.jar` desde el directorio `EXE` utilizando los siguientes scripts:

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