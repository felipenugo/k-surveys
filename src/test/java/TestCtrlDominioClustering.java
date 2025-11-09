import static org.junit.Assert.*;
import org.junit.Test;
import domain.clustering.*;
import domain.controller.CtrlDominioClustering;

/**
 * Test para CtrlDominioClustering.
 */
public class TestCtrlDominioClustering {
    
    @Test
    public void testEjecucionBasica() {
        CtrlDominioClustering ctrl = new CtrlDominioClustering();
        
        Object[][] data = {
            {1.0, 1.0}, {1.5, 1.2},
            {8.0, 8.0}, {8.5, 8.2}
        };
        
        String id = ctrl.ejecutarClustering(data, 2, "KMeans");
        ClusterResults results = ctrl.obtenerResultados(id);
        
        assertNotNull(results);
        assertEquals(2, results.getK());
        assertTrue(results.hasConverged());
    }
    
    @Test
    public void testMultiplesAnalisis() {
        CtrlDominioClustering ctrl = new CtrlDominioClustering();
        
        Object[][] data = {{1.0, 1.0}, {9.0, 9.0}};
        
        String id1 = ctrl.ejecutarClustering(data, 2, "KMeans");
        String id2 = ctrl.ejecutarClustering(data, 2, "KMeansPlusPlus");
        String id3 = ctrl.ejecutarClustering(data, 2, "KMedoids");
        
        assertEquals(3, ctrl.getNumeroAnalisis());
        assertEquals(3, ctrl.listarAnalisis().size());
    }
    
    @Test
    public void testResumen() {
        CtrlDominioClustering ctrl = new CtrlDominioClustering();
        
        Object[][] data = {{1.0, 1.0}, {8.0, 8.0}};
        String id = ctrl.ejecutarClustering(data, 2, "KMeans");
        
        String resumen = ctrl.obtenerResumen(id);
        assertNotNull("El resumen no debe ser null", resumen);
        assertFalse("El resumen no debe estar vacío", resumen.isEmpty());
    }
}
