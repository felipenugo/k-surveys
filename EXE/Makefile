# Makefile
# Compilar antes de ejecutar: cd ../FONT && ./gradlew jar

JAVA := java
JAR_FILE := FormsApp.jar

# Driver principal (DriverMain)
run:
	@if [ ! -f "$(JAR_FILE)" ]; then \
		echo "ERROR: JAR no encontrado. Compila primero: cd ../FONT && ./gradlew jar"; \
		exit 1; \
	fi; \
	if [ -n "$(INPUT)" ] && [ -n "$(OUTPUT)" ]; then \
		$(JAVA) -jar $(JAR_FILE) < "$(INPUT)" > "$(OUTPUT)"; \
	elif [ -n "$(INPUT)" ]; then \
		$(JAVA) -jar $(JAR_FILE) < "$(INPUT)"; \
	elif [ -n "$(OUTPUT)" ]; then \
		$(JAVA) -jar $(JAR_FILE) > "$(OUTPUT)"; \
	else \
		$(JAVA) -jar $(JAR_FILE); \
	fi

# Ejecutar todos los tests
test:
	@echo "Ejecutando todos los tests:"
	@for driver in SessionDriver ClusteringDriver SurveyDriver ResponseDriver MySurveysDriver AppDriver; do \
		echo ""; \
		echo "--- Tests de $$driver ---"; \
		for input in tests/$$driver/input/*.txt; do \
			if [ -f "$$input" ]; then \
				test_name=$$(basename "$$input" .txt); \
				output="tests/$$driver/output/$${test_name}_output.txt"; \
				echo "Ejecutando $$test_name..."; \
				$(JAVA) -jar $(JAR_FILE) < "$$input" > "$$output" 2>&1; \
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
			rm -f tests/$$driver/output/*.txt; \
			echo "Limpiado tests/$$driver/output/"; \
		fi; \
	done
	@echo "Limpieza completada."

.PHONY: run test clean
