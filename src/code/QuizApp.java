import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;


public class QuizApp extends Application
{
    @Override
    public void start(final Stage stage)
    {
        final Label l;
        final Scene s;

        l = new Label("Hello there from JavaFX!!");
        s = new Scene(l, 300, 200);

        stage.setScene(s);
        stage.setTitle("welcome to GUI land");
        stage.show();
    }

    public static void main(final String[] args)
    {
        launch(args);
    }
}
