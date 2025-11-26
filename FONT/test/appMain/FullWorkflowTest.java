package appMain;

import data.*;
import domain.clustering.*;
import domain.controller.*;
import domain.model.*;
import domain.model.enums.TypeQuestion;
import domain.service.*;
import org.junit.Before;
import org.junit.Test;
import domain.clustering.TextDistanceType;
import data.ResponseRepository;
import data.UserRepository;
import data.SurveyRepository;
import data.QuestionRepository;
import data.AnswerRepository;
import domain.clustering.DistanceType;
import domain.clustering.QualityMetricType;
import domain.clustering.ClusteringAnalysis;
import domain.clustering.DistanceCalculator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FullWorkflowTest {

    private UserController userController;
    private SurveyController surveyController;
    private ResponseController responseController;
    private AnalysisController analysisController;
    private ResponseRepository responseRepository;

    private List<Question> questions;
    private Survey survey;
    private List<Response> responses;

    @Before
    public void setUp() {
        // Inicializar repositorios
        UserRepository userRepository = new UserRepository();
        SurveyRepository surveyRepository = new SurveyRepository();
        QuestionRepository questionRepository = new QuestionRepository();
        responseRepository = new ResponseRepository();
        AnswerRepository answerRepository = new AnswerRepository();

        // Inicializar servicios y controladores
        UserService userService = new UserService(userRepository);
        userController = new UserController(userService);

        SurveyService surveyService = new SurveyService(surveyRepository, userController);
        surveyController = new SurveyController(surveyService);
        
        ResponseService responseService = new ResponseService(responseRepository, userController, surveyService);
        responseController = new ResponseController(responseService);

        analysisController = new AnalysisController();

        // Crear directorio para reportes
        new File("build/clustering_reports").mkdirs();
    }

    @Test
    public void testFullWorkflow() {
        // Paso 1: Registrar e Iniciar Sesión
        String username = "testUser";
        userController.registerUser(username, "test@gmail.com", "password");
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

        // Paso 4: Determinar K Óptimo antes de ejecutar el análisis completo
        Integer optimalK = analysisController.determineOptimalK(responses, questions, survey, 2, 5);
        assertEquals("Optimal K should be 3 for this dataset", 3, (int) optimalK);
        System.out.println("\nDetermined Optimal K = " + optimalK);

        // Paso 5: Análisis de Clustering de Espectro Completo
        runAllClusteringAnalyses();
    }

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
        Random random = new Random(42); // Fixed seed for deterministic tests
        String[] fruits = {"apple", "banana", "orange"};
        String surveyId = survey.getSURVEY_ID();

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
        // Log back in the main user
        userController.loginUser("testUser", "password");
    }

    private void runAllClusteringAnalyses() {
        List<String> algorithms = analysisController.getAvailableAlgorithms();
        List<DistanceType> distanceTypes = List.of(DistanceType.EUCLIDEAN, DistanceType.MANHATTAN);
        List<QualityMetricType> qualityMetricTypes = List.of(QualityMetricType.SILHOUETTE, QualityMetricType.CALINSKI_HARABASZ, QualityMetricType.DAVIES_BOULDIN);
        int[] kValues = {2, 3, 4}; // Probar un rango de valores K

        System.out.println("\n--- Starting Full Clustering Analysis ---");

        for (String algorithmName : algorithms) {
            for (DistanceType distanceType : distanceTypes) {
                for (int k : kValues) {
                    System.out.println("\n=============================================");
                    System.out.printf("ALGORITHM: %s, DISTANCE: %s, K: %d\n", algorithmName, distanceType, k);
                    System.out.println("=============================================");

                    // Use AnalysisController to manage the analysis
                    ClusteringAnalysis analysis = analysisController.createAnalysis(survey, k, algorithmName, new HashMap<>());
                    DistanceCalculator distanceCalc = new DistanceCalculator(distanceType, TextDistanceType.LEVENSHTEIN);
                    
                    analysisController.executeAnalysis(analysis.getId(), responses, questions, distanceCalc);

                    ClusteringAnalysis completedAnalysis = analysisController.getAnalysis(analysis.getId());

                    assertTrue("Analysis should converge", completedAnalysis.hasConverged());
                    assertEquals("Should produce k clusters", k, completedAnalysis.getClusters().size());

                    // Calculate and print quality metrics
                    for (QualityMetricType qualityMetric : qualityMetricTypes) {
                        Double score = analysisController.calculateQuality(analysis.getId(), qualityMetric);
                        System.out.printf("- Quality Metric: %-20s | Score: %f\n", qualityMetric, score);
                        assertNotNull("Score should not be null", score);
                        assertTrue("Score should be a valid number", !score.isNaN() && !score.isInfinite());
                    }

                    // Export results
                    String filePath = String.format("build/clustering_reports/alg_%s_dist_%s_k_%d.txt", algorithmName, distanceType, k);
                    try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
                        writer.write(completedAnalysis.exportResults());
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                    File file = new File(filePath);
                    assertTrue("Export file should be created", file.exists());
                    assertTrue("Export file should not be empty", file.length() > 0);
                }
            }
        }
        System.out.println("\n--- Full Clustering Analysis Finished ---\n");
    }
}
