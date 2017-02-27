import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Denver Quane
 * ID#101611184
 * CS-351
 * last Rev. 2/15/17
 * This class handles the GUI for the running simulation
 */

public class AppController implements Initializable
{
  @FXML
  private VBox masterBox;
  @FXML
  private BorderPane bP;

  @FXML
  private Canvas gameCanvas;

  @FXML
  private Button patternButton1;
  @FXML
  private Button patternButton2;
  @FXML
  private Button patternButton3;
  @FXML
  private Button patternButton4;
  @FXML
  private Button patternButton5;
  @FXML
  private Button patternButton6;
  @FXML
  private Button patternButton7;


  @FXML
  private Button checkerBoardButton;
  @FXML
  private Button gosperButton;
  @FXML
  private Button valentineButton;
  @FXML
  private Button emptyButton;
  @FXML
  private Button emptyButton2;
  @FXML
  private Button resetCamera;
  @FXML
  private Button randomButton;
  @FXML
  private Button almostFullButton;
  @FXML
  private ToggleButton pauseButton;
  @FXML
  private Slider cameraSpeedSlider;
  @FXML
  private Button flagButton;
  @FXML
  private Button clearFlagButton;
  @FXML
  private Button closeButton;
  @FXML
  private Label updatesValue;
  @FXML
  private TextField patternFileNameTextField;
  @FXML
  private Button savePatternButton;
  @FXML
  private Button loadPatternByNameButton;
  @FXML
  private Button simulateFrameButton;
  @FXML
  private TextField loadPatternFilenameField;
  @FXML
  private TextField cellRowField;
  @FXML
  private TextField cellColField;
  @FXML
  private TextField xFlagField;
  @FXML
  private TextField yFlagField;
  @FXML
  private Label cellSizeLabel;
  @FXML
  private AnchorPane canvasContainerAnchor;

  @FXML
  private Label framesValue;

  private boolean cameraMode = true;

  private SimpleIntegerProperty framesProperty = new SimpleIntegerProperty();
  private int tempUpdatesValue;


  private Grid gameGrid = null;
  private AnimationTimer drawLoop;

  /**
   * Called by the close program button to close the entire program (stopping all threads)
   * @param e the actionevent triggered by the button press
   */
  @FXML
  private void closeProgram(ActionEvent e)
  {
    if(e.getSource() == closeButton)
    {
      drawLoop.stop();
      gameGrid.killThreads();
      Platform.exit();
      System.exit(0);
    }
  }


  /**
   *  Called by the buttons that trigger preset board configurations to load
   * @param e
   */
  @FXML
  private void changeToPreset(ActionEvent e)
  {
    if(e.getSource() == randomButton)
    {
      gameGrid.setRandomPreset();
    }
    else if(e.getSource() == almostFullButton)
    {
      gameGrid.setAlmostFullPreset();
    }
    else if (e.getSource() == gosperButton)
    {
      gameGrid.setGunPreset();
    }
    else if(e.getSource() == checkerBoardButton)
    {
      gameGrid.setCheckerboardPreset();
    }
    else if(e.getSource() == valentineButton)
    {
      gameGrid.setValentinePreset();
    }
  }

  /**
   * Called by the buttons who are responsible for loading patterns
   * at the specified flag locations on the board
   *
   * @param e The action event triggered by the button press
   */
  @FXML
  private void loadPatternByButtonName(ActionEvent e)
  {
    if(e.getSource() == patternButton1)
    {
      gameGrid.setPattern(patternButton1.getText());
    }
    else if(e.getSource() == patternButton2)
    {
      gameGrid.setPattern(patternButton2.getText());
    }
    else if(e.getSource() == patternButton3)
    {
      gameGrid.setPattern(patternButton3.getText());
    }
    else if(e.getSource() == patternButton4)
    {
      gameGrid.setPattern(patternButton4.getText());
    }
    else if(e.getSource() == patternButton5)
    {
      gameGrid.setPattern(patternButton5.getText());
    }
    else if(e.getSource() == patternButton6)
    {
      gameGrid.setPattern(patternButton6.getText());
    }
    else if(e.getSource() == patternButton7)
    {
      gameGrid.setPattern(patternButton7.getText());
    }
  }

  /**
   * Called by the play/pause button
   * @param e actionevent triggered by the button press
   */
  @FXML
  private void togglePause(ActionEvent e)
  {
    if(e.getSource() == pauseButton)
    {
      gameGrid.togglePause();
    }
  }

  /**
   * Called by the reset camera button
   * @param e actionevent called by the button press
   */
  @FXML
  private void resetCamera(ActionEvent e)
  {
    if(e.getSource() == resetCamera)
    {
      gameGrid.resetCamera();
    }
  }

  /**
   * Called by the reset board button, and also the "empty preset" button
   * @param e actionevent triggered by button presses
   */
  @FXML
  private void resetBoard(ActionEvent e)
  {
    if(e.getSource() == emptyButton || e.getSource() == emptyButton2)
    {
      gameGrid.resetBoard();
    }
  }

  /**
   * Called by the "save pattern" button, or by pressing enter after typing the
   * @param e
   */
  @FXML
  private void saveCameraViewAsPattern(ActionEvent e)
  {
    if(e.getSource() == savePatternButton || e.getSource() == patternFileNameTextField)
    {
      String filename = patternFileNameTextField.getText();

      gameGrid.saveCurrentCameraViewAsPattern(filename + ".rle");
    }
  }

  @FXML
  private void loadPatternByFilename(ActionEvent e)
  {
    if(e.getSource() == loadPatternByNameButton || e.getSource() == loadPatternFilenameField)
    {
      String filename = loadPatternFilenameField.getText();
      if(filename.equals(""))
        filename = "dot";

      gameGrid.loadPatternAtFlagsByFilename(filename + ".rle");
      //clearFlagButton.setDisable(true);
    }
  }

  @FXML
  private void simulateSingleFrame(ActionEvent e)
  {
    if(e.getSource() == simulateFrameButton)
    {
      gameGrid.simulateBoardFixedQuantity(1);
    }
  }

  @FXML
  private void flagOnInputCoords(ActionEvent e)
  {
    if(e.getSource() == flagButton || e.getSource() == xFlagField || e.getSource() == yFlagField)
    {
      String x = xFlagField.getText();
      String y = yFlagField.getText();

      int cellCol = Integer.parseInt(x);
      int cellRow = Integer.parseInt(y);

      gameGrid.toggleFlagAtCellCoords(cellRow, cellCol);
      //clearFlagButton.setDisable(false);
    }
  }

  @FXML
  private void clearFlags(ActionEvent e)
  {
    if(e.getSource() == clearFlagButton)
    {
      gameGrid.clearFlags();
     // clearFlagButton.setDisable(true);
    }
  }

  @FXML
  private void moveCameraToCoords(ActionEvent e)
  {
    if(e.getSource() == cellRowField || e.getSource() == cellColField)
    {
      int row = Integer.parseInt(cellRowField.getText());
      int col = Integer.parseInt(cellColField.getText());

      gameGrid.moveCameraToCoords(row, col);
    }
  }

  /**
   * The initialize function is called when Controller.java fetches it's AppController (instance).
   * The method initializes the GUI elements found within the AppController, such as the buttons, sliders,
   * Canvas, etc, and adds listeners to the elements that require them
   */
  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    gameGrid = new Grid();


    drawLoop = new AnimationTimer() {
      int frameCnt = 0;
      long lastNanoTime = 0;

      @Override
      public void handle(long now)
      {
        if(gameGrid.isInit())  gameGrid.drawToCanvas(gameCanvas);

        updatesValue.setText(Integer.toString(tempUpdatesValue));

        frameCnt++;
        long curTimeNano = System.nanoTime();
        if(curTimeNano > lastNanoTime + 1_000_000_000)
        {
          framesProperty.set(frameCnt);

          frameCnt = 0;
          lastNanoTime = curTimeNano;
        }
      }
    };

   drawLoop.start();
   gameCanvas.setFocusTraversable(true);
   gameCanvas.requestFocus();
   gameCanvas.setOnMousePressed(canvasMousePressedEventHandler);
   gameCanvas.setOnMouseReleased(canvasMouseReleasedEventHandler);
   gameCanvas.setOnScroll(canvasMouseZoomEventHandler);
   gameCanvas.setOnMouseDragged(canvasMouseDraggedEventHandler);
   gameCanvas.setOnMouseMoved(canvasMouseMovedEventHandler);

   bP.prefHeightProperty().bind(masterBox.heightProperty());
   bP.prefWidthProperty().bind(masterBox.widthProperty());
   canvasContainerAnchor.prefWidthProperty().bind(bP.widthProperty());
   canvasContainerAnchor.prefHeightProperty().bind(bP.heightProperty());
   gameCanvas.widthProperty().bind(canvasContainerAnchor.widthProperty());
   gameCanvas.heightProperty().bind(canvasContainerAnchor.heightProperty());

   cameraSpeedSlider.valueProperty().addListener(
           (observable, oldValue, newValue) -> gameGrid.adjustCameraPanSpeed(newValue.intValue()));

   gameCanvas.heightProperty().addListener(
           (observable, oldValue, newValue) -> gameGrid.updateCanvasHeight(newValue.intValue()));
   gameCanvas.widthProperty().addListener(
           (observable, oldValue, newValue) -> gameGrid.updateCanvasWidth(newValue.intValue()));
   gameCanvas.setOnKeyPressed(event ->
   {
     switch(event.getCode())
     {
       case SHIFT:
         cameraMode = false;
         break;
     }
   });
   gameCanvas.setOnKeyReleased(event ->
   {
     switch(event.getCode())
     {
       case SHIFT:
         cameraMode = true;
         break;
     }
   });
   gameCanvas.setOnMouseEntered(event -> gameCanvas.requestFocus());



   //well this is some ugly code right here
  gameGrid.getPausedProperty().addListener((observable, oldValue, newValue) ->
  {
    if(newValue == true) //now paused, enable buttons
    {
      gosperButton.setDisable(false);
      randomButton.setDisable(false);
      emptyButton.setDisable(false);
      emptyButton2.setDisable(false);
      almostFullButton.setDisable(false);
      savePatternButton.setDisable(false);
      loadPatternByNameButton.setDisable(false);
      flagButton.setDisable(false);
      simulateFrameButton.setDisable(false);
      patternButton1.setDisable(false);
      patternButton2.setDisable(false);
      patternButton3.setDisable(false);
      patternButton4.setDisable(false);
      patternButton5.setDisable(false);
      patternButton6.setDisable(false);
      patternButton7.setDisable(false);
      checkerBoardButton.setDisable(false);
      valentineButton.setDisable(false);
    }
    else
    {
      gosperButton.setDisable(true);
      randomButton.setDisable(true);
      almostFullButton.setDisable(true);
      emptyButton.setDisable(true);
      emptyButton2.setDisable(true);
      savePatternButton.setDisable(true);
      loadPatternByNameButton.setDisable(true);
      flagButton.setDisable(true);
      simulateFrameButton.setDisable(true);
      patternButton1.setDisable(true);
      patternButton2.setDisable(true);
      patternButton3.setDisable(true);
      patternButton4.setDisable(true);
      patternButton5.setDisable(true);
      patternButton6.setDisable(true);
      patternButton7.setDisable(true);
      checkerBoardButton.setDisable(true);
      valentineButton.setDisable(true);
    }
  });

  gameGrid.getUpdatesProperty().addListener((observable, oldValue, newValue) -> tempUpdatesValue = newValue.intValue());
  gameGrid.getFlagsProperty().addListener((observable, oldValue, newValue) -> clearFlagButton.setDisable(!newValue));
  framesProperty.addListener((observable, oldValue, newValue) -> framesValue.setText(newValue.toString()));
  }


  private double startClickX, startClickY;
  private double rightStartClickX, rightStartClickY;

  private EventHandler<MouseEvent> canvasMousePressedEventHandler =
          new EventHandler<MouseEvent>()
          {
            @Override
            public void handle(MouseEvent event)
            {
              if(event.isPrimaryButtonDown())
              {
                startClickX = event.getX();
                startClickY = event.getY();
              }
              else if(event.isSecondaryButtonDown())
              {
                rightStartClickX = event.getX();
                rightStartClickY = event.getY();
              }

              //coordinates of the click IN the canvas
            }
          };

  private EventHandler<MouseEvent> canvasMouseDraggedEventHandler =
          new EventHandler<MouseEvent>()
          {
    @Override
    public void handle(MouseEvent event)
    {

      if(event.isPrimaryButtonDown())
      {
        if(!cameraMode)
          gameGrid.handleMouseEventAtCoords(Grid.MouseEventTypes.DRAG_CLICK, (int)event.getX(), (int)event.getY());
        else
          gameGrid.handleDraggedDistance(startClickY - event.getY(), startClickX - event.getX());
      }
    }
  };


  private EventHandler<MouseEvent> canvasMouseReleasedEventHandler =
          new EventHandler<MouseEvent>()
          {
            @Override
            public void handle(MouseEvent event)
            {

              double endClickX = event.getX();
              double endClickY = event.getY();

              if(startClickY == endClickY && startClickX == endClickX)
              {
                gameGrid.handleMouseEventAtCoords(Grid.MouseEventTypes.LEFT_CLICK, (int)startClickX, (int)startClickY);
              }
              else if(rightStartClickX == endClickX && rightStartClickY == endClickY)
              {
                gameGrid.handleMouseEventAtCoords(Grid.MouseEventTypes.RIGHT_CLICK, (int)rightStartClickX, (int)rightStartClickY);
              }
            }
          };

  private EventHandler<ScrollEvent> canvasMouseZoomEventHandler =
          event ->
          {

            double vertScroll = event.getDeltaY();

            gameGrid.handleZoom(vertScroll > 0, (int)event.getX(), (int)event.getY());
            //System.out.println(vertScroll);
          };

  private EventHandler<MouseEvent> canvasMouseMovedEventHandler =
          new EventHandler<MouseEvent>()
          {
            @Override
            public void handle(MouseEvent event)
            {
              int curMouseX = (int)event.getX();
              int curMouseY = (int)event.getY();

              int cellX = gameGrid.getCellColBasedOnPxCoords(curMouseX);
              int cellY = gameGrid.getCellRowBasedOnPxCoords(curMouseY);

              cellColField.setText(Integer.toString(cellX ));
              cellRowField.setText(Integer.toString(cellY ));

            }
          };


  /**
   * This function specifies to the Controller's instance of the Grid that it should initialize and setup
   * a specified number of threads, and then start these threads for processing the board
   * @param threads The number of threads to initialize and start within the GameGrid
   * @see Grid
   */
  public void setAndStartGameThreads(int threads)
  {
    gameGrid.setThreadAmt(threads);
    gameGrid.startThreads();
  }

  /**
   * Defines the dimensions for the Grid that is owned by the controller, then initializes the data required for the
   * Grid's operation, and finally initializes the camera for the Grid (based on the Canvas size)
   * @param cellDims The size (in x and y dimensions) of the gameboard to be generated
   *                 @see Grid
   *                 @see Camera
   */
  public void setGridDimensionsAndInit(int cellDims)
  {
    //gameGrid = new Grid(cellDims, cellDims);
    gameGrid.setDims(cellDims, cellDims);
    gameGrid.constructGridStructures();
    gameGrid.initCamera((int)gameCanvas.getWidth(), (int)gameCanvas.getWidth());
    gameGrid.setRandomPreset();
    gameGrid.getPixelSizeProperty().addListener(
            (observable, oldValue, newValue) -> cellSizeLabel.setText("Cell Size: " + newValue.intValue() + " Pixels"));
  }
}
