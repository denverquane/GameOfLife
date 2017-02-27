import javafx.beans.property.SimpleIntegerProperty;

/**
 * Denver Quane
 * ID#101611184
 * CS-351
 * last Rev. 2/15/17
 * This class is responsible for the Camera that defines
 * what the user/program currently sees of the Game Board, as defined in Grid.java
 */

//the camera class "looks over" the board
// it does NOT influence the more intricate aspects of pixel drawing on the canvas and such
public class Camera
{
  private static final int MAX_PIXEL_SIZE = 50;
  private static final int DEFAULT_PIXEL_SIZE = 2;
  private static final int MIN_PIXEL_SIZE = 1;

  private int colPos, rowPos;
  private int width, height;
  private int gridCols, gridRows;
  private int canvasWidth, canvasHeight;

  //private int pixelSize = DEFAULT_PIXEL_SIZE;
  private SimpleIntegerProperty pixelSize = new SimpleIntegerProperty(DEFAULT_PIXEL_SIZE);
  private int panSpeedMult = 1;

  private double preciseX, preciseY;

  /**
   * Constructs a camera object and sets it's initial values
   * @param row The row Cell location that the camera starts at
   * @param col The column Cell location that the camera starts at
   * @param canvasWidth The pixel width of the canvas that the camera should draw to
   * @param canvasHeight The pixel height of the canvas that the camera should draw to
   */
  Camera(int row, int col, int canvasWidth, int canvasHeight)
  {
    this.colPos = col;
    this.rowPos = row;
    preciseY = rowPos * pixelSize.get();
    preciseX = colPos * pixelSize.get();


    this.canvasWidth = canvasWidth;
    this.canvasHeight = canvasHeight;
    width = canvasWidth / pixelSize.get();
    height = canvasHeight / pixelSize.get();
  }

  /**
   *
   * @return Pixel size in integer property format for listeners in the GUI
   */
  public SimpleIntegerProperty getPixelProperty()
  {
    return pixelSize;
  }

  /**
   * Updates the size of the camera when the size of the canvas has changed
   * @param width the new pixel width of the canvas
   */
  public void updateCanvasWidth(int width)
  {
    canvasWidth = width;
    this.width = canvasWidth / pixelSize.get();
  }

  /**
   * Updates the size of the camera when the size of the canvas has changed
   * @param height the new pixel height of the canvas
   */
  public void updateCanvasHeight(int height)
  {
    canvasHeight = height;
    this.height = canvasHeight / pixelSize.get();
  }

  /**
   * Sets the size, in cells, of how large the board that the camera traverses is
   * @param rows How many rows the board has
   * @param cols How many columns the board has
   */
  public void setGridCellDims(int rows, int cols)
  {
    gridCols = cols;
    gridRows = rows;
  }

  private void validateCameraPosition()
  {
    if(colPos > gridCols - width)
    {
      colPos = gridCols - width;
    }

    if(rowPos > gridRows  - height)
    {
      rowPos = gridRows - height;
    }

    if(rowPos < 1 )
      rowPos = 1;

    if(colPos < 1)
      colPos = 1;

    preciseX = colPos * pixelSize.get();
    preciseY = rowPos * pixelSize.get();
  }

  /**
   * Moves the camera to a specified location on the board
   * @param row row coordinate
   * @param col column coordinate
   */
  public void setNewPosition(int row, int col)
  {
    rowPos = row;
    colPos = col;

    validateCameraPosition();
  }

  /**
   * Resets the position, size, and zoom of the camera
   */
  public void reset()
  {
    colPos = 1;
    rowPos = 1;
    preciseX = colPos * pixelSize.get();
    preciseY = rowPos * pixelSize.get();

    //validateCameraPosition();

    pixelSize.set( DEFAULT_PIXEL_SIZE);

    //TODO adjust for canvas size
    width = canvasWidth / pixelSize.get();
    height = canvasHeight / pixelSize.get();
  }

  /**
   * Specifies the location that the Camera should place at the center of the new zoom level
   * @param change Change in zoom (positive or negative)
   * @param zoomCol Column location for the new zoom
   * @param zoomRow Row location for the new zoom
   */
  public void zoomAdjustAtCellCoords(int change, int zoomCol, int zoomRow)
  {
    int oldCenterX = colPos + (width / 2);
    int oldCenterY = rowPos + (height / 2);

    pixelSize.set(pixelSize.get() + change);

    if (pixelSize.get() > MAX_PIXEL_SIZE)
    {
      pixelSize.set( MAX_PIXEL_SIZE);
    }
    else if (pixelSize.get() < MIN_PIXEL_SIZE)
    {
      pixelSize.set(MIN_PIXEL_SIZE);
    }

    width = canvasWidth / pixelSize.get();
    height = canvasHeight / pixelSize.get();

    if (change > 0) //zooming in
    {
      colPos = zoomCol - (width / 2);
      rowPos = zoomRow - (height / 2);
    }
    else
    {
      colPos = oldCenterX - (width / 2);
      rowPos = oldCenterY - (height / 2);
    }
    preciseX = colPos * pixelSize.get();
    preciseY = rowPos * pixelSize.get();

    validateCameraPosition();
  }

  /**
   * Specifies the change of camera location by how much it was moved in the x and y dimensions
   * @param x displacement by dragging in the x dimension
   * @param y displacement by dragging in the y dimension
   */
  public void moveByDrag(double x, double y)
  {
    //System.out.println(x);

    if(Math.abs(x) > Math.abs(y))
    {
      if(x > 0)
      {
        x = panSpeedMult*pixelSize.get();
      }
      else if(x < 0)
      {
        x = -panSpeedMult*pixelSize.get();
      }
      preciseX += x;
      colPos = (int)preciseX / pixelSize.get();
    }
    else
    {
      if(y > 0)
      {
        y = panSpeedMult*pixelSize.get();
      }
      else if(y < 0)
      {
        y = -panSpeedMult*pixelSize.get();
      }
      preciseY += y;
      rowPos = (int)preciseY / pixelSize.get();
    }
    validateCameraPosition();
  }

  /**
   * Sets the new panning speed of the camera
   * @param newVal New speed value
   */
  public void setPanSpeed(int newVal)
  {
    panSpeedMult = newVal;
  }

  /**
   *
   * @return Camera's width in cells
   */
  public int getWidth(){return width;}

  /**
   *
   * @return Camera's height in cells
   */
  public int getHeight(){return height;}

  /**
   *
   * @return Column position of the camera
   */
  public int getColPos(){return colPos;}

  /**
   *
   * @return Row position of the camera
   */
  public int getRowPos(){return rowPos;}

  /**
   * Current pixel size (depends on the zoom)
   * @return current pixel size
   */
  public int getPixelSize(){return pixelSize.get();}
}
