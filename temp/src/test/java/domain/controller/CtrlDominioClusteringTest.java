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
        // Initialize repositories
        UserRepository userRepository = new UserRepository();
        surveyRepository = new SurveyRepository();
        QuestionRepository questionRepository = new QuestionRepository();
        responseRepository = new ResponseRepository();
        AnswerRepository answerRepository = new AnswerRepository();

        // Initialize services and controllers
        UserService userService = new UserService(userRepository);
        userController = new UserController(userService);

        SurveyService surveyService = new SurveyService(surveyRepository, userController);
        surveyController = new SurveyController(surveyService);

        ResponseService responseService = new ResponseService(responseRepository, userController, surveyService);
        responseController = new ResponseController(responseService);

        analysisController = new AnalysisController();
    }

    @Test
    public void testCreateAnalysisWithKMeans() {
        // Step 1: Register and Login
        String username = "testUser";
        userController.registerUser(username, "test@gmail.com", "password");
        userController.loginUser(username, "password");

        // Step 2: Create Survey with Mixed Questions
        createTestSurvey();
        assertNotNull(survey);
        assertEquals(3, questions.size());

        // Step 3: Generate Diverse Responses
        generateTestResponses(50);

        // Fetch responses from repository for analysis
        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        // Step 4: Create KMeans Analysis
        int k = 2;
        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, k, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);

        analysis.execute(responses, questions, distance);

        assertNotNull(analysis);
        assertEquals(k, analysis.getClusters().size());
        assertTrue(analysis.hasConverged());
    }

    @Test
    public void testExecuteAnalysisWithKMedoids() {
        String username = "testUser2";
        userController.registerUser(username, "test2@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        int k = 3;
        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, k, new KMedoids());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);

        analysis.execute(responses, questions, distance);

        assertEquals(k, analysis.getClusters().size());
        assertTrue(analysis.getIterations() >= 0);
        assertNotNull(analysis.getExecutionTime());
    }

    @Test
    public void testClusterDistribution() {
        String username = "testUser3";
        userController.registerUser(username, "test3@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(60);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(60, responses.size());

        int k = 3;
        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, k, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);

        analysis.execute(responses, questions, distance);

        List<Cluster> clusters = analysis.getClusters();
        int totalMembers = clusters.stream().mapToInt(Cluster::getSize).sum();
        assertEquals(60, totalMembers);
        assertEquals(3, clusters.size());
    }

    @Test
    public void testCentroidCalculation() {
        String username = "testUser4";
        userController.registerUser(username, "test4@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);

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
        userController.registerUser(username, "test5@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);

        analysis.execute(responses, questions, distance);

        Long executionTime = analysis.getExecutionTime();
        assertNotNull(executionTime);
        assertTrue(executionTime >= 0);
    }

    @Test
    public void testExportAnalysisResults() {
        String username = "testUser6";
        userController.registerUser(username, "test6@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);

        analysis.execute(responses, questions, distance);

        String exportedResults = analysis.exportResults();
        assertNotNull(exportedResults);
        assertTrue(exportedResults.length() > 0);
    }

    @Test
    public void testAnalysisConvergence() {
        String username = "testUser7";
        userController.registerUser(username, "test7@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);

        analysis.execute(responses, questions, distance);

        Boolean converged = analysis.hasConverged();
        assertNotNull(converged);
        assertTrue(converged);
    }

    @Test
    public void testDifferentKValues() {
        String username = "testUser8";
        userController.registerUser(username, "test8@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(100);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(100, responses.size());

        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);

        for (int k = 2; k <= 5; k++) {
            ClusteringAnalysis analysis = new ClusteringAnalysis(survey, k, new KMeans());
            analysis.execute(responses, questions, distance);

            assertEquals(k, analysis.getClusters().size());
        }
    }

    @Test
    public void testMultipleDistanceTypes() {
        String username = "testUser9";
        userController.registerUser(username, "test9@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        List<DistanceType> distanceTypes = List.of(DistanceType.EUCLIDEAN, DistanceType.MANHATTAN);

        for (DistanceType distanceType : distanceTypes) {
            DistanceCalculator distance = new DistanceCalculator(distanceType);
            ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
            analysis.execute(responses, questions, distance);

            assertEquals(2, analysis.getClusters().size());
        }
    }

    @Test
    public void testClusterQualityMetrics() {
        String username = "testUser10";
        userController.registerUser(username, "test10@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);

        analysis.execute(responses, questions, distance);

        assertEquals(2, analysis.getClusters().size());
        assertTrue(analysis.hasConverged());
        assertTrue(analysis.getExecutionTime() >= 0);
        assertNotNull(analysis.getIterations());
    }

    @Test
    public void testClusterMembership() {
        String username = "testUser11";
        userController.registerUser(username, "test11@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 2, new KMeans());
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);

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

    // ============= HELPER METHODS =============

    private void createTestSurvey() {
        survey = surveyController.initializeNewSurvey("Test Survey", "A survey for testing", userController.getUsernameLoggedIn());
        questions = new ArrayList<>();

        // Question 1: Multiple Choice
        MultipleChoiceQuestion q1 = new MultipleChoiceQuestion(0, survey.getSURVEY_ID());
        q1.setQuestionText("What is your favorite color?");
        q1.addOption(new OptionQuestion(0, "Red"));
        q1.addOption(new OptionQuestion(1, "Green"));
        q1.addOption(new OptionQuestion(2, "Blue"));
        questions.add(q1);

        // Question 2: Textual
        Question q2 = new Question(1, survey.getSURVEY_ID());
        q2.setTypeQuestion(TypeQuestion.TEXTUAL);
        q2.setQuestionText("What is your favorite fruit?");
        questions.add(q2);

        // Question 3: Numerical
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

        userController.logoutUser(); // Log out main user before starting to respond

        for (int i = 0; i < count; i++) {
            String responderUsername = "responder" + i;
            userController.registerUser(responderUsername, responderUsername + "@gmail.com", "password");
            userController.loginUser(responderUsername, "password");

            String responseId = responseController.startResponse(surveyId);

            // Answer for Q1: Multiple Choice
            String mcAnswer = String.valueOf(random.nextInt(3));
            responseController.updateAnswer(surveyId, responseId, 0, mcAnswer, TypeQuestion.MULTIPLE_CHOICE);

            // Answer for Q2: Textual
            String textAnswer = fruits[i % fruits.length];
            responseController.updateAnswer(surveyId, responseId, 1, textAnswer, TypeQuestion.TEXTUAL);

            // Answer for Q3: Numerical
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
        // Log back in the main user (usando el username guardado)
        userController.loginUser(mainUsername, "password"); // ← CORREGIDO
    }

    // AÑADE ESTO AL FINAL DE CtrlDominioClusteringTest.java (después del método generateTestResponses)

    @Test
    public void testRunAnalysisMethod() {
        String username = "testUserCtrl";
        userController.registerUser(username, "testctrl@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        // Probar el método runAnalysis de CtrlDominioClustering
        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String analysisId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN");

        assertNotNull(analysisId);
        assertNotNull(ctrlClustering.obtenerResultadosAnalisis(analysisId));
    }

    @Test
    public void testExportarAnalisis() throws IOException {
        String username = "testUserExport";
        userController.registerUser(username, "testexport@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String analysisId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN");

        String filePath = "build/test_reports/clustering_export_test.txt";
        String savedPath = ctrlClustering.exportarAnalisis(analysisId, filePath);

        assertNotNull(savedPath);
        assertTrue(new File(savedPath).exists());
    }

    @Test
    public void testObtenerMetricasCalidad() {
        String username = "testUserMetrics";
        userController.registerUser(username, "testmetrics@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String analysisId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN");

        String metrics = ctrlClustering.obtenerMetricasCalidad(analysisId);

        assertNotNull(metrics);
        assertTrue(metrics.contains("Coeficiente de Silhouette"));
        assertTrue(metrics.contains("Índice de Calinski-Harabasz"));
        assertTrue(metrics.contains("Índice de Davies-Bouldin"));
    }

    @Test
    public void testEncontrarKOptima() {
        String username = "testUserOptimalK";
        userController.registerUser(username, "testoptimalk@gmail.com", "password");
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
        userController.registerUser(username, "testlist@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String id1 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN");
        String id2 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 3, 100, 0.001, "EUCLIDEAN");

        List<String> analysisList = ctrlClustering.listarAnalisis();

        assertNotNull(analysisList);
        assertTrue(analysisList.size() >= 2);
        assertTrue(analysisList.contains(id1));
        assertTrue(analysisList.contains(id2));
    }

    @Test
    public void testDeleteAnalysis() {
        String username = "testUserDelete";
        userController.registerUser(username, "testdelete@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String analysisId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN");

        List<String> before = ctrlClustering.listarAnalisis();
        assertTrue(before.contains(analysisId));

        ctrlClustering.deleteAnalysis(analysisId);

        List<String> after = ctrlClustering.listarAnalisis();
        assertFalse(after.contains(analysisId));
    }

    @Test
    public void testObtenerResultadosAnalisis() {
        String username = "testUserResults";
        userController.registerUser(username, "testresults@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String analysisId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN");

        String results = ctrlClustering.obtenerResultadosAnalisis(analysisId);

        assertNotNull(results);
        assertTrue(results.length() > 0);
    }

    @Test
    public void testMultipleAlgorithms() {
        String username = "testUserMultiAlgo";
        userController.registerUser(username, "testmultialgo@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Prueba con diferentes algoritmos
        String kmId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN");
        String kmIdPlus = ctrlClustering.runAnalysis("KMeansPlusPlus", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN");
        String kmedId = ctrlClustering.runAnalysis("KMedoids", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN");

        assertNotNull(kmId);
        assertNotNull(kmIdPlus);
        assertNotNull(kmedId);
        assertNotEquals(kmId, kmIdPlus);
        assertNotEquals(kmIdPlus, kmedId);
    }

    @Test
    public void testMultipleDistanceMetrics() {
        String username = "testUserDistMetric";
        userController.registerUser(username, "testdistmetric@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Prueba con diferentes métricas de distancia
        String eucId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN");
        String manhId = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "MANHATTAN");

        assertNotNull(eucId);
        assertNotNull(manhId);
        assertNotEquals(eucId, manhId);
    }

    @Test
    public void testLimpiarAnalisis() {
        String username = "testUserClear";
        userController.registerUser(username, "testclear@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertEquals(50, responses.size());

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        String id1 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.001, "EUCLIDEAN");
        String id2 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 3, 100, 0.001, "EUCLIDEAN");

        List<String> before = ctrlClustering.listarAnalisis();
        assertTrue(before.size() >= 2);

        ctrlClustering.limpiarAnalisis();

        List<String> after = ctrlClustering.listarAnalisis();
        assertEquals(0, after.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunAnalysisInvalidK() {
        String username = "testUserInvalidK";
        userController.registerUser(username, "testinvalidk@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(5); // Solo 5 respuestas

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Intenta con k > número de respuestas (debe lanzar excepción)
        ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 10, 100, 0.001, "EUCLIDEAN");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunAnalysisInvalidSurveyId() {
        String username = "testUserInvalidSurvey";
        userController.registerUser(username, "testinvalidsurvey@gmail.com", "password");
        userController.loginUser(username, "password");

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Intenta con survey ID inválido (debe lanzar excepción)
        ctrlClustering.runAnalysis("KMeans", "invalid-survey-id", 2, 100, 0.001, "EUCLIDEAN");
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
        userController.registerUser(username, "testiterations@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Prueba con diferentes números de iteraciones
        String id1 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 50, 0.001, "EUCLIDEAN");
        String id2 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 200, 0.001, "EUCLIDEAN");

        assertNotNull(id1);
        assertNotNull(id2);
    }

    @Test
    public void testRunAnalysisWithDifferentTolerance() {
        String username = "testUserTolerance";
        userController.registerUser(username, "testtolerance@gmail.com", "password");
        userController.loginUser(username, "password");

        createTestSurvey();
        generateTestResponses(50);

        CtrlDominioClustering ctrlClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Prueba con diferentes tolerancias
        String id1 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.0001, "EUCLIDEAN");
        String id2 = ctrlClustering.runAnalysis("KMeans", survey.getSURVEY_ID(), 2, 100, 0.01, "EUCLIDEAN");

        assertNotNull(id1);
        assertNotNull(id2);
    }

}
