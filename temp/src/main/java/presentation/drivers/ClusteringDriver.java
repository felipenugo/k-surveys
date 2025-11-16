package presentation.drivers;

import domain.controller.CtrlDominioClustering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Driver para ejecutar y gestionar análisis de clustering desde la consola.
 */
public class ClusteringDriver {

    private final CtrlDominioClustering ctrlDominioClustering;
    private final BufferedReader reader;

    /**
     * Constructor del driver.
     * @param ctrlDominioClustering Controlador de dominio de clustering.
     */
    public ClusteringDriver(CtrlDominioClustering ctrlDominioClustering) {
        this.ctrlDominioClustering = ctrlDominioClustering;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * Inicia el menú principal del driver de clustering.
     */
    public void run() {
        boolean exit = false;
        while (!exit) {
            System.out.println("--- Menú Principal de Clustering ---");
            System.out.println("1. Ejecutar un nuevo análisis");
            System.out.println("2. Evaluar un análisis existente");
            System.out.println("3. Encontrar la K óptima para una encuesta");
            System.out.println("4. Ver resultados de un análisis");
            System.out.println("5. Salir");
            System.out.print("Elige una opción: ");

            try {
                String option = reader.readLine();
                switch (option) {
                    case "1":
                        ejecutarNuevoAnalisis();
                        break;
                    case "2":
                        evaluarAnalisisExistente();
                        break;
                    case "3":
                        encontrarKOptima();
                        break;
                    case "4":
                        verResultadosAnalisis();
                        break;
                    case "5":
                        exit = true;
                        System.out.println("Saliendo del menú de clustering...");
                        break;
                    default:
                        System.out.println("Opción no válida. Inténtalo de nuevo.");
                }
            } catch (IOException e) {
                System.out.println("Error de entrada/salida: " + e.getMessage());
            }
        }
    }

    /**
     * Guía al usuario para ejecutar un nuevo análisis de clustering.
     */
    private void ejecutarNuevoAnalisis() {
        try {
            System.out.println("\n--- Ejecutar Nuevo Análisis ---");
            
            System.out.print("Introduce el ID de la encuesta a analizar: ");
            String surveyId = reader.readLine();

            System.out.println("Selecciona un algoritmo (1: KMeans, 2: KMeans++, 3: KMedoids):");
            String algorithm = selectAlgorithm();
            if (algorithm == null) return;

            System.out.print("Introduce el número de clusters (k): ");
            int k = Integer.parseInt(reader.readLine());

            System.out.println("Selecciona una métrica de distancia (1: Euclidea, 2: Manhattan):");
            String distanceMetric = selectDistanceMetric();
            if (distanceMetric == null) return;

            System.out.println("Ejecutando análisis... Esto puede tardar un momento.");
            String analysisId = ctrlDominioClustering.runAnalysis(algorithm, surveyId, k, 100, 1e-4, distanceMetric);

            if (analysisId == null) {
                System.out.println("Error: No se pudo ejecutar el análisis. Verifica que la encuesta y las respuestas existen.");
                return;
            }

            System.out.println("Análisis ejecutado con éxito. ID del análisis: " + analysisId);

            // Exportación automática
            try {
                System.out.print("Introduce la ruta del archivo para exportar los resultados (p.ej., ./exports/analysis.txt): ");
                String path = reader.readLine();
                String absolutePath = ctrlDominioClustering.exportarAnalisis(analysisId, path);
                System.out.println("Resultados exportados automáticamente a: " + absolutePath);
            } catch (IOException e) {
                System.out.println("Error al exportar los resultados: " + e.getMessage());
            }

            // Menú de visualización post-análisis
            visualizarResultados(analysisId);

        } catch (IOException | NumberFormatException e) {
            System.out.println("Entrada no válida: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ha ocurrido un error: " + e.getMessage());
        }
    }

    /**
     * Permite al usuario evaluar un análisis de clustering ya guardado.
     */
    private void evaluarAnalisisExistente() {
        try {
            System.out.println("\n--- Evaluar Análisis Existente ---");
            List<String> analysisIds = ctrlDominioClustering.listarAnalisis();

            if (analysisIds.isEmpty()) {
                System.out.println("No hay análisis guardados para evaluar.");
                return;
            }

            System.out.println("Análisis disponibles (más recientes primero):");
            for (int i = 0; i < analysisIds.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, analysisIds.get(i));
            }

            System.out.print("Selecciona el número del análisis a evaluar: ");
            int choice = Integer.parseInt(reader.readLine()) - 1;

            if (choice < 0 || choice >= analysisIds.size()) {
                System.out.println("Selección no válida.");
                return;
            }

            String analysisId = analysisIds.get(choice);
            System.out.println("\nEvaluando análisis: " + analysisId);
            
            String qualityMetrics = ctrlDominioClustering.obtenerMetricasCalidad(analysisId);
            System.out.println(qualityMetrics);

        } catch (IOException | NumberFormatException e) {
            System.out.println("Entrada no válida: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ha ocurrido un error: " + e.getMessage());
        }
    }

    /**
     * Inicia el proceso para encontrar el número óptimo de clusters (k).
     */
    private void encontrarKOptima() {
        try {
            System.out.println("\n--- Encontrar K Óptima ---");
            
            System.out.print("Introduce el ID de la encuesta: ");
            String surveyId = reader.readLine();

            System.out.println("Selecciona un algoritmo (1: KMeans, 2: KMeans++, 3: KMedoids):");
            String algorithm = selectAlgorithm();
            if (algorithm == null) return;

            System.out.println("Selecciona una métrica de distancia (1: Euclidea, 2: Manhattan):");
            String distanceMetric = selectDistanceMetric();
            if (distanceMetric == null) return;

            System.out.println("Buscando K óptima (k de 2 a 10)... Esto puede tardar varios minutos.");
            String report = ctrlDominioClustering.encontrarKOptima(surveyId, algorithm, 100, 1e-4, distanceMetric);
            
            System.out.println("\n--- Informe de K Óptima ---");
            System.out.println(report);

        } catch (IOException e) {
            System.out.println("Entrada no válida: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ha ocurrido un error: " + e.getMessage());
        }
    }

    /**
     * Muestra los resultados detallados de un análisis de clustering existente.
     */
    private void verResultadosAnalisis() {
        try {
            System.out.println("\n--- Ver Resultados de Análisis ---");
            List<String> analysisIds = ctrlDominioClustering.listarAnalisis();

            if (analysisIds.isEmpty()) {
                System.out.println("No hay análisis guardados para ver.");
                return;
            }

            System.out.println("Análisis disponibles (más recientes primero):");
            for (int i = 0; i < analysisIds.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, analysisIds.get(i));
            }

            System.out.print("Selecciona el número del análisis a ver: ");
            int choice = Integer.parseInt(reader.readLine()) - 1;

            if (choice < 0 || choice >= analysisIds.size()) {
                System.out.println("Selección no válida.");
                return;
            }

            String analysisId = analysisIds.get(choice);
            System.out.println("\n--- Resultados del Análisis: " + analysisId + " ---");
            
            String results = ctrlDominioClustering.obtenerResultadosAnalisis(analysisId);
            System.out.println(results);

        } catch (IOException | NumberFormatException e) {
            System.out.println("Entrada no válida: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ha ocurrido un error: " + e.getMessage());
        }
    }

    /**
     * Muestra un menú para visualizar los resultados de un análisis recién ejecutado.
     * @param analysisId El ID del análisis a visualizar.
     */
    private void visualizarResultados(String analysisId) throws IOException {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n--- ¿Qué deseas hacer con el nuevo análisis? ---");
            System.out.println("1. Ver métricas de calidad");
            System.out.println("2. Volver al menú principal");
            System.out.print("Elige una opción: ");

            String option = reader.readLine();
            switch (option) {
                case "1":
                    String qualityMetrics = ctrlDominioClustering.obtenerMetricasCalidad(analysisId);
                    System.out.println("\n--- Métricas de Calidad ---");
                    System.out.println(qualityMetrics);
                    break;
                case "2":
                    exit = true;
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }

    /**
     * Helper para seleccionar un algoritmo de clustering.
     * @return El nombre del algoritmo o null si la opción es inválida.
     */
    private String selectAlgorithm() throws IOException {
        System.out.print("Opción: ");
        String option = reader.readLine();
        switch (option) {
            case "1": return "KMeans";
            case "2": return "KMeansPlusPlus";
            case "3": return "KMedoids";
            default:
                System.out.println("Algoritmo no válido.");
                return null;
        }
    }

    /**
     * Helper para seleccionar una métrica de distancia.
     * @return El nombre de la métrica o null si la opción es inválida.
     */
    private String selectDistanceMetric() throws IOException {
        System.out.print("Opción: ");
        String option = reader.readLine();
        switch (option) {
            case "1": return "EUCLIDEAN";
            case "2": return "MANHATTAN";
            default:
                System.out.println("Métrica no válida.");
                return null;
        }
    }
}