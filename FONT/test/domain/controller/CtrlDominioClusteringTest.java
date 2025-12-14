package domain.controller;

import data.AnswerRepository;
import data.QuestionRepository;
import data.ResponseRepository;
import data.SurveyRepository;
import data.UserRepository;
import domain.clustering.*;
import domain.model.*;
import domain.model.enums.TypeQuestion;
import domain.service.ResponseService;
import domain.service.SurveyService;
import domain.service.UserService;
import org.junit.Before;
import org.junit.Test;
import domain.clustering.TextDistanceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests completos para CtrlDominioClustering.
 * Basado en FullWorkflowTest con estructura completa.
 */
public class CtrlDominioClusteringTest {

    private UserController userController;
    private SurveyController surveyController;
    private ResponseController responseController;
    private AnalysisController analysisController;
    private ResponseRepository responseRepository;
    private SurveyRepository surveyRepository;

    private List<Question> questions;
    private Survey survey;
    private List<Response> responses;


    @Before
    public void setUp() {
        UserRepository userRepository = new UserRepository();
        this.surveyRepository = new SurveyRepository();
        this.responseRepository = new ResponseRepository();
        userRepository.clear();
        this.surveyRepository.clear();
        this.responseRepository.clear();
        UserService userService = new UserService(userRepository, this.surveyRepository, this.responseRepository);

        // Inicializar servicios y controladores
        userController = new UserController(userService);

        SurveyService surveyService = new SurveyService(surveyRepository, userController, userService);
        surveyController = new SurveyController(surveyService);

        ResponseService responseService = new ResponseService(this.responseRepository, userController, surveyService);
        responseController = new ResponseController(responseService);

        analysisController = new AnalysisController();
    }

    @Test
    public void testCreateAnalysisWithKMeans() {
        // Paso 1: Registrar e Iniciar Sesión
        String username = "testUser";
        userController.registerUser(username, "test@gmail.com", "pass   word", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        // Paso 2: Crear Encuesta con Preguntas Mixtas
        createTestSurvey();
        assertNotNull(survey);
        assertEquals(3, questions.size());

        // Paso 3: Generar Respuestas Diversas
        generateTestResponses(50);

        // Obtener respuestas del repositorio para análisis
        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        // Paso 4: Crear Análisis KMeans
        int k = 2;
        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, k, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN, TextDistanceType.LEVENSHTEIN);

        analysis.execute(responses, questions, distance);

        assertNotNull(analysis);
        assertEquals(k, analysis.getClusters().size());
        assertTrue(analysis.hasConverged());
    }

    @Test
    public void testExecuteAnalysisWithKMedoids() {
        String username = "testUser2";
        userController.registerUser(username, "test2@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        int k = 3;
        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, k, new KMedoids());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN, TextDistanceType.LEVENSHTEIN);

        analysis.execute(responses, questions, distance);

        assertEquals(k, analysis.getClusters().size());
        assertTrue(analysis.getIterations() >= 0);
        assertNotNull(analysis.getExecutionTime());
    }

    @Test
    public void testClusterDistribution() {
        String username = "testUser3";
        userController.registerUser(username, "test3@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(60);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(60, responses.size());

        int k = 3;
        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, k, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN, TextDistanceType.LEVENSHTEIN);

        analysis.execute(responses, questions, distance);

        List<Cluster> clusters = analysis.getClusters();
        int totalMembers = clusters.stream().mapToInt(Cluster::getSize).sum();
        assertEquals(60, totalMembers);
        assertEquals(3, clusters.size());
    }

    @Test
    public void testCentroidCalculation() {
        String username = "testUser4";
        userController.registerUser(username, "test4@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN, TextDistanceType.LEVENSHTEIN);

        analysis.execute(responses, questions, distance);

        for (Cluster cluster : analysis.getClusters()) {
            Centroid centroid = cluster.getCentroid();
            assertNotNull(centroid);
            assertEquals(3, centroid.getNumDimensions()); // 3 preguntas
        }
    }

    @Test
    public void testAnalysisExecutionTime() {
        String username = "testUser5";
        userController.registerUser(username, "test5@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN, TextDistanceType.LEVENSHTEIN);

        analysis.execute(responses, questions, distance);

        Long executionTime = analysis.getExecutionTime();
        assertNotNull(executionTime);
        assertTrue(executionTime >= 0);
    }

    @Test
    public void testExportAnalysisResults() {
        String username = "testUser6";
        userController.registerUser(username, "test6@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN, TextDistanceType.LEVENSHTEIN);

        analysis.execute(responses, questions, distance);

        String exportedResults = analysis.exportResults();
        assertNotNull(exportedResults);
        assertTrue(exportedResults.length() > 0);
    }

    @Test
    public void testAnalysisConvergence() {
        String username = "testUser7";
        userController.registerUser(username, "test7@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN, TextDistanceType.LEVENSHTEIN);

        analysis.execute(responses, questions, distance);

        Boolean converged = analysis.hasConverged();
        assertNotNull(converged);
        assertTrue(converged);
    }

    @Test
    public void testDifferentKValues() {
        String username = "testUser8";
        userController.registerUser(username, "test8@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(100);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(100, responses.size());

        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN, TextDistanceType.LEVENSHTEIN);

        for (int k = 2; k <= 5; k++) {
            ClusteringAnalysis analysis = new ClusteringAnalysis(survey, k, new KMeans());
            analysis.execute(responses, questions, distance);

            assertEquals(k, analysis.getClusters().size());
        }
    }

    @Test
    public void testMultipleDistanceTypes() {
        String username = "testUser9";
        userController.registerUser(username, "test9@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        List<DistanceType> distanceTypes = List.of(DistanceType.EUCLIDEAN, DistanceType.MANHATTAN);

        for (DistanceType distanceType : distanceTypes) {
            DistanceCalculator distance = new DistanceCalculator(distanceType, TextDistanceType.LEVENSHTEIN);
            ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
            analysis.execute(responses, questions, distance);

            assertEquals(2, analysis.getClusters().size());
        }
    }

    @Test
    public void testClusterQualityMetrics() {
        String username = "testUser10";
        userController.registerUser(username, "test10@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN, TextDistanceType.LEVENSHTEIN);

        analysis.execute(responses, questions, distance);

        assertEquals(2, analysis.getClusters().size());
        assertTrue(analysis.hasConverged());
        assertTrue(analysis.getExecutionTime() >= 0);
        assertNotNull(analysis.getIterations());
    }

    @Test
    public void testClusterMembership() {
        String username = "testUser11";
        userController.registerUser(username, "test11@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN, TextDistanceType.LEVENSHTEIN);

        analysis.execute(responses, questions, distance);

        for (Cluster cluster : analysis.getClusters()) {
            List<ClusterMembership> members = cluster.getMembers();
            assertNotNull(members);
            assertTrue(members.size() > 0);

            for (ClusterMembership member : members) {
                assertNotNull(member.getResponseId());
                assertTrue(member.getDistance() >= 0.0);
            }
        }
    }

    // ============= MÉTODOS AUXILIARES =============

    private void createTestSurvey() {
        survey = surveyController.initializeNewSurvey("Test Survey", "A survey for testing", userController.getUsernameLoggedIn());
        questions = new ArrayList<>();

        // Pregunta 1: Opción Múltiple
        MultipleChoiceQuestion q1 = new MultipleChoiceQuestion(0, survey.getSURVEY_ID());
        q1.setQuestionText("What is your favorite color?");
        q1.addOption(new OptionQuestion(0, "Red"));
        q1.addOption(new OptionQuestion(1, "Green"));
        q1.addOption(new OptionQuestion(2, "Blue"));
        questions.add(q1);

        // Pregunta 2: Textual
        Question q2 = new Question(1, survey.getSURVEY_ID());
        q2.setTypeQuestion(TypeQuestion.TEXTUAL);
        q2.setQuestionText("What is your favorite fruit?");
        questions.add(q2);

        // Pregunta 3: Numérica
        Question q3 = new Question(2, survey.getSURVEY_ID());
        q3.setTypeQuestion(TypeQuestion.NUMERICAL);
        q3.setQuestionText("What is your age?");
        questions.add(q3);

        survey.getQuestions().addAll(questions);
        survey = surveyController.createSurvey(survey);
    }

    private void generateTestResponses(int count) {
        Random random = new Random();
        String[] fruits = {"apple", "banana", "orange"};
        String surveyId = survey.getSURVEY_ID();
        String mainUsername = userController.getUsernameLoggedIn(); // GUARDAR usuario principal

        userController.logoutUser(); // Cerrar sesión del usuario principal antes de comenzar a responder

        for (int i = 0; i < count; i++) {
            String responderUsername = "responder" + i;
            userController.registerUser(responderUsername, responderUsername + "@gmail.com", "password", "What is your pet's name?", "Fluffy");
            userController.loginUser(responderUsername, "password");

            String responseId = responseController.startResponse(surveyId);

            // Respuesta para P1: Opción Múltiple
            String mcAnswer = String.valueOf(random.nextInt(3));
            responseController.updateAnswer(surveyId, responseId, 0, mcAnswer, TypeQuestion.MULTIPLE_CHOICE);

            // Respuesta para P2: Textual
            String textAnswer = fruits[i % fruits.length];
            responseController.updateAnswer(surveyId, responseId, 1, textAnswer, TypeQuestion.TEXTUAL);

            // Respuesta para P3: Numérica
            double age;
            int cluster = i % 3;
            if (cluster == 0) {
                age = 18 + random.nextDouble() * 5; // 18-23
            } else if (cluster == 1) {
                age = 35 + random.nextDouble() * 10; // 35-45
            } else {
                age = 60 + random.nextDouble() * 15; // 60-75
            }
            responseController.updateAnswer(surveyId, responseId, 2, age);

            userController.logoutUser();
        }
        // Iniciar sesión nuevamente con el usuario principal (usando el username guardado)
        userController.loginUser(mainUsername, "password"); // ← CORREGIDO
    }

    @Test
    public void testRunAnalysisMethod() {
        String username = "testUserCtrl";
        userController.registerUser(username, "testctrl@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        // Probar el método runAnalysis de CtrlDominioClustering
        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String analysisId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");

        assertNotNull(analysisId);
        assertNotNull(ctrlClustering.obtenerResultadosAnalisis(analysisId));
    }

    @Test
    public void testExportarAnalisis() throws IOException {
        String username = "testUserExport";
        userController.registerUser(username, "testexport@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String analysisId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");

        String filePath = "build/test_reports/clustering_export_test.txt";
        String savedPath = ctrlClustering.exportarAnalisis(analysisId, filePath);

        assertNotNull(savedPath);
        assertTrue(new File(savedPath).exists());
    }

    @Test
    public void testObtenerMetricasCalidad() {
        String username = "testUserMetrics";
        userController.registerUser(username, "testmetrics@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String analysisId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");

        String metrics = ctrlClustering.obtenerMetricasCalidad(analysisId);

        assertNotNull(metrics);
        assertTrue(metrics.contains("Coeficiente de Silhouette"));
        assertTrue(metrics.contains("Índice de Calinski-Harabasz"));
        assertTrue(metrics.contains("Índice de Davies-Bouldin"));
    }

    @Test
    public void testEncontrarKOptima() {
        String username = "testUserOptimalK";
        userController.registerUser(username, "testoptimalk@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(100); // Más respuestas para búsqueda de K óptima

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(100, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String report = ctrlClustering.encontrarKOptima(survey.getSURVEY_ID(), "KMeans", 100, 0.001, "EUCLIDEAN");

        assertNotNull(report);
        assertTrue(report.contains("--- Búsqueda de K Óptima ---"));
        assertTrue(report.contains("Recomendación"));
        assertTrue(report.contains("óptima recomendada"));
    }

    @Test
    public void testListarAnalisis() {
        String username = "testUserList";
        userController.registerUser(username, "testlist@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String id1 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");
        String id2 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 3, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");

        List<String> analysisList = ctrlClustering.listarAnalisis();

        assertNotNull(analysisList);
        assertTrue(analysisList.size() >= 2);
        assertTrue(analysisList.contains(id1));
        assertTrue(analysisList.contains(id2));
    }

    @Test
    public void testDeleteAnalysis() {
        String username = "testUserDelete";
        userController.registerUser(username, "testdelete@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String analysisId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");

        List<String> before = ctrlClustering.listarAnalisis();
        assertTrue(before.contains(analysisId));

        ctrlClustering.deleteAnalysis(analysisId);

        List<String> after = ctrlClustering.listarAnalisis();
        assertFalse(after.contains(analysisId));
    }

    @Test
    public void testObtenerResultadosAnalisis() {
        String username = "testUserResults";
        userController.registerUser(username, "testresults@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String analysisId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");

        String results = ctrlClustering.obtenerResultadosAnalisis(analysisId);

        assertNotNull(results);
        assertTrue(results.length() > 0);
    }

    @Test
    public void testMultipleAlgorithms() {
        String username = "testUserMultiAlgo";
        userController.registerUser(username, "testmultialgo@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Prueba con diferentes algoritmos
        String kmId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");
        String kmIdPlus = ctrlClustering.runAnalysis("KMeansPlusPlus", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");
        String kmedId = ctrlClustering.runAnalysis("KMedoids", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");

        assertNotNull(kmId);
        assertNotNull(kmIdPlus);
        assertNotNull(kmedId);
        assertNotEquals(kmId, kmIdPlus);
        assertNotEquals(kmIdPlus, kmedId);
    }

    @Test
    public void testMultipleDistanceMetrics() {
        String username = "testUserDistMetric";
        userController.registerUser(username, "testdistmetric@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Prueba con diferentes métricas de distancia
        String eucId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");
        String manhId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "MANHATTAN", "LEVENSHTEIN");

        assertNotNull(eucId);
        assertNotNull(manhId);
        assertNotEquals(eucId, manhId);
    }

    @Test
    public void testLimpiarAnalisis() {
        String username = "testUserClear";
        userController.registerUser(username, "testclear@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String id1 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");
        String id2 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 3, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");

        List<String> before = ctrlClustering.listarAnalisis();
        assertTrue(before.size() >= 2);

        ctrlClustering.limpiarAnalisis();

        List<String> after = ctrlClustering.listarAnalisis();
        assertEquals(0, after.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunAnalysisInvalidK() {
        String username = "testUserInvalidK";
        userController.registerUser(username, "testinvalidk@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(5); // Solo 5 respuestas

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Intenta con k > número de respuestas (debe lanzar excepción)
        ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 10, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunAnalysisInvalidSurveyId() {
        String username = "testUserInvalidSurvey";
        userController.registerUser(username, "testinvalidsurvey@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Intenta con survey ID inválido (debe lanzar excepción)
        ctrlClustering.runAnalysis("KMeans", "invalid-survey-id", 2, 100, 0.001, "EUCLIDEAN", "LEVENSHTEIN");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExportarAnalisisInvalidId() throws IOException {
        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Intenta exportar análisis que no existe (debe lanzar excepción)
        ctrlClustering.exportarAnalisis("invalid-analysis-id", "build/test.txt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testObtenerMetricasCalidadInvalidId() {
        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Intenta obtener métricas de análisis que no existe
        ctrlClustering.obtenerMetricasCalidad("invalid-analysis-id");
    }

    @Test
    public void testRunAnalysisWithDifferentIterations() {
        String username = "testUserIterations";
        userController.registerUser(username, "testiterations@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Prueba con diferentes números de iteraciones
        String id1 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 50, 0.001, "EUCLIDEAN", "LEVENSHTEIN");
        String id2 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 200, 0.001, "EUCLIDEAN", "LEVENSHTEIN");

        assertNotNull(id1);
        assertNotNull(id2);
    }

    @Test
    public void testRunAnalysisWithDifferentTolerance() {
        String username = "testUserTolerance";
        userController.registerUser(username, "testtolerance@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Prueba con diferentes tolerancias
        String id1 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.0001, "EUCLIDEAN", "LEVENSHTEIN");
        String id2 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.01, "EUCLIDEAN", "LEVENSHTEIN");

        assertNotNull(id1);
        assertNotNull(id2);
    }

}
