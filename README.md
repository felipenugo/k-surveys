
Useful commands:


./gradlew test: will run your unit tests.
./gradlew run : will run your application in the environment. This is useful to test your application in the development environment.
./gradlew jar: will create the jar inside the directory <project root>/build/libs with only the project's code. Not dependencies.
./gradlew assembleDist: will create a .tar and a .zip (both contain the same) in the directory <project root>/build/distributions that contain
the whole directory structure that will allow to install your project along with its dependencies in a machine without IDE (only with java 11 installed) and run it.
./gradlew clean: will clean the compilation files and the created artifacts


More info
Los archivos .bin son ignorados por git; para cambiar esto modificar el archivo .gitignore.
Todos los directorios tienen que contener un fichero index.txt en español describiendo el directorio y todos sus ficheros.
El index.txt de la raíz del proyecto contiene los nombres y emails de los desarrolladores.
Los ficheros .txt deben ser ortográficamente correctos (acentuación aceptada en español).



Proyecto reorganizado para tener un layout similar a la plantilla Java-A3C-PROP-Ejemplo.

Estructura relevante:
- DATA/: Archivos de datos y ejemplos (ej., loan-test.csv, input.txt, output.txt)
- DOC/: Documentación del proyecto (copiado desde DOCS/)
- EXE/: Recursos y despliegues ejecutables
- FONT/: Código fuente (contiene las fuentes Java compilables). Nota: el antiguo `src/` fue movido a `temp/` como copia de seguridad.

Gradle application plugin
https://docs.gradle.org/current/userguide/application_plugin.html
