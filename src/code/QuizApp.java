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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Demonstrates quiz app.
 *
 * @author clinton nguyen
 * @author kyle cheon
 * @version 1.0
 */
public class QuizApp extends Application
{

    private static class Question
    {
        final String questionText;
        final String answer;

        Question(final String questionText, final String answer)
        {
            this.questionText = questionText;
            this.answer = answer;
        }
    }

    private static final int NUMBER_OF_QUESTIONS = 10;
    private static final int ZERO = 0;
    private static final int WIDTH_OF_THE_SCENE = 400;
    private static final int HEIGHT_OF_THE_SCENE = 600;
    private static final String QUIZ_FILE_NAME = "/quiz.txt";

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
    private Label missedSummaryLabel;
    private VBox mainLayout;

    /**
     * This method loads GUI for JavaFX
     * @param stage is the stage used to draw scenes
     */
    @Override
    public void start(final Stage stage)
    {
        questionLabel = new Label("Press 'Start Quiz' to begin!");
        timerLabel = new Label("Time: ");
        answerField = new TextField();
        answerField.setPromptText("Enter your answer here");
        submitButton = new Button("Submit");
        startQuizButton = new Button("Start Quiz");
        scoreLabel = new Label("Score: 0");
        missedSummaryLabel = new Label();

        // Event handling for button click and ENTER key
        submitButton.setOnAction(e -> submitAnswer());
        answerField.setOnAction(e -> submitAnswer()); // Handles ENTER key
        startQuizButton.setOnAction(e -> startQuiz());

        // Arrange components in a VBox
        mainLayout = new VBox(10);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(
                questionLabel,
                timerLabel,
                answerField,
                submitButton,
                scoreLabel,
                startQuizButton,
                missedSummaryLabel
        );

        final Scene scene = new Scene(mainLayout, WIDTH_OF_THE_SCENE, HEIGHT_OF_THE_SCENE);

        stage.setScene(scene);
        stage.setTitle("Quiz App");
        stage.show();

        // Initially disable answer input until the quiz starts
        answerField.setDisable(true);
        submitButton.setDisable(true);
    }

    /*
     * this app starts the quiz.
     */
    private void startQuiz()
    {
        loadQuestions();
        score = ZERO;
        currentQuestionIndex = ZERO;
        missedQuestions = new ArrayList<>();
        scoreLabel.setText("Score: " + score);
        missedSummaryLabel.setText("");

        // Shuffle the questions and choose 10 random ones (or all if less than 10)
        Collections.shuffle(allQuestions);
        if(allQuestions.size() > NUMBER_OF_QUESTIONS)
        {
            quizQuestions = allQuestions.subList(ZERO, NUMBER_OF_QUESTIONS);
        }
        else
        {
            quizQuestions = new ArrayList<>(allQuestions);
        }

        // Prepare the UI for the quiz
        startQuizButton.setDisable(true);
        answerField.setDisable(false);
        submitButton.setDisable(false);

        // Show the first question
        showNextQuestion();
    }

    /*
     * this method loads the chosen quiz questions.
     */
    // Load all questions from quiz.txt
    private void loadQuestions()
    {
        allQuestions = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(
                        getClass().getResourceAsStream(QUIZ_FILE_NAME)))))
        {
            String line;
            while((line = br.readLine()) != null)
            {
                if(line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if(parts.length >= 2)
                {
                    allQuestions.add(new Question(parts[0].trim(), parts[1].trim()));
                }
            }
        }
        catch(final IOException e)
        {
            e.printStackTrace();
            questionLabel.setText("Error loading quiz file!");
        }
    }

    /*
     * this method displays next UI for next quiz question
     */
    // Display the next question or end the quiz if all questions have been answered
    private void showNextQuestion()
    {
        if(timer != null)
        {
            timer.stop();
        }
        if(currentQuestionIndex >= quizQuestions.size())
        {
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

    /*
     * this method starts timer.
     */
    // Start a countdown timer for the current question
    private void startTimer()
    {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event ->
        {
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft);
            if(timeLeft <= ZERO)
            {
                timer.stop();
                // Record the question as missed and move on
                missedQuestions.add(quizQuestions.get(currentQuestionIndex));
                currentQuestionIndex++;
                showNextQuestion();
            }
        }));
        timer.setCycleCount(QUESTION_TIME);
        timer.play();
    }

    /*
     * this method stores and submits user's answer
     */
    // Check the submitted answer and update the score accordingly
    private void submitAnswer()
    {
        if(timer != null)
        {
            timer.stop();
        }
        Question currentQuestion = quizQuestions.get(currentQuestionIndex);
        String userAnswer = answerField.getText().trim();

        if(userAnswer.equalsIgnoreCase(currentQuestion.answer))
        {
            score++;
        }
        else
        {
            missedQuestions.add(currentQuestion);
        }
        scoreLabel.setText("Score: " + score);
        currentQuestionIndex++;
        showNextQuestion();
    }

    /*
     * this method closes the app once time is over.
     */
    // end the quiz, show the final score and summary of missed questions, then allow a restart
    private void endQuiz()
    {
        // disable input
        answerField.setDisable(true);
        submitButton.setDisable(true);

        // build a summary
        StringBuilder summary = new StringBuilder();
        summary.append("Quiz Over! Final Score: ").append(score).append("\n");

        if(!missedQuestions.isEmpty())
        {
            summary.append("\nMissed Questions:\n");
            for(Question q : missedQuestions)
            {
                summary.append("• ")
                        .append(q.questionText)
                        .append(" (Answer: ").append(q.answer).append(")\n");
            }
        }

        // display the summary
        missedSummaryLabel.setText(summary.toString());

        // update the main question label and timer label
        questionLabel.setText("Quiz Over!");
        timerLabel.setText("");

        // allow another quiz start
        startQuizButton.setDisable(false);
    }

    /**
     * drives the GUI for this quiz app.
     * @param args arguments from command line.
     */
    public static void main(final String[] args)
    {
        launch(args);
    }
}