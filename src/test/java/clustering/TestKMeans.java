import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

// Asume que tienes mocks/stubs para estas clases
import stubs.StubOpenQuestion;
import stubs.StubChoiceQuestion;
import stubs.StubResponseSet;
import stubs.StubTextualResponse;
import stubs.StubMultipleChoiceResponse;

class KMeansTests {

    private KMeans kmeans;
    private DistanceCalculator distanceCalc;
    
    // Datos de prueba
    private StubOpenQuestion q_num;
    private StubOpenQuestion q_text;
    private List<Question> questions;
    private List<ResponseSet> data;

    @BeforeEach
    void setUp() {
        // k=2, 10 iteraciones, tolerancia baja
        kmeans = new KMeans(10, 1e-5); 
        kmeans.setRandomSeed(42); // Para que los centroides iniciales sean consistentes
        distanceCalc = new DistanceCalculator(DistanceType.EUCLIDEAN);

        // --- Definir Preguntas ---
        q_num = new StubOpenQuestion("q_num", true, 0.0, 10.0); // Numérica (0-10)
        q_text = new StubOpenQuestion("q_text", false, 0.0, 0.0); // Texto
        questions = List.of(q_num, q_text);

        // --- Definir Puntos de Datos (2 clusters obvios) ---
        // Cluster A: "Bajo y 'A'"
        StubResponseSet rsA1 = createResponseSet("rsA1", "1", "A");
        StubResponseSet rsA2 = createResponseSet("rsA2", "2", "A");
        StubResponseSet rsA3 = createResponseSet("rsA3", "1", "A");
        // Cluster B: "Alto y 'B'"
        StubResponseSet rsB1 = createResponseSet("rsB1", "9", "B");
        StubResponseSet rsB2 = createResponseSet("rsB2", "10", "B");
        StubResponseSet rsB3 = createResponseSet("rsB3", "8", "B");
        
        data = List.of(rsA1, rsA2, rsA3, rsB1, rsB2, rsB3);
    }

    // --- Helper para crear un ResponseSet (simplificado) ---
    private StubResponseSet createResponseSet(String id, String numVal, String textVal) {
        StubResponseSet rs = new StubResponseSet(id);
        rs.addResponse(new StubTextualResponse("q_num", numVal));
        rs.addResponse(new StubTextualResponse("q_text", textVal));
        return rs;
    }

    @Test
    @DisplayName("KMeans: execute() encuentra clusters perfectos (k=2)")
    void testKMeansExecute() {
        // Ejecutar el algoritmo
        List<Cluster> clusters = kmeans.execute(data, questions, 2, distanceCalc);

        // 1. Comprobar que tenemos 2 clusters
        assertEquals(2, clusters.size());

        // 2. Encontrar los clusters A y B (el orden puede variar)
        Cluster clusterA = clusters.get(0).contains("rsA1") ? clusters.get(0) : clusters.get(1);
        Cluster clusterB = clusters.get(0).contains("rsB1") ? clusters.get(0) : clusters.get(1);

        // 3. Comprobar el tamaño y los miembros de cada cluster
        assertEquals(3, clusterA.getSize());
        assertTrue(clusterA.contains("rsA1"));
        assertTrue(clusterA.contains("rsA2"));
        assertTrue(clusterA.contains("rsA3"));
        
        assertEquals(3, clusterB.getSize());
        assertTrue(clusterB.contains("rsB1"));
        assertTrue(clusterB.contains("rsB2"));
        assertTrue(clusterB.contains("rsB3"));

        // 4. (CRÍTICO) Comprobar los centroides Híbridos (Mean + Medoid)
        Centroid centroidA = clusterA.getCentroid();
        Centroid centroidB = clusterB.getCentroid();

        // Centroide A: Promedio de (1, 2, 1) = 1.333. Medoide de ("A", "A", "A") = "A"
        assertEquals(1.333, (Double) centroidA.getComponent(0), 1e-3);
        assertEquals("A", (String) centroidA.getComponent(1));
        
        // Centroide B: Promedio de (9, 10, 8) = 9.0. Medoide de ("B", "B", "B") = "B"
        assertEquals(9.0, (Double) centroidB.getComponent(0), 1e-3);
        assertEquals("B", (String) centroidB.getComponent(1));
    }
    
    @Test
    @DisplayName("KMeans: k=1 devuelve un solo cluster")
    void testKMeansK1() {
        List<Cluster> clusters = kmeans.execute(data, questions, 1, distanceCalc);
        
        assertEquals(1, clusters.size());
        assertEquals(6, clusters.get(0).getSize());
    }
}