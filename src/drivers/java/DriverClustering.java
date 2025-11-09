package drivers;

import domain.controller.CtrlDominioClustering;
import domain.clustering.ClusterResults;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Driver CLI para probar los algoritmos de clustering.
 * Permite crear datos sintéticos, ejecutar algoritmos, comparar resultados
 * y visualizar información de clusters.
 * 
 * @author Tu nombre
 */
public class DriverClustering {

    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private static CtrlDominioClustering controller = new CtrlDominioClustering();
    private static Map<String, Object[][]> datasets = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("    DRIVER DE CLUSTERING - PROP 2024/25");
        System.out.println("==============================================\n");

        boolean running = true;
        while (running) {
            try {
                mostrarMenu();
                String opcion = leerLinea("Selecciona una opción: ").trim().toUpperCase();

                switch (opcion) {
                    case "1":
                    case "CREAR":
                        crearDatosSinteticos();
                        break;
                    case "2":
                    case "CARGAR":
                        cargarDatosManual();
                        break;
                    case "3":
                    case "EJECUTAR":
                        ejecutarAlgoritmo();
                        break;
                    case "4":
                    case "COMPARAR":
                        compararAlgoritmos();
                        break;
                    case "5":
                    case "VER":
                        verResultados();
                        break;
                    case "6":
                    case "INFO":
                        verInfoCluster();
                        break;
                    case "7":
                    case "LISTAR":
                        listarAnalisis();
                        break;
                    case "8":
                    case "LIMPIAR":
                        limpiarAnalisis();
                        break;
                    case "0":
                    case "EXIT":
                    case "SALIR":
                        running = false;
                        System.out.println("\n¡Hasta luego! Cerrando driver...");
                        break;
                    default:
                        System.out.println("⚠ Opción no válida. Intenta de nuevo.\n");
                }
            } catch (IOException e) {
                System.err.println("❌ Error de entrada/salida: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("❌ Error inesperado: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Muestra el menú principal del driver.
     */
    private static void mostrarMenu() {
        System.out.println("\n╔══════════════════════ MENÚ PRINCIPAL ═══════════════════════╗");
        System.out.println("║  1. CREAR      - Generar datos sintéticos                  ║");
        System.out.println("║  2. CARGAR     - Cargar datos manualmente                  ║");
        System.out.println("║  3. EJECUTAR   - Ejecutar un algoritmo de clustering       ║");
        System.out.println("║  4. COMPARAR   - Comparar los 3 algoritmos                 ║");
        System.out.println("║  5. VER        - Ver resultados de un análisis             ║");
        System.out.println("║  6. INFO       - Ver información de un cluster específico  ║");
        System.out.println("║  7. LISTAR     - Listar todos los análisis guardados       ║");
        System.out.println("║  8. LIMPIAR    - Limpiar todos los análisis                ║");
        System.out.println("║  0. SALIR      - Cerrar el driver                          ║");
        System.out.println("╚═════════════════════════════════════════════════════════════╝");
    }

    /**
     * Crea un dataset sintético con clusters bien definidos.
     */
    private static void crearDatosSinteticos() throws IOException {
        System.out.println("\n--- CREAR DATOS SINTÉTICOS ---");

        String nombre = leerLinea("Nombre del dataset: ");
        int numPuntos = leerEntero("Número de puntos por cluster: ");
        int numClusters = leerEntero("Número de clusters: ");
        int dimensiones = leerEntero("Número de dimensiones: ");

        // Generar datos con clusters separados
        Object[][] data = generarDatosClusters(numPuntos, numClusters, dimensiones);
        datasets.put(nombre, data);

        System.out.println("✓ Dataset '" + nombre + "' creado con " + data.length + " puntos.");
        System.out.println("  Dimensiones: " + dimensiones);
        System.out.println("  Clusters teóricos: " + numClusters);
        
        // Mostrar muestra de datos
        System.out.println("\n  Muestra (primeros 3 puntos):");
        for (int i = 0; i < Math.min(3, data.length); i++) {
            System.out.print("  Punto " + i + ": [");
            for (int j = 0; j < data[i].length; j++) {
                System.out.print(String.format("%.2f", data[i][j]));
                if (j < data[i].length - 1) System.out.print(", ");
            }
            System.out.println("]");
        }
    }

    /**
     * Genera datos sintéticos con clusters bien separados.
     */
    private static Object[][] generarDatosClusters(int puntosPerCluster, int numClusters, int dimensiones) {
        int totalPuntos = puntosPerCluster * numClusters;
        Object[][] data = new Object[totalPuntos][dimensiones];

        for (int cluster = 0; cluster < numClusters; cluster++) {
            // Centro del cluster (aleatorio en rango amplio)
            double[] centro = new double[dimensiones];
            for (int d = 0; d < dimensiones; d++) {
                centro[d] = (cluster * 10.0) + Math.random() * 5.0; // Clusters separados
            }

            // Generar puntos alrededor del centro
            for (int p = 0; p < puntosPerCluster; p++) {
                int idx = cluster * puntosPerCluster + p;
                for (int d = 0; d < dimensiones; d++) {
                    // Añadir ruido gaussiano al centro
                    double ruido = (Math.random() - 0.5) * 2.0; // Ruido en [-1, 1]
                    data[idx][d] = centro[d] + ruido;
                }
            }
        }

        return data;
    }

    /**
     * Carga datos manualmente desde la entrada del usuario.
     */
    private static void cargarDatosManual() throws IOException {
        System.out.println("\n--- CARGAR DATOS MANUALMENTE ---");

        String nombre = leerLinea("Nombre del dataset: ");
        int numPuntos = leerEntero("Número de puntos: ");
        int dimensiones = leerEntero("Número de dimensiones: ");

        Object[][] data = new Object[numPuntos][dimensiones];

        System.out.println("\nIntroduce los datos (separados por espacios):");
        for (int i = 0; i < numPuntos; i++) {
            System.out.print("Punto " + i + ": ");
            String[] valores = leerLinea("").trim().split("\\s+");
            
            if (valores.length != dimensiones) {
                System.out.println("⚠ Error: se esperaban " + dimensiones + " valores, se recibieron " + valores.length);
                i--; // Reintentar este punto
                continue;
            }

            for (int j = 0; j < dimensiones; j++) {
                try {
                    data[i][j] = Double.parseDouble(valores[j]);
                } catch (NumberFormatException e) {
                    System.out.println("⚠ Error: '" + valores[j] + "' no es un número válido");
                    i--; // Reintentar este punto
                    break;
                }
            }
        }

        datasets.put(nombre, data);
        System.out.println("✓ Dataset '" + nombre + "' cargado con " + numPuntos + " puntos.");
    }

    /**
     * Ejecuta un algoritmo de clustering sobre un dataset.
     */
    private static void ejecutarAlgoritmo() throws IOException {
        System.out.println("\n--- EJECUTAR CLUSTERING ---");

        if (datasets.isEmpty()) {
            System.out.println("⚠ No hay datasets cargados. Crea o carga datos primero.");
            return;
        }

        // Seleccionar dataset
        System.out.println("\nDatasets disponibles:");
        for (String nombre : datasets.keySet()) {
            Object[][] data = datasets.get(nombre);
            System.out.println("  - " + nombre + " (" + data.length + " puntos, " + data[0].length + " dim)");
        }

        String datasetNombre = leerLinea("\nDataset a usar: ");
        if (!datasets.containsKey(datasetNombre)) {
            System.out.println("⚠ Dataset no encontrado.");
            return;
        }

        Object[][] data = datasets.get(datasetNombre);

        // Seleccionar algoritmo
        System.out.println("\nAlgoritmos disponibles:");
        System.out.println("  1. KMeans");
        System.out.println("  2. KMeansPlusPlus");
        System.out.println("  3. KMedoids");

        String algoOpcion = leerLinea("Algoritmo (1-3): ").trim();
        String algoritmo;
        switch (algoOpcion) {
            case "1":
                algoritmo = "KMeans";
                break;
            case "2":
                algoritmo = "KMeansPlusPlus";
                break;
            case "3":
                algoritmo = "KMedoids";
                break;
            default:
                System.out.println("⚠ Opción no válida.");
                return;
        }

        // Parámetros
        int k = leerEntero("Número de clusters (k): ");
        
        System.out.print("¿Configurar parámetros avanzados? (s/n): ");
        String avanzado = leerLinea("").trim().toLowerCase();
        
        int maxIter = 100;
        double tolerance = 1e-4;
        
        if (avanzado.equals("s") || avanzado.equals("si")) {
            maxIter = leerEntero("Máximo de iteraciones [100]: ", 100);
            tolerance = leerDouble("Tolerancia de convergencia [0.0001]: ", 1e-4);
        }

        // Generar ID único para el análisis
        String analisisId = datasetNombre + "_" + algoritmo + "_k" + k + "_" + System.currentTimeMillis();

        System.out.println("\n⏳ Ejecutando " + algoritmo + " con k=" + k + "...");

        long startTime = System.currentTimeMillis();
        String resultado = controller.ejecutarClustering(analisisId, algoritmo, data, k, maxIter, tolerance);
        long endTime = System.currentTimeMillis();

        System.out.println("✓ " + resultado);
        System.out.println("  Tiempo de ejecución: " + (endTime - startTime) + " ms");
        System.out.println("  ID del análisis: " + analisisId);

        // Mostrar resumen
        mostrarResumen(analisisId);
    }

    /**
     * Compara los 3 algoritmos sobre el mismo dataset.
     */
    private static void compararAlgoritmos() throws IOException {
        System.out.println("\n--- COMPARAR ALGORITMOS ---");

        if (datasets.isEmpty()) {
            System.out.println("⚠ No hay datasets cargados. Crea o carga datos primero.");
            return;
        }

        // Seleccionar dataset
        System.out.println("\nDatasets disponibles:");
        for (String nombre : datasets.keySet()) {
            Object[][] data = datasets.get(nombre);
            System.out.println("  - " + nombre + " (" + data.length + " puntos, " + data[0].length + " dim)");
        }

        String datasetNombre = leerLinea("\nDataset a usar: ");
        if (!datasets.containsKey(datasetNombre)) {
            System.out.println("⚠ Dataset no encontrado.");
            return;
        }

        Object[][] data = datasets.get(datasetNombre);
        int k = leerEntero("Número de clusters (k): ");

        System.out.println("\n⏳ Ejecutando los 3 algoritmos con k=" + k + "...\n");

        String[] algoritmos = {"KMeans", "KMeansPlusPlus", "KMedoids"};
        Map<String, Long> tiempos = new HashMap<>();
        Map<String, String> ids = new HashMap<>();

        for (String algoritmo : algoritmos) {
            String analisisId = datasetNombre + "_" + algoritmo + "_k" + k + "_" + System.currentTimeMillis();
            ids.put(algoritmo, analisisId);

            System.out.println("➤ Ejecutando " + algoritmo + "...");
            long startTime = System.currentTimeMillis();
            controller.ejecutarClustering(analisisId, algoritmo, data, k, 100, 1e-4);
            long endTime = System.currentTimeMillis();
            
            tiempos.put(algoritmo, endTime - startTime);
        }

        // Mostrar comparación
        System.out.println("\n╔═══════════════════════ COMPARACIÓN ═══════════════════════╗");
        System.out.println(String.format("║ %-20s ║ %10s ║ %10s ║ %10s ║", "Algoritmo", "Tiempo (ms)", "Iterac.", "Converg."));
        System.out.println("╠════════════════════════════════════════════════════════════╣");

        for (String algoritmo : algoritmos) {
            String analisisId = ids.get(algoritmo);
            ClusterResults results = controller.obtenerResultados(analisisId);
            
            System.out.println(String.format("║ %-20s ║ %10d ║ %10d ║ %10s ║",
                algoritmo,
                tiempos.get(algoritmo),
                results.getIterations(),
                results.isConverged() ? "Sí" : "No"
            ));
        }
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        // Mostrar distribución de clusters
        System.out.println("\n--- Distribución de puntos por cluster ---");
        for (String algoritmo : algoritmos) {
            String analisisId = ids.get(algoritmo);
            System.out.println("\n" + algoritmo + ":");
            mostrarDistribucion(analisisId);
        }
    }

    /**
     * Muestra los resultados de un análisis guardado.
     */
    private static void verResultados() throws IOException {
        System.out.println("\n--- VER RESULTADOS ---");

        String analisisId = leerLinea("ID del análisis: ");
        
        try {
            mostrarResumen(analisisId);
            mostrarDistribucion(analisisId);
        } catch (IllegalArgumentException e) {
            System.out.println("⚠ " + e.getMessage());
        }
    }

    /**
     * Muestra información detallada de un cluster específico.
     */
    private static void verInfoCluster() throws IOException {
        System.out.println("\n--- INFORMACIÓN DE CLUSTER ---");

        String analisisId = leerLinea("ID del análisis: ");
        int clusterId = leerEntero("ID del cluster: ");

        try {
            String info = controller.obtenerInfoCluster(analisisId, clusterId);
            System.out.println("\n" + info);
        } catch (IllegalArgumentException e) {
            System.out.println("⚠ " + e.getMessage());
        }
    }

    /**
     * Lista todos los análisis guardados.
     */
    private static void listarAnalisis() {
        System.out.println("\n--- ANÁLISIS GUARDADOS ---");
        
        List<String> analisis = controller.listarAnalisis();
        
        if (analisis.isEmpty()) {
            System.out.println("  No hay análisis guardados.");
        } else {
            System.out.println("  Total: " + analisis.size() + " análisis");
            for (int i = 0; i < analisis.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + analisis.get(i));
            }
        }
    }

    /**
     * Limpia todos los análisis guardados.
     */
    private static void limpiarAnalisis() throws IOException {
        System.out.print("\n¿Estás seguro de limpiar todos los análisis? (s/n): ");
        String confirmacion = leerLinea("").trim().toLowerCase();
        
        if (confirmacion.equals("s") || confirmacion.equals("si")) {
            controller.limpiarAnalisis();
            System.out.println("✓ Todos los análisis han sido eliminados.");
        } else {
            System.out.println("  Operación cancelada.");
        }
    }

    /**
     * Muestra un resumen del análisis.
     */
    private static void mostrarResumen(String analisisId) {
        String resumen = controller.obtenerResumen(analisisId);
        System.out.println("\n" + resumen);
    }

    /**
     * Muestra la distribución de puntos por cluster.
     */
    private static void mostrarDistribucion(String analisisId) {
        ClusterResults results = controller.obtenerResultados(analisisId);
        Integer[] asignaciones = results.getClusterAssignments();
        
        // Contar puntos por cluster
        Map<Integer, Integer> conteo = new HashMap<>();
        for (int clusterId : asignaciones) {
            conteo.put(clusterId, conteo.getOrDefault(clusterId, 0) + 1);
        }

        // Mostrar distribución
        for (int i = 0; i < results.getK(); i++) {
            int count = conteo.getOrDefault(i, 0);
            double porcentaje = (count * 100.0) / asignaciones.length;
            System.out.println(String.format("  Cluster %d: %4d puntos (%.1f%%)", i, count, porcentaje));
        }
    }

    // ==================== UTILIDADES DE ENTRADA ====================

    private static String leerLinea(String prompt) throws IOException {
        System.out.print(prompt);
        return reader.readLine();
    }

    private static int leerEntero(String prompt) throws IOException {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(reader.readLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("⚠ Por favor, introduce un número entero válido.");
            }
        }
    }

    private static int leerEntero(String prompt, int defaultValue) throws IOException {
        System.out.print(prompt);
        String input = reader.readLine().trim();
        if (input.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("⚠ Valor no válido, usando " + defaultValue);
            return defaultValue;
        }
    }

    private static double leerDouble(String prompt, double defaultValue) throws IOException {
        System.out.print(prompt);
        String input = reader.readLine().trim();
        if (input.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("⚠ Valor no válido, usando " + defaultValue);
            return defaultValue;
        }
    }
}
