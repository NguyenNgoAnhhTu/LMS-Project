package com.example.quiz.AI;

import com.example.quiz.AI.request.AIRequest;
import com.example.quiz.AI.response.AIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Random;

@Service
public class AIService {
    @Value("${ai.api}")
    private String API;

//    @Value("ai.token")
//    private String TOKEN;

    private final String TOKEN = "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";

    @Value("${ai.username}")
    private String USERNAME;

    @Value("${ai.password}")
    private String PASSWORD;

    @Autowired
    private RestTemplate restTemplate;

    public AIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private final String MODEL = "google/gemini-2.0-pro-exp-02-05:free";
//    private final boolean STREAM = false;

    // generate question type MCQ (Multiple Choice Question)
    private String generatePromptForMCQ(int numOfQuestions, int numOfAnswerOptions,
                                        int numOfCorrectAnswer, String questionDescription) {
        return "generate " + numOfQuestions + " questions have " + numOfAnswerOptions + " answer options and " + numOfCorrectAnswer + " of them is the correct answer" +
                "\ntopic is " + questionDescription +
                "\njust output follow json form and do not change value of questionType:" +
                "\n{" +
                "\n // accept value: number" +
                "\n \"total_questions\": " + numOfQuestions + "," +
                "\n \"questions\": [" +
                "\n     {" +
                "\n         \"questionText\":\"\"," +
                "\n         \"questionType\":\"MCQ\"," +
                "\n         \"answerOptions\": [" +
                "\n             {" +
                "\n                 \"optionText\":\"\"," +
                "\n                 // accept value: true, false" +
                "\n                 \"isCorrect\":" +
                "\n             }" +
                "\n         ]" +
                "\n     }" +
                "\n ]" +
                "\n}";
    }

    // generate question type SCQ (Single Choice Question)
    private String generatePromptForSCQ(int numOfQuestions, int numOfAnswerOptions, String questionDescription) {
        return "generate " + numOfQuestions + " questions have " + numOfAnswerOptions + " answer options and 1 of them is the correct answer" +
                "\ntopic is " + questionDescription +
                "\njust output follow json form and do not change value of questionType:" +
                "\n{" +
                "\n // accept value: number" +
                "\n \"total_questions\": " + numOfQuestions + "," +
                "\n \"questions\": [" +
                "\n     {" +
                "\n         \"questionText\":\"\"," +
                "\n         \"questionType\":\"SCQ\"," +
                "\n         \"answerOptions\": [" +
                "\n             {" +
                "\n                 \"optionText\":\"\"," +
                "\n                 // accept value: true, false" +
                "\n                 \"isCorrect\":" +
                "\n             }" +
                "\n         ]" +
                "\n     }" +
                "\n ]" +
                "\n}";
    }

    // generate question type TFQ (True / False Question)
    private String generatePromptForTFQ(int numOfQuestions, String questionDescription) {
        return "generate " + numOfQuestions + " questions have 2 answer options is True/False" +
                "\ntopic is " + questionDescription +
                "\njust output follow json form and do not change value of questionType:" +
                "\n{" +
                "\n // accept value: number" +
                "\n \"total_questions\": " + numOfQuestions + "," +
                "\n \"questions\": [" +
                "\n     {" +
                "\n         \"questionText\":\"\"," +
                "\n         \"questionType\":\"SCQ\"," +
                "\n         \"answerOptions\": [" +
                "\n             {" +
                "\n                 \"optionText\":\"True\"," +
                "\n                 // accept value: true, false" +
                "\n                 \"isCorrect\":" +
                "\n             }," +
                "\n             {" +
                "\n                 \"optionText\":\"False\"," +
                "\n                 // accept value: true, false" +
                "\n                 \"isCorrect\":" +
                "\n             }" +
                "\n         ]" +
                "\n     }" +
                "\n  ]" +
                "\n}";
    }

    // generate AIRequest
//    private AIRequest generateRequestBody(String model, String prompt, boolean stream) {
//        return new AIRequest(model, prompt, stream);
//    }
//
//    private AIRequest generateRequestBody(String prompt, boolean stream) {
//        return new AIRequest(MODEL, prompt, stream);
//    }
//
//    private AIRequest generateRequestBody(String prompt) {
//        return new AIRequest(MODEL, prompt, STREAM);
//    }

    private AIRequest generateRequestBody(String content){
        return new AIRequest(MODEL, content);
    }

    // create HttpEntity
    private HttpEntity<AIRequest> getHttpEntity(AIRequest aiRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Basic " + encodeCredentials(USERNAME, PASSWORD));
//        headers.set("Authorization", "Bearer " + TOKEN);
        headers.setBearerAuth(TOKEN);

        return new HttpEntity<>(aiRequest, headers);
    }

    // get Response from AI
    public String getResponseAIGenerate(AIRequest aiRequest) {
        HttpEntity<AIRequest> request = getHttpEntity(aiRequest);

        try {
            ResponseEntity<AIResponse> response = restTemplate.exchange(API, HttpMethod.POST, request, AIResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().getChoices().getLast().getMessage().getContent();
            }
        } catch (Exception e) {
            return "Failed to connect to AI service";
        }

        return "No response from AI";
    }

    // type, numOfQuestion, numOfAnswerOptions, questionDescription
    public AIResponse getResponseAIGenerate(String type, int numOfQuestions, int numOfAnswerOptions,
                                            String questionDescription) {

        String prompt = switch (type) {
            case "Single Choice Question" -> generatePromptForSCQ(numOfQuestions, numOfAnswerOptions, questionDescription);
            case "True/False Question" -> generatePromptForTFQ(numOfQuestions, questionDescription);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };

        AIRequest aiRequest = generateRequestBody(prompt);

        HttpEntity<AIRequest> request = getHttpEntity(aiRequest);

        try {
            ResponseEntity<AIResponse> response = restTemplate.exchange(API, HttpMethod.POST, request, AIResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            return new AIResponse("No response from AI service");
        }

        return new AIResponse("No response from AI");
    }

    // authentication
    private String encodeCredentials(String username, String password) {
        String credentials = username + ":" + password;
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }

//    public String[] getModelNames() {
//        return new String[]{"deepseek-r1:latest", "llama3.1:latest", "qwen2.5-coder:32b"};
//    }

    public String AIWarning() {
        String[] warnings = {"This AI is stupid, please don't hope too much",
                "You may have to edit a lot after creating, think carefully before using",
                "This is not the main function and does not replace the main function",
                "You will have to wait a long time, use it if you have enough time and patience"};

        Random random = new Random(System.currentTimeMillis());
        return warnings[random.nextInt(warnings.length)];
    }

    public int[] getNumOfAnswerOptions() {
        return new int[]{2, 3, 4, 5, 6};
    }

//    public int[] getNumOfCorrectAnswer() {
//        return new int[]{2, 3, 4};
//    }

    public String[] getTypes(){
        return new String[]{"Single Choice Question", "True/False Question"};
    }
}
