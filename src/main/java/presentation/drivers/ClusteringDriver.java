package presentation.drivers;

import domain.controller.CtrlDominioClustering;
import domain.controller.SurveyController;
import domain.clustering.ClusterResults;
import domain.clustering.Cluster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Driver para ejecutar análisis de clustering desde la consola.
 */
public class ClusteringDriver {

    private final CtrlDominioClustering ctrlDominioClustering;
    private final SurveyController surveyController;
    private String lastAnalysisId; // Guardar el ID del último análisis ejecutado

    /**
     * Constructor del driver de clustering.
     * 
     * @param ctrlDominioClustering Controlador de dominio de clustering
     * @param surveyController Controlador de encuestas
     */
    public ClusteringDriver(CtrlDominioClustering ctrlDominioClustering, SurveyController surveyController) {
        this.ctrlDominioClustering = ctrlDominioClustering;
        this.surveyController = surveyController;
        this.lastAnalysisId = null;
    }

    /**
     * Ejecuta el menú interactivo de clustering para una encuesta.
     * 
     * @param surveyId ID de la encuesta a analizar
     */
    public void run(String surveyId) {
        try {
            System.out.println("--- Menu de Clustering ---");
            System.out.println("Selecciona un algoritmo:");
            System.out.println("1. KMeans");
            System.out.println("2. KMeans++");
            System.out.println("3. KMedoids");
            System.out.print("Opcion: ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String algorithmOption = reader.readLine();
            String algorithm = "";
            switch (algorithmOption) {
                case "1":
                    algorithm = "KMeans";
                    break;
                case "2":
                    algorithm = "KMeansPlusPlus";
                    break;
                case "3":
                    algorithm = "KMedoids";
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    return;
            }

            System.out.print("Introduce el numero de clusters (k): ");
            int k = Integer.parseInt(reader.readLine());

            System.out.println("Selecciona una metrica de distancia:");
            System.out.println("1. Euclidea");
            System.out.println("2. Manhattan");
            System.out.print("Opcion: ");
            String distanceOption = reader.readLine();
            String distanceMetric = "";
            switch (distanceOption) {
                case "1":
                    distanceMetric = "EUCLIDEAN";
                    break;
                case "2":
                    distanceMetric = "MANHATTAN";
                    break;
                default:
                    System.out.println("Opcion invalida.");
                    return;
            }

            String analysisId = surveyId + "_" + algorithm + "_k" + k + "_" + System.currentTimeMillis();
            
            String realAnalysisId = ctrlDominioClustering.ejecutarClustering(analysisId, algorithm, surveyId, k, 100, 1e-4, distanceMetric);
            
            if (realAnalysisId == null) {
                System.out.println("Error: No se pudo ejecutar el análisis. Verifica que la encuesta existe y tiene respuestas.");
                return;
            }
            
            lastAnalysisId = realAnalysisId; // Guardar el ID real del análisis
            System.out.println("Análisis ejecutado con éxito (ID: " + realAnalysisId + ")");
            
            // Mostrar opción de visualización
            boolean showVisualizationMenu = true;
            while (showVisualizationMenu) {
                System.out.println("\n--- Menu de Visualizacion ---");
                System.out.println("1. Ver resumen del analisis");
                System.out.println("2. Ver distribucion de clusters");
                System.out.println("3. Ver detalles de un cluster especifico");
                System.out.println("4. Ver todos los detalles");
                System.out.println("5. Salir");
                System.out.print("Opcion: ");
                
                String visualizationOption = reader.readLine();
                switch (visualizationOption) {
                    case "1":
                        visualizarResumen(realAnalysisId);
                        break;
                    case "2":
                        visualizarDistribucionClusters(realAnalysisId);
                        break;
                    case "3":
                        visualizarClusterEspecifico(realAnalysisId, reader);
                        break;
                    case "4":
                        visualizarTodo(realAnalysisId);
                        break;
                    case "5":
                        showVisualizationMenu = false;
                        break;
                    default:
                        System.out.println("Opcion invalida.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Visualiza un resumen general del análisis de clustering.
     * 
     * @param analysisId ID del análisis
     */
    private void visualizarResumen(String analysisId) {
        try {
            System.out.println("\n========================================");
            System.out.println("       RESUMEN DEL ANALISIS");
            System.out.println("========================================");
            String resumen = ctrlDominioClustering.obtenerResumen(analysisId);
            System.out.println(resumen);
            System.out.println("========================================\n");
        } catch (Exception e) {
            System.out.println("Error al obtener resumen: " + e.getMessage());
        }
    }
    
    /**
     * Visualiza la distribución de elementos en cada cluster.
     * 
     * @param analysisId ID del análisis
     */
    private void visualizarDistribucionClusters(String analysisId) {
        try {
            ClusterResults results = ctrlDominioClustering.obtenerResultados(analysisId);
            List<Cluster> clusters = results.getClusters();
            
            System.out.println("\n========================================");
            System.out.println("   DISTRIBUCION DE CLUSTERS");
            System.out.println("========================================");
            System.out.println("Numero total de clusters: " + results.getK());
            System.out.println("Numero total de respuestas: " + results.getNumberOfResponses());
            System.out.println("Iteraciones realizadas: " + results.getIterations());
            System.out.println("Convergencia: " + (results.hasConverged() ? "SI" : "NO"));
            System.out.println("----------------------------------------");
            
            // Calcular el tamaño máximo para el gráfico de barras
            int maxSize = 0;
            for (Cluster cluster : clusters) {
                if (cluster.getSize() > maxSize) {
                    maxSize = cluster.getSize();
                }
            }
            
            // Mostrar gráfico de barras simple en ASCII
            for (int i = 0; i < clusters.size(); i++) {
                Cluster cluster = clusters.get(i);
                int size = cluster.getSize();
                double percentage = (size * 100.0) / results.getNumberOfResponses();
                
                System.out.printf("Cluster %d: [%-20s] %3d respuestas (%.1f%%)%n", 
                    i, 
                    generarBarraASCII(size, maxSize, 20), 
                    size, 
                    percentage);
            }
            System.out.println("========================================\n");
        } catch (Exception e) {
            System.out.println("Error al obtener distribucion: " + e.getMessage());
        }
    }
    
    /**
     * Visualiza detalles de un cluster específico.
     * 
     * @param analysisId ID del análisis
     * @param reader BufferedReader para leer entrada del usuario
     */
    private void visualizarClusterEspecifico(String analysisId, BufferedReader reader) {
        try {
            System.out.print("Introduce el indice del cluster (empezando desde 0): ");
            int clusterIndex = Integer.parseInt(reader.readLine());
            
            System.out.println("\n========================================");
            System.out.println("   DETALLES DEL CLUSTER " + clusterIndex);
            System.out.println("========================================");
            String info = ctrlDominioClustering.obtenerInfoCluster(analysisId, clusterIndex);
            System.out.println(info);
            System.out.println("========================================\n");
        } catch (IOException e) {
            System.out.println("Error al leer el indice: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error al obtener informacion del cluster: " + e.getMessage());
        }
    }
    
    /**
     * Visualiza todos los detalles del análisis de forma completa.
     * 
     * @param analysisId ID del análisis
     */
    private void visualizarTodo(String analysisId) {
        try {
            ClusterResults results = ctrlDominioClustering.obtenerResultados(analysisId);
            
            System.out.println("\n========================================");
            System.out.println("   ANALISIS COMPLETO DE CLUSTERING");
            System.out.println("========================================");
            
            // Resumen general
            System.out.println("\n--- RESUMEN GENERAL ---");
            String resumen = ctrlDominioClustering.obtenerResumen(analysisId);
            System.out.println(resumen);
            
            // Distribución
            System.out.println("\n--- DISTRIBUCION DE CLUSTERS ---");
            List<Cluster> clusters = results.getClusters();
            int maxSize = 0;
            for (Cluster cluster : clusters) {
                if (cluster.getSize() > maxSize) {
                    maxSize = cluster.getSize();
                }
            }
            
            for (int i = 0; i < clusters.size(); i++) {
                Cluster cluster = clusters.get(i);
                int size = cluster.getSize();
                double percentage = (size * 100.0) / results.getNumberOfResponses();
                
                System.out.printf("Cluster %d: [%-20s] %3d respuestas (%.1f%%)%n", 
                    i, 
                    generarBarraASCII(size, maxSize, 20), 
                    size, 
                    percentage);
            }
            
            // Detalles de cada cluster
            System.out.println("\n--- DETALLES DE CADA CLUSTER ---");
            for (int i = 0; i < clusters.size(); i++) {
                System.out.println("\nCluster " + i + ":");
                String info = ctrlDominioClustering.obtenerInfoCluster(analysisId, i);
                System.out.println(info);
            }
            
            System.out.println("\n========================================\n");
        } catch (Exception e) {
            System.out.println("Error al visualizar todo: " + e.getMessage());
        }
    }
    
    /**
     * Genera una barra ASCII para representar visualmente un valor.
     * 
     * @param value Valor actual
     * @param maxValue Valor máximo
     * @param length Longitud de la barra
     * @return String con la barra en ASCII
     */
    private String generarBarraASCII(int value, int maxValue, int length) {
        if (maxValue == 0) return "";
        
        int filledLength = (int) ((value * length) / (double) maxValue);
        StringBuilder bar = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            if (i < filledLength) {
                bar.append("█");
            } else {
                bar.append(" ");
            }
        }
        
        return bar.toString();
    }
}
