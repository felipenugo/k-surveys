# run-cli.sh - Guía de Uso

Script para compilar y ejecutar la aplicación K-SURVEY CLI.

## Uso Básico

### Ejecución interactiva (manual):
```bash
bash run-cli.sh
```

### Ejecución con archivos de entrada/salida:
```bash
bash run-cli.sh <archivo_entrada> <archivo_salida>
```

## Ejemplos

```bash
# Modo interactivo
bash run-cli.sh

# Con input y output
bash run-cli.sh input.txt output.txt

# Solo con input (output en consola)
bash run-cli.sh input.txt
```

## Formato del archivo de entrada

Una línea por cada entrada que solicita el programa:

```
2                      # Opción del menú: Registrarse
usuario                # Nombre de usuario
usuario@gmail.com      # Email
password123            # Contraseña
usuario                # Usuario para login
password123            # Contraseña para login
3                      # Cerrar sesión
3                      # Salir
```

## Notas

- El script compila automáticamente con `./gradlew assembleDist`
- Soporta rutas relativas y absolutas
