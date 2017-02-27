import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Denver Quane
 * ID#101611184
 * CS-351
 * last Rev. 2/15/17
 * This Main class launches the application, and specifically launches the opening launcher found in
 * Controller.java
 */

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception
  {

    FXMLLoader launchLoader = new FXMLLoader(getClass().getResource("launcher.fxml"));

    Parent root = (Parent)launchLoader.load();

    synchronized (primaryStage)
    {
      primaryStage.setTitle("Conway's Game of Life - Launcher");
      primaryStage.setScene(new Scene(root, 425, 250));
      primaryStage.show();
    }
  }

  public static void main(String[] args)
  {
    launch(args);
  }
}
