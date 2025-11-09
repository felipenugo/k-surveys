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

class DistanceCalculatorTests {

    private DistanceCalculator euclideanCalc;
    private DistanceCalculator manhattanCalc;
    
    // Nuestros datos de prueba
    private StubOpenQuestion q_num;
    private StubChoiceQuestion q_choice;
    private StubOpenQuestion q_text;
    private List<Question> questions;

    @BeforeEach
    void setUp() {
        euclideanCalc = new DistanceCalculator(DistanceType.EUCLIDEAN);
        manhattanCalc = new DistanceCalculator(DistanceType.MANHATTAN);

        // --- Definir Preguntas ---
        // Q1: Numérica (0-100)
        q_num = new StubOpenQuestion("q_num", true, 0.0, 100.0); 
        // Q2: Choice (3 opciones)
        q_choice = new StubChoiceQuestion("q_choice", 3);
        // Q3: Texto
        q_text = new StubOpenQuestion("q_text", false, 0.0, 0.0);
        
        questions = List.of(q_num, q_choice, q_text);
    }

    // --- Helper para crear un ResponseSet ---
    private StubResponseSet createResponseSet(String id, String numVal, boolean[] choiceVal, String textVal) {
        StubResponseSet rs = new StubResponseSet(id);
        rs.addResponse(new StubTextualResponse("q_num", numVal));
        rs.addResponse(new StubMultipleChoiceResponse("q_choice", choiceVal));
        rs.addResponse(new StubTextualResponse("q_text", textVal));
        return rs;
    }

    // --- Tests para calculate() (RS vs RS) ---
    
    @Test
    @DisplayName("DistCalc(RS, RS): Dos puntos idénticos")
    void testCalculateIdentical() {
        StubResponseSet rs1 = createResponseSet("rs1", "50", new boolean[]{true, false, true}, "hola");
        double dist = euclideanCalc.calculate(rs1, rs1, questions);
        assertEquals(0.0, dist);
    }
    
    @Test
    @DisplayName("DistCalc(RS, RS): Diferencia solo Numérica (0 vs 100)")
    void testCalculateNumericOnly() {
        StubResponseSet rs1 = createResponseSet("rs1", "0",   new boolean[]{true, false, true}, "hola");
        StubResponseSet rs2 = createResponseSet("rs2", "100", new boolean[]{true, false, true}, "hola");
        // Distancia local = |(100-0)/(100-0)| = 1.0
        // Distancia total = sqrt(1.0^2 + 0^2 + 0^2) = 1.0
        double dist = euclideanCalc.calculate(rs1, rs2, questions);
        assertEquals(1.0, dist);
    }

    @Test
    @DisplayName("DistCalc(RS, RS): Diferencia solo Choice")
    void testCalculateChoiceOnly() {
        StubResponseSet rs1 = createResponseSet("rs1", "50", new boolean[]{true, false, false}, "hola"); // Inter: 1, Union: 1
        StubResponseSet rs2 = createResponseSet("rs2", "50", new boolean[]{true, true, false}, "hola"); // Inter: 1, Union: 2
        // Jaccard: 1 - (1/2) = 0.5
        // Distancia total = sqrt(0^2 + 0.5^2 + 0^2) = 0.5
        double dist = euclideanCalc.calculate(rs1, rs2, questions);
        assertEquals(0.5, dist);
    }

    @Test
    @DisplayName("DistCalc(RS, RS): Diferencia solo Texto")
    void testCalculateTextOnly() {
        StubResponseSet rs1 = createResponseSet("rs1", "50", new boolean[]{true, false, true}, "abcde");
        StubResponseSet rs2 = createResponseSet("rs2", "50", new boolean[]{true, false, true}, "abXde");
        // Distancia local = 1 (charDiff) / 5 (maxLen) = 0.2
        // Distancia total = sqrt(0^2 + 0^2 + 0.2^2) = 0.2
        double dist = euclideanCalc.calculate(rs1, rs2, questions);
        assertEquals(0.2, dist, 1e-9);
    }

    @Test
    @DisplayName("DistCalc(RS, RS): Distancia Manhattan con Pesos")
    void testCalculateManhattanWithWeights() {
        manhattanCalc.setWeight("q_num", 2.0); // Doble peso a la numérica
        manhattanCalc.setWeight("q_text", 0.5); // Mitad de peso al texto

        StubResponseSet rs1 = createResponseSet("rs1", "0",   new boolean[]{true, false, false}, "hola");
        StubResponseSet rs2 = createResponseSet("rs2", "50",  new boolean[]{true, true, false}, "adios");
        
        // Distancia local numérica: |(50-0)/(100-0)| = 0.5. Con peso = 0.5 * 2.0 = 1.0
        // Distancia local choice: 1 - (1/2) = 0.5. Con peso = 0.5 * 1.0 = 0.5
        // Distancia local texto: 4 (charDiff) / 5 (maxLen) = 0.8. Con peso = 0.8 * 0.5 = 0.4
        // Distancia total Manhattan = 1.0 + 0.5 + 0.4 = 1.9
        
        double dist = manhattanCalc.calculate(rs1, rs2, questions);
        assertEquals(1.9, dist, 1e-9);
    }

    // --- Tests para calculateToCentroid() (RS vs Centroid) ---

    @Test
    @DisplayName("DistCalc(RS, C): Punto coincide con Centroide")
    void testCalculateToCentroidIdentical() {
        StubResponseSet rs1 = createResponseSet("rs1", "50", new boolean[]{true, false, false}, "hola");
        Centroid c1 = new Centroid(List.of("q_num", "q_choice", "q_text"));
        c1.setComponent(0, 50.0);                     // Promedio numérico
        c1.setComponent(1, new double[]{1.0, 0.0, 0.0}); // Promedio choice
        c1.setComponent(2, "hola");                   // Medoide de texto
        
        // Distancia local num: |(50-50)/(100-0)| = 0.0
        // Distancia local choice: sqrt((1-1)^2 + (0-0)^2 + (0-0)^2) = 0.0
        // Distancia local texto: 0.0
        // Distancia total = sqrt(0) = 0.0
        
        double dist = euclideanCalc.calculateToCentroid(rs1, c1, questions);
        assertEquals(0.0, dist, 1e-9);
    }

    @Test
    @DisplayName("DistCalc(RS, C): Punto vs Centroide Mixto")
    void testCalculateToCentroidMixed() {
        StubResponseSet rs1 = createResponseSet("rs1", "0", new boolean[]{true, false, true}, "abc"); // [1,0,1]
        Centroid c1 = new Centroid(List.of("q_num", "q_choice", "q_text"));
        c1.setComponent(0, 100.0);                    // Promedio numérico
        c1.setComponent(1, new double[]{0.5, 0.5, 0.5}); // Promedio choice
        c1.setComponent(2, "xyz");                    // Medoide de texto
        
        // Distancia local num: |(0-100)/(100-0)| = 1.0
        // Distancia local choice: sqrt((1-0.5)^2 + (0-0.5)^2 + (1-0.5)^2) = sqrt(0.25 + 0.25 + 0.25) = sqrt(0.75)
        // Distancia local texto: 3 (charDiff) / 3 (maxLen) = 1.0
        
        // Distancia total Euclidiana = sqrt( (1.0)^2 + (sqrt(0.75))^2 + (1.0)^2 )
        // = sqrt( 1.0 + 0.75 + 1.0 ) = sqrt(2.75)
        
        double expectedDist = Math.sqrt(1.0 + 0.75 + 1.0);
        double dist = euclideanCalc.calculateToCentroid(rs1, c1, questions);
        assertEquals(expectedDist, dist, 1e-9);
    }
}