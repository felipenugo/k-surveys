# Makefile
# Compilar antes de ejecutar: cd ../FONT && ./gradlew jar

JAVA := java
JAR_FILE := K-SURVEYS.jar
LIB_DIR := lib
JAVAFX_PATH := $(LIB_DIR)/javafx-base-21.0.2-linux.jar:$(LIB_DIR)/javafx-controls-21.0.2-linux.jar:$(LIB_DIR)/javafx-fxml-21.0.2-linux.jar:$(LIB_DIR)/javafx-graphics-21.0.2-linux.jar
JAVA_FX := $(JAVA) --module-path "$(JAVAFX_PATH)" --add-modules javafx.controls,javafx.fxml

# Driver principal (DriverMain)
run:
	@if [ ! -f "$(JAR_FILE)" ]; then \
		echo "ERROR: JAR no encontrado. Compila primero: cd ../FONT && ./gradlew jar"; \
		exit 1; \
	fi; \
	if [ ! -d "$(LIB_DIR)" ]; then \
		echo "ERROR: Directorio lib/ con JavaFX no encontrado. Compila primero: cd ../FONT && ./gradlew jar"; \
		exit 1; \
	fi; \
	if [ -n "$(INPUT)" ] && [ -n "$(OUTPUT)" ]; then \
		$(JAVA_FX) -jar $(JAR_FILE) < "$(INPUT)" > "$(OUTPUT)"; \
	elif [ -n "$(INPUT)" ]; then \
		$(JAVA_FX) -jar $(JAR_FILE) < "$(INPUT)"; \
	elif [ -n "$(OUTPUT)" ]; then \
		$(JAVA_FX) -jar $(JAR_FILE) > "$(OUTPUT)"; \
	else \
		$(JAVA_FX) -jar $(JAR_FILE); \
	fi

# Ejecutar todos los tests de consola
test:
	@if [ ! -f "$(JAR_FILE)" ]; then \
		echo "ERROR: JAR no encontrado. Compila primero: cd ../FONT && ./gradlew jar"; \
		exit 1; \
	fi
	@echo "Ejecutando todos los tests:"
	@for driver in SessionDriver ClusteringDriver SurveyDriver ResponseDriver MySurveysDriver AppDriver; do \
		echo ""; \
		echo "--- Tests de $$driver ---"; \
		for input in tests/$$driver/input/*.txt; do \
			if [ -f "$$input" ]; then \
				test_name=$$(basename "$$input" .txt); \
				output="tests/$$driver/output/$${test_name}_output.txt"; \
				echo "Ejecutando $$test_name..."; \
				$(JAVA_FX) -cp $(JAR_FILE) presentation.driverMain.DriverMain < "$$input" > "$$output" 2>&1; \
			fi; \
		done; \
	done
	@echo ""
	@echo "Tests completados."

# Limpiar archivos de output generados por los tests
clean:
	@echo "Eliminando archivos de output de tests..."
	@for driver in SessionDriver ClusteringDriver SurveyDriver ResponseDriver MySurveysDriver AppDriver; do \
		if [ -d "tests/$$driver/output" ]; then \
			rm -f tests/$$driver/output/*_output.txt; \
			echo "Limpiado tests/$$driver/output/"; \
		fi; \
	done
	@echo "Limpieza completada."

.PHONY: run test clean
