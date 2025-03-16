import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizApp extends Application {

    // Inner class to store a question and its answer
    private static class Question {
        String questionText;
        String answer;

        Question(String questionText, String answer) {
            this.questionText = questionText;
            this.answer = answer;
        }
    }

    // Data structures for questions and quiz state
    private List<Question> allQuestions;
    private List<Question> quizQuestions;
    private int currentQuestionIndex;
    private int score;
    private List<Question> missedQuestions;

    // Timer variables
    private Timeline timer;
    private int timeLeft;
    private final int QUESTION_TIME = 15;

    // UI Components
    private Label questionLabel;
    private Label timerLabel;
    private TextField answerField;
    private Button submitButton;
    private Button startQuizButton;
    private Label scoreLabel;
    private VBox mainLayout;

    @Override
    public void start(Stage stage) {
        // Initialize UI components
        questionLabel = new Label("Press 'Start Quiz' to begin!");
        timerLabel = new Label("Time: ");
        answerField = new TextField();
        answerField.setPromptText("Enter your answer here");
        submitButton = new Button("Submit");
        startQuizButton = new Button("Start Quiz");
        scoreLabel = new Label("Score: 0");

        // Event handling for button click and ENTER key
        submitButton.setOnAction(e -> submitAnswer());
        answerField.setOnAction(e -> submitAnswer()); // Handles ENTER key
        startQuizButton.setOnAction(e -> startQuiz());

        // Arrange components in a VBox
        mainLayout = new VBox(10);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(questionLabel, timerLabel, answerField, submitButton, scoreLabel, startQuizButton);

        Scene scene = new Scene(mainLayout, 400, 300);
        // Add external stylesheet if available (styles.css should be in your project resources)
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Quiz App");
        stage.show();

        // Initially disable answer input until the quiz starts
        answerField.setDisable(true);
        submitButton.setDisable(true);
    }

    // Called when the user clicks "Start Quiz"
    private void startQuiz() {
        loadQuestions();
        score = 0;
        currentQuestionIndex = 0;
        missedQuestions = new ArrayList<>();
        scoreLabel.setText("Score: " + score);

        // Shuffle the questions and choose 10 random ones (or all if less than 10)
        Collections.shuffle(allQuestions);
        if (allQuestions.size() > 10) {
            quizQuestions = allQuestions.subList(0, 10);
        } else {
            quizQuestions = new ArrayList<>(allQuestions);
        }

        // Prepare the UI for the quiz
        startQuizButton.setDisable(true);
        answerField.setDisable(false);
        submitButton.setDisable(false);

        // Show the first question
        showNextQuestion();
    }

    // Load all questions from quiz.txt; each line must be in the format: question|answer
    private void loadQuestions() {
        allQuestions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/quiz.txt"))))
        {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    allQuestions.add(new Question(parts[0].trim(), parts[1].trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            questionLabel.setText("Error loading quiz file!");
        }
    }

    // Display the next question or end the quiz if all questions have been answered
    private void showNextQuestion() {
        if (timer != null) {
            timer.stop();
        }
        if (currentQuestionIndex >= quizQuestions.size()) {
            endQuiz();
            return;
        }
        Question currentQuestion = quizQuestions.get(currentQuestionIndex);
        questionLabel.setText("Question " + (currentQuestionIndex + 1) + ": " + currentQuestion.questionText);
        answerField.clear();
        timeLeft = QUESTION_TIME;
        timerLabel.setText("Time: " + timeLeft);
        startTimer();
    }

    // Start a countdown timer for the current question
    private void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft);
            if (timeLeft <= 0) {
                timer.stop();
                // Time's up: record the question as missed and move on
                missedQuestions.add(quizQuestions.get(currentQuestionIndex));
                currentQuestionIndex++;
                showNextQuestion();
            }
        }));
        timer.setCycleCount(QUESTION_TIME);
        timer.play();
    }

    // Check the submitted answer and update the score accordingly
    private void submitAnswer() {
        if (timer != null) {
            timer.stop();
        }
        Question currentQuestion = quizQuestions.get(currentQuestionIndex);
        String userAnswer = answerField.getText().trim();

        if (userAnswer.equalsIgnoreCase(currentQuestion.answer)) {
            score++;
        } else {
            // Record the question for summary if answered incorrectly
            missedQuestions.add(currentQuestion);
        }
        scoreLabel.setText("Score: " + score);
        currentQuestionIndex++;
        showNextQuestion();
    }

    // End the quiz, show the final score and summary of missed questions, then allow a restart
    private void endQuiz() {
        answerField.setDisable(true);
        submitButton.setDisable(true);
        StringBuilder summary = new StringBuilder();
        summary.append("Quiz Over! Final Score: ").append(score).append("\n");
        if (!missedQuestions.isEmpty()) {
            summary.append("Missed Questions:\n");
            for (Question q : missedQuestions) {
                summary.append(q.questionText)
                        .append(" (Answer: ").append(q.answer).append(")\n");
            }
        }
        questionLabel.setText(summary.toString());
        timerLabel.setText("");
        startQuizButton.setDisable(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}