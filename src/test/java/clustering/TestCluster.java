import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Asume que tienes mocks/stubs para estas clases
import stubs.StubResponseSet;

class ClusterTests {

    private Cluster cluster;
    private StubResponseSet rs1;
    private StubResponseSet rs2;

    @BeforeEach
    void setUp() {
        cluster = new Cluster("c1");
        rs1 = new StubResponseSet("rs1");
        rs2 = new StubResponseSet("rs2");
    }

    @Test
    @DisplayName("Cluster: Constructor y Label")
    void testClusterConstructor() {
        assertEquals("c1", cluster.getId());
        assertEquals("c1", cluster.getLabel()); // Label por defecto
        cluster.setLabel("Mi Super Cluster");
        assertEquals("Mi Super Cluster", cluster.getLabel());
    }

    @Test
    @DisplayName("Cluster: Añadir Miembros y Tamaño")
    void testAddMemberAndSize() {
        assertEquals(0, cluster.getSize());
        
        cluster.addMember(rs1, 10.0);
        assertEquals(1, cluster.getSize());

        cluster.addMember(rs2, 20.0);
        assertEquals(2, cluster.getSize());
    }

    @Test
    @DisplayName("Cluster: Comprobar y Eliminar Miembros")
    void testContainsAndRemoveMember() {
        cluster.addMember(rs1, 10.0);
        cluster.addMember(rs2, 20.0);

        assertTrue(cluster.contains("rs1"));
        assertTrue(cluster.contains("rs2"));
        assertFalse(cluster.contains("rs_inexistente"));

        boolean removed = cluster.removeMember("rs1");
        assertTrue(removed);
        assertEquals(1, cluster.getSize());
        assertFalse(cluster.contains("rs1"));
        assertTrue(cluster.contains("rs2"));
    }

    @Test
    @DisplayName("Cluster: Distancia Media")
    void testAverageDistance() {
        assertEquals(0.0, cluster.getAverageDistance(), "La distancia media de un cluster vacío debe ser 0");

        cluster.addMember(rs1, 10.0);
        assertEquals(10.0, cluster.getAverageDistance());

        cluster.addMember(rs2, 20.0);
        assertEquals(15.0, cluster.getAverageDistance()); // (10 + 20) / 2
    }
}