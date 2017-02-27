import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Denver Quane
 * ID#101611184
 * CS-351
 * last Rev. 2/15/17
 * This class handles the GUI for the opening launcher for the program
 */
public class Controller implements Initializable
{
  @FXML
  private Button confirmButton;

  @FXML
  private Slider sizeSlider;

  @FXML
  private Label currentSizeLabel;

  @FXML
  private Slider threadSlider;

  private String curValueString;

  private static final String curValue = "Grid Dimensions: ";

  @FXML
  private void switchScenes(ActionEvent e)
  {
    Stage stage;
    Parent root;

    if(e.getSource() == confirmButton)
    {
      stage = (Stage)confirmButton.getScene().getWindow();
      int threads = threadSlider.valueProperty().intValue();
      int dimensions = sizeSlider.valueProperty().intValue();

      try
      {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        root = (Parent)loader.load();
        AppController simulationController = loader.<AppController>getController();
        simulationController.setGridDimensionsAndInit(dimensions);
        simulationController.setAndStartGameThreads(threads);

        Scene scene = new Scene(root);
        stage.setTitle("Conway's Game Of Life - Simulating " + dimensions + " by " + dimensions + " Cells on " + threads + " Threads");

        stage.setScene(scene);
        stage.show();

      }
      catch (IOException exc)
      {
        exc.printStackTrace();
      }
    }
  }

  /**
   * Initilaizes the basic UI elements for the opening launcher GUI, which is responsible for loading the new
   * "AppController" scene
   * @param location
   * @param resources
   * @see AppController
   */
  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    assert confirmButton != null : "fx:id=\"myButton\" was not injected: check your FXML file 'simple.fxml'.";


    sizeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
    {
      curValueString = NumberFormat.getNumberInstance(Locale.US).format(newValue.intValue());

      currentSizeLabel.setText(curValue + curValueString + " x " + curValueString + " = "
              + NumberFormat.getNumberInstance(Locale.US).format(newValue.intValue() * newValue.intValue())
              + " Total Cells");
    });

    int value = (int)sizeSlider.getValue();
    curValueString = NumberFormat.getNumberInstance(Locale.US).format(value);
    currentSizeLabel.setText(curValue + curValueString + " x " + curValueString + " = "
     + NumberFormat.getNumberInstance(Locale.US).format(value * value) + " Total Cells");



  }
}
