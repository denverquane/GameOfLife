import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

/**
 * Denver Quane
 * ID#101611184
 * CS-351
 * last Rev. 2/15/17
 * This class handles all of the logic and drawing for the board representation of Conway's Game of Life
 */

public class Grid
{
  /**
   * Clarifies to the board whether a received mouse event was a left, right, or a dragging click
   */
  public enum MouseEventTypes
  {
    LEFT_CLICK, RIGHT_CLICK, DRAG_CLICK
  }

  private static final String PATTERN_DIR = "data/patterns/";

  private static final int TOTAL_3X3_COMBOS = 512;
  private static final int MIN_GRID_SIZE = 3;
  static final byte DEAD = 0;
  private static final byte ALIVE = 1;
  private static final byte MAX_AGE  = 10;
  private static final long NANO_PER_SEC = 1_000_000_000;

  private SimpleIntegerProperty currentUpdatesPS = new SimpleIntegerProperty();

  private int row_size;
  private int col_size;
  private int threads;

  private byte[][] board = null;
  private byte[][] new_board;

  private Camera boardCamera = null;

  private CyclicBarrier barrier;

  private byte[] newStateLookupTable;
  private Color[] ageColorGradients;

  private Boolean boardUpdated = true;
  private SimpleBooleanProperty paused = new SimpleBooleanProperty(true);
  private SimpleBooleanProperty currentlyTrackingFlags = new SimpleBooleanProperty(false);
  private Boolean threadKill = false;
  private volatile Integer boardUpdateQuantity;

  private simulatingThread[] simulationThreads;

  private ArrayList<Point> flaggedPoints;

  class simulatingThread extends Thread
  {
    private int startRow;
    private int endRow;

    simulatingThread(int startRow, int numRows)
    {
      this.startRow = startRow;
      this.endRow = startRow + numRows;
    }

    @Override
    public void run()
    {
      for(;;)
      {
        if(threadKill)
          break;

        if ((paused.get() == false || boardUpdateQuantity > 0) && !boardUpdated)
        {

          updateThreadGrid(this.startRow, this.endRow);
          boardUpdateQuantity--;
        }
        else
        {
          try
          {
            Thread.sleep(100); //prevent taxing the CPU too much while the game is paused
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
        }
      }
    }
  }

  /**
   * Used to get the Boolean property for the current paused status
   * - Used to update GUI elements
   * @return Paused SimpleBooleanProperty
   * @see AppController
   * @see SimpleBooleanProperty
   */
  public SimpleBooleanProperty getPausedProperty()
  {
    return paused;
  }

  /**
   * Returns the Integer Property for the current number of board updates per second
   * - Determines the info text displayed on the GUI
   * @return updates per second SimpleIntegerProperty
   * @see AppController
   * @see SimpleIntegerProperty
   */
  public SimpleIntegerProperty getUpdatesProperty()
  {
    return currentUpdatesPS;
  }

  /**
   * Returns the Boolean property indicating if the gameboard currently has flags to track
   * - Determines the status of the GUI's buttons
   * @return flag tracking SimpleBooleanProperty
   * @see AppController
   * @see SimpleBooleanProperty
   */
  public SimpleBooleanProperty getFlagsProperty(){ return currentlyTrackingFlags;}

  /**
   * Indicates that the threads should be killed (and thus, the program terminated
   */
  public void killThreads()
  {
    threadKill = true;
  }

  /**
   * Default constructor does NOT initialize the board structures when it is created, and is required to be called later
   */
  public Grid()
  {
    boardUpdateQuantity = 0;
  }

  /**
   * Simply checks if the grid object (or rather, the board array) has been initialized with the proper data structures
   * @return
   */
  public boolean isInit()
  {
    return board != null;
  }

  /**
   * Sets the dimensions of the board grid, +2 in each dimension to account for the empty,
   * un-updated buffer cells on the borders
   * @param rows Row (y) size of the grid
   * @param cols Column (x) size of the grid
   */
  public void setDims(int rows, int cols)
  {
    if(rows < MIN_GRID_SIZE || cols < MIN_GRID_SIZE)
    {
      System.out.println("Too small a grid!");
      board = null;
    }
    else
    {
      row_size = rows + 2;
      col_size = cols + 2;
    }
  }

  /**
   * Allocates and constructs the essential data structures for the board
   */
  public void constructGridStructures()
  {
    board = new byte[row_size][col_size];
    new_board = new byte[row_size][col_size];
    newStateLookupTable = new byte[TOTAL_3X3_COMBOS];
    constructLookupTable();
    generateColors();
    flaggedPoints = new ArrayList<>();
  }

  /**
   * Creates the camera to look over the board
   * @param camWidth Initial cell width of the camera view
   * @param camHeight Initial cell height of the camera view
   *                  @see Camera
   */
  public void initCamera(int camWidth, int camHeight)
  {
    boardCamera = new Camera(1, 1, camWidth, camHeight);
    boardCamera.setGridCellDims(row_size -1, col_size -1);
  }

  /**
   *
   * @return pixel property of the camera for eventlisteners in the GUI
   */
  public SimpleIntegerProperty getPixelSizeProperty()
  {
    return boardCamera.getPixelProperty();
  }

  /**
   * Moves the camera to a position on the board
   * @param cellrow row coordinate
   * @param cellcol column coordinate
   */
  public void moveCameraToCoords(int cellrow, int cellcol)
  {
    boardCamera.setNewPosition(cellrow, cellcol);
    boardUpdated = true;
  }

  /**
   * Specifies the number of threads to update the board with
   * @param threads Number of threads
   */
  public void setThreadAmt(int threads)
  {
    this.threads = threads;
    simulationThreads = new simulatingThread[this.threads];
  }

  /**
   * Receives the canvas to be drawn to, and proceeds to draw the board according to what the camera object can see
   * of the board
   * @param canvas The canvas to be drawn to
   *               @see Camera
   */
  public void drawToCanvas(Canvas canvas)
  {
      if(boardUpdated)
      {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        int startRow = boardCamera.getRowPos();
        int startCol = boardCamera.getColPos();

        int maxRowsToDraw = boardCamera.getHeight() + startRow;
        int maxColsToDraw = boardCamera.getWidth() + startCol;
        boolean drawGrid = boardCamera.getPixelSize() > 4;

        if(maxRowsToDraw > row_size - 1)
          maxRowsToDraw = row_size - 1;
        if(maxColsToDraw > col_size - 1)
          maxColsToDraw = col_size - 1;

        if(startCol < 0)
          startCol = 0;
        if(startRow < 0)
          startRow = 0;

        if(drawGrid)
        {
          gc.setFill(Color.DIMGRAY.darker().darker());
          gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
        else
        {
          gc.setFill(Color.BLACK);
          gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        for(int row = startRow; row < maxRowsToDraw ; row++) //starting a new row
        {
          for(int col = startCol; col < maxColsToDraw ; col++) //traversing a new column
          {
            boolean isFlaggedCell = false;
            Color color = null;

            byte curCell = board[row][col];

            Iterator<Point> iter = flaggedPoints.iterator();
            Point temp;

            while(iter.hasNext())
            {
              temp = iter.next();
              if(temp.getX() == row)
              {
                if(temp.getY() == col)
                {
                  isFlaggedCell = true;
                  color = Color.RED;
                  break;
                }
              }
            }

            if(!isFlaggedCell)
            {
              if(curCell != DEAD)
                color = ageColorGradients[curCell];
              else
                color = Color.BLACK;
            }

            gc.setFill(color);

            int pixelSize = boardCamera.getPixelSize();

            if(!drawGrid)
            {
              if(curCell != DEAD || isFlaggedCell)
                gc.fillRect(pixelSize*(col - boardCamera.getColPos()), (row - boardCamera.getRowPos())*pixelSize, pixelSize, pixelSize);
            }
            else //draw within grid lines
            {
              int tempSize = pixelSize - 1;
              gc.fillRect((col - boardCamera.getColPos())*pixelSize, (row - boardCamera.getRowPos())*pixelSize, tempSize, tempSize);
            }
          }
        }
        synchronized (boardUpdated) {boardUpdated = false;}
      }
      else
      {
        try
        {
          Thread.sleep(10);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }
  }

  /**
   * Starts the threads used to update the board, along with a cyclic barrier to swap the old and new
   * boards when they have been updated completely
   */
  public void startThreads()
  {
    int threadHeight = (row_size - 2) / this.threads;

    barrier = new CyclicBarrier(this.threads,
            new Runnable()
            {
              int frameCnt = 0;
              long lastNanoTime = 0;

              @Override
              public void run()
              {
                swapBoards();
                //swapSets();
                //board is ready to display
                synchronized (boardUpdated)
                {
                  boardUpdated = true;
                }
                frameCnt++;
                long curNanoTime = System.nanoTime();

                if(curNanoTime > lastNanoTime + NANO_PER_SEC)
                {
                  //System.out.println("Updates PS: " + frameCnt);
                  currentUpdatesPS.set(frameCnt);
                  frameCnt = 0;
                  lastNanoTime = curNanoTime;
                }

              }
            });

    for(int i = 0; i < this.threads; i++)
    {
      //the last thread of the board should compensate for uneven splits of the board amongst the threads
      if(i + 1 == this.threads)
      {
        int dx = ( row_size - 2 - (i+1)*threadHeight);
        //System.out.println(dx);
        simulationThreads[i] = new simulatingThread(i*threadHeight + 1, threadHeight + dx);
      }
      else
      {simulationThreads[i] = new simulatingThread(i*threadHeight + 1, threadHeight);

      }

      simulationThreads[i].setDaemon(true);
      simulationThreads[i].start();
    }
    //all threads are now running
  }

  /**
   * Updates the entirety of the game board based on the start, and end rows provided. This is to allow
   * for all threads to call the same function, with their own regions of "responsibility" for updating on the board
   * @param startRow The starting row for a given thread to start updating from
   * @param endRow The last row that a thread should update for it's region
   */
  public void updateThreadGrid(int startRow, int endRow)
  {
      for (int row = startRow; row < endRow; row++)
      {
        byte[] oldRow = board[row - 1];
        byte[] curRow = board[row];
        byte[] newRow = board[row + 1];

        int environment
                = ((oldRow[0] != DEAD) ? 32 : 0) + ((oldRow[1] != DEAD) ? 4 : 0)
                + ((curRow[0] != DEAD) ? 16 : 0) + ((curRow[1] != DEAD) ? 2 : 0)
                + ((newRow[0] != DEAD) ? 8 : 0) + ((newRow[1] != DEAD) ? 1 : 0);

        for (int col = 1; col < col_size - 1; col++)
        {
          environment = ((environment % 64) * 8) //shifts the 2x3 "window" to get ready for the new 3 values
                  + ((oldRow[col + 1] != DEAD) ? 4 : 0)
                  + ((curRow[col + 1] != DEAD) ? 2 : 0)
                  + ((newRow[col + 1] != DEAD) ? 1 : 0);

          byte newValue = newStateLookupTable[environment];
          new_board[row][col] = newValue;

          if(newValue != DEAD) //new value is alive, so add the old to it (which could be 0)
          {
            new_board[row][col] += curRow[col];
            if(new_board[row][col] > MAX_AGE) new_board[row][col] = MAX_AGE;
          }
        }
      }

    try
    {
      barrier.await();
    }
    catch (InterruptedException | BrokenBarrierException ex) {return;}
  }

  /**
   * Handles mouse events from the camera depending on their type and location
   * @param type Mouse event type, whether it be left, right, or a drag click
   * @param x pixel x location of the mouse event
   * @param y pixel y location of the mouse event
   */
  public void handleMouseEventAtCoords(MouseEventTypes type, int x, int y)
  {
    int cellCol = getCellColBasedOnPxCoords(x);
    int cellRow = getCellRowBasedOnPxCoords(y);

    if(cellCol < 1) cellCol = 1;
    if(cellRow < 1) cellRow = 1;

    if(cellCol > col_size - 2) cellCol = col_size - 2;
    if(cellRow > row_size - 2) cellRow = row_size - 2;


      switch (type)
      {
        case LEFT_CLICK:
          if (paused.get())
          {
            toggleBoardAtPos(cellRow - 1, cellCol - 1);
          }
          break;

        case RIGHT_CLICK:
          toggleFlagAtCellCoords(cellRow, cellCol);
          break;

        case DRAG_CLICK:
          if (paused.get() && board[cellRow][cellCol] == DEAD)
          {
            toggleBoardAtPos(cellRow - 1, cellCol - 1);
          }
          break;
      }

  }

  /**
   * Toggles a flag at a cell coordinate location
   * @param cellRow Row coordinate of the cell to toggle
   * @param cellCol Column coordinate of the cell to toggle
   */
  public void toggleFlagAtCellCoords(int cellRow, int cellCol)
  {
    if(paused.get())
    {
      Point temp = new Point(cellRow, cellCol);
      if(flaggedPoints.contains(temp))
      {
        flaggedPoints.remove(temp);
      }
      else
      {
        flaggedPoints.add(temp);
      }
      boardUpdated = true;
      if(flaggedPoints.isEmpty()) currentlyTrackingFlags.set(false);
      else currentlyTrackingFlags.set(true);
    }
  }

  /**
   * Clears all currently active flags
   */
  public void clearFlags()
  {
    flaggedPoints.clear();
    boardUpdated = true;
    currentlyTrackingFlags.set(false);
  }

  /**
   * Determines the Cell Column coordinate based on a pixel coordinate
   * @param x Pixel x coordinate on the Camera/canvas
   * @return Cell Column coordinate (x)
   * @see Camera
   */
  public int getCellColBasedOnPxCoords(int x)
  {
    int cellCol = x / boardCamera.getPixelSize();

    cellCol += boardCamera.getColPos();

    return cellCol;
  }

  /**
   * Determines the Cell Row coordinate based on a pixel coordinate
   * @param y Pixel y coordinate on the Camera/canvas
   * @return Cell Row coordinate (y)
   * @see Camera
   */
  public int getCellRowBasedOnPxCoords(int y)
  {
    int cellCol = y / boardCamera.getPixelSize();

    cellCol += boardCamera.getRowPos();

    return cellCol;
  }

  /**
   * Adjusts the camera based on how far the canvas has been dragged
   * @param y Y distance of drag
   * @param x X distance of drag
   *          @see Camera
   */
  public void handleDraggedDistance(double y, double x)
  {
    boardCamera.moveByDrag(x, y);
    boardUpdated = true;
  }

  /**
   * Specifies that the Board should update a fixed number of times (such as the button specifying a single update)
   * @param num Number of times to update the board before stopping
   */
  public void simulateBoardFixedQuantity(int num)
  {
    synchronized (boardUpdateQuantity)
    {
      boardUpdateQuantity = num;
    }
  }

  /**
   * Handles a change in zoom at a specific x and y coordinate on the canvas
   * @param direction positive or negative zoom direction
   * @param xLocation pixel x coordinate of the zoom event
   * @param yLocation pixel y coordinate of the zoom event
   */
  public void handleZoom(boolean direction, int xLocation, int yLocation) //false is negative zoom (out)
  {
    int change;
    if(direction)
    {
      change = +1;
    }
    else
    {
      change = -1;
    }
    int cellRow = getCellRowBasedOnPxCoords(yLocation);
    int cellCol = getCellColBasedOnPxCoords(xLocation);

    //gets the location of the zoom

    boardCamera.zoomAdjustAtCellCoords(change, cellCol, cellRow);
    //boardCamera.adjustPixelSize(change);
    boardUpdated = true;
  }

  /**
   * Loads a randomly distributed alive preset of the board
   */
  public void setRandomPreset()
  {
    resetBoard();
    for(int row = 1; row < row_size - 1; row++)
    {
      for(int col = 1; col < col_size - 1; col++)
      {
        double rand = Math.random();
        if(rand > 0.5)
        {
          board[row][col] = ALIVE;
        }
        else
        {
          board[row][col] = DEAD;
        }
      }
    }
  }

  /**
   * Loads the checkerboard preset
   */
  public void setCheckerboardPreset()
  {
    resetBoard();
    for(int row = 1; row < row_size - 1; row++)
    {
      int cutOff = (row * 2) % 18;

      for(int col = row; col < col_size - 1; col++)
      {
        if(cutOff % 2 == 0 && cutOff < 16)
        {
          board[row][col] = ALIVE;
        }
        cutOff++;
        cutOff %= 18;
      }
    }
  }

  /**
   * Loads my custom special preset (hearts across the board)
   */

  public void setValentinePreset()
  {
    resetBoard();

    String filename = "heart.rle";
    RLEPattern pat = new RLEPattern(PATTERN_DIR + filename);
    if(pat.isInvalid())
    {
      pat = new RLEPattern(filename);
    }
    int patWidth = pat.getX();
    int patHeight = pat.getY();

    for(int row = patHeight; row < (row_size - patHeight); row+=patHeight*2)
    {
      for(int col = patWidth; col < (col_size - patWidth); col+=patWidth*2)
      {
        loadPatternAtPos(pat, col, row);
      }
    }
  }

  /**
   * Loads the entire board as alive, except for the border cells
   */
  public void setAlmostFullPreset()
  {
      resetBoard();
      for(int row = 2; row < row_size - 2; row++)
      {
        for(int col = 2; col < col_size - 2; col++)
        {
          board[row][col] = ALIVE;
        }
      }
  }

  /**
   * Loads a preset with "glider gun" patterns along the top of the board
   */
  public void setGunPreset()
  {
    String filename = "gliderGun.rle";
    resetBoard();
    RLEPattern pat;

    pat = new RLEPattern(PATTERN_DIR + filename);

    if(pat.isInvalid()) //jar format messed up, load w/ other method
    {
      pat = new RLEPattern(filename);
    }

    loadPatternAtPos(pat, 5, 5);
    loadPatternAtPos(pat, 45, 5);
    loadPatternAtPos(pat, 85, 5);
    loadPatternAtPos(pat, 125, 5);
    loadPatternAtPos(pat, 165, 5);
  }

  /**
   * Loads the pattern by the specified filename, and places it at the flags on the board
   * @param filename Name of pattern file to load
   */
  public void setPattern(String filename)
  {
    loadPatternAtFlagsByFilename(filename);
  }

  /**
   * Saves the current camera's view of the board as a .RLE pattern file to be loaded later
   * @param filename Name of file to be saved
   *                 @see RLEPattern
   */
  public void saveCurrentCameraViewAsPattern(String filename)
  {
    PrintWriter writer;
    System.out.println("Saving file...");

    if(filename.equals(".rle"))
      filename = "noName.rle";

    try
    {
      writer = new PrintWriter(PATTERN_DIR + filename, "UTF-8");
    }
    catch (FileNotFoundException e)
    {
      //e.printStackTrace();
      try
      {
        writer = new PrintWriter(filename, "UTF-8");
      } catch (FileNotFoundException | UnsupportedEncodingException e1)
      {
        e1.printStackTrace();
        return;
      }
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace();
      return;
    }
    int startCol = boardCamera.getColPos();
    int startRow = boardCamera.getRowPos();

    int colWidth = boardCamera.getWidth();
    int rowHeight = boardCamera.getHeight();

    int patternHeight = startRow + rowHeight;
    int patternWidth = startCol + colWidth;

    String curRowString;

    StringBuilder entirePattern = new StringBuilder();

    int beginColsToSkip  = 0;
    int endColsToSkip = 0;
    int beginRowsToSkip = 0;
    int endRowsToSkip = 0;

    for(int col = startCol; col < patternWidth; col++)
    {
      if (isColumnEmpty(col, startRow, patternHeight)) beginColsToSkip++;
      else break;
    }

    for(int col = (patternWidth - 1); col > startCol + beginColsToSkip; col--)
    {
      if(isColumnEmpty(col, startRow, patternHeight)) endColsToSkip++;
      else break;
    }

    for(int row = startRow; row < patternHeight; row++) //beginning rows to skip
    {
      if (isRowEmpty(row, startCol, patternWidth)) beginRowsToSkip++;
      else break;
    }

    for(int row = patternHeight - 1; row > startRow + beginRowsToSkip; row--)
    {
      if (isRowEmpty(row, startCol, patternWidth)) endRowsToSkip++;
      else break;
    }

    for(int row = startRow + beginRowsToSkip; row < patternHeight - endRowsToSkip; row++)
    {
      byte[] subRow = Arrays.copyOfRange(board[row], startCol + beginColsToSkip, patternWidth - (endColsToSkip - 1));
      curRowString = RLEPattern.convertRowToEncodedString(subRow);
      entirePattern.append(curRowString);
    }

    writer.println("x = " + (colWidth - beginColsToSkip - endColsToSkip) + ", y = " + (rowHeight - beginRowsToSkip - endRowsToSkip)  + ", rule = null");
    writer.println(entirePattern);

    writer.close();
    System.out.println("Pattern saved as \"" + filename + "\" in \"" + PATTERN_DIR+".\"");
  }

  /**
   * Loads the pattern by it's filename at the flag locations on the board
   * @param filename
   * @see RLEPattern
   */
  public void loadPatternAtFlagsByFilename(String filename)
  {
    RLEPattern pat;

    pat = new RLEPattern(PATTERN_DIR + filename);
    if(pat.isInvalid())
    {
      pat = new RLEPattern(filename);
    }

    Iterator<Point> iter = flaggedPoints.iterator();
    while(iter.hasNext())
    {
      Point temp = iter.next();

      loadPatternAtPos(pat, (int)temp.getY(), (int)temp.getX());
    }
    flaggedPoints.clear();
    currentlyTrackingFlags.set(false);
  }

  /**
   * Toggles the pause status of the game
   */
  public void togglePause()
  {
    synchronized (paused) { paused.set(!paused.get());  }
  }

  /**
   * Resets the camera's position
   * @see Camera
   */
  public void resetCamera()
  {
    boardCamera.reset();
    boardUpdated = true;
  }

  /**
   * Triggers a change in the camera's values due to a change in the canvas height
   * @param height New pixel height of the canvas
   *               @see Camera
   */
  public void updateCanvasHeight(int height)
  {
    boardCamera.updateCanvasHeight(height);
    boardUpdated = true;
  }

  /**
   * Triggers a change in the camera's width based on the change in the canvas width
   * @param width New pixel width of the canvas
   *              @see Camera
   */
  public void updateCanvasWidth(int width)
  {
    boardCamera.updateCanvasWidth(width);
    boardUpdated = true;
  }

  /**
   * Changes the speed at which the camera pans across the board
   * @param newValue New camera pan speed
   *                 @see Camera
   */
  public void adjustCameraPanSpeed(int newValue)
  {
    boardCamera.setPanSpeed(newValue);
  }

  /**
   * resets the game board
   */
  public void resetBoard()
  {
    synchronized (board)
    {
      for (int row = 1; row < row_size - 1; row++)
      {
        for (int col = 1; col < col_size - 1; col++)
        {
          board[row][col] = DEAD;
        }
      }
    }
    boardUpdated = true;
  }

  private int numberOfNeighbors(int bitValue)
  {
    int totalNeighbors = 0;
    for(int bitIndex = 0; bitIndex < 9; bitIndex++)
    {
      boolean bitIsSet = (bitValue & (1 << bitIndex)) != 0;

      if(bitIsSet && bitIndex != 4) { totalNeighbors++; }
    }
    return totalNeighbors;
  }

  private void swapBoards()
  {
    byte[][] old = board;
    board = new_board;
    new_board = old;
  }

  private byte newCellState(byte currentState, int numNeighbors)
  {
    if(currentState != DEAD) //alive (some age > 0)
    {
      if(numNeighbors < 2 || numNeighbors > 3) { return DEAD; }
      else
      {
        if(currentState < MAX_AGE) { return (byte)(currentState + 1); }
        else { return MAX_AGE; }
      }
    }
    else //cell is dead
    {
      if(numNeighbors == 3) { return ALIVE; }
      else { return DEAD; }
    }
  }

  private void generateColors()
  {
    ageColorGradients = new Color[11];
    for(int i = 0; i < 11; i++)
    {
      ageColorGradients[i] = new Color((double)i * 0.02,
              1.0 - ((double)i * 0.03),
              (double)i * 0.09, 1.0);
    }
  }

  private void loadPatternAtPos(RLEPattern pattern, int x, int y)
  {
    int patWidth = pattern.getX();
    int patHeight = pattern.getY();

    if(x + patWidth > col_size || y + patHeight > row_size || x < 0 || y < 0)
    {
      System.out.println("invalid pattern placement!");
      return;
    }

    byte[][] arr = pattern.getArray();

    synchronized (board)
    {
      for (int row = 0; row < patHeight; row++)
      {
        for (int col = 0; col < patWidth; col++)
        {
          markBoardAtPos(arr[row][col], y + row, x + col);
        }
      }
    }
    boardUpdated = true;
  }

  private void markBoardAtPos(byte value, int row, int col)
  {
    board[row + 1][col + 1] = value;
  }

  private void toggleBoardAtPos(int row, int col)
  {
    if(board[row + 1][col + 1] == DEAD) { board[row + 1][col + 1] = ALIVE;  }
    else { board[row + 1][col + 1] = DEAD; }

    boardUpdated = true;
  }

  private void constructLookupTable()
  {
    for(int bitValue = 0; bitValue < TOTAL_3X3_COMBOS; bitValue++)
    {
      int neighbors = numberOfNeighbors(bitValue);

      byte curCellState = ((bitValue & 16) != 0) ? ALIVE : DEAD;
      //if the current constructed 3x3 has a center cell that is alive or dead

      newStateLookupTable[bitValue] = newCellState(curCellState, neighbors);
    }
  }

  private boolean isColumnEmpty(int col, int startRow, int endRow)
  {
    for(int row = startRow; row < endRow; row++)
    {
      if(board[row][col] != DEAD) return false;
    }
    return true;
  }
  private boolean isRowEmpty(int row, int startCol, int endCol)
  {
    for(int col = startCol; col < endCol; col++)
    {
      if(board[row][col] != DEAD) return false;
    }
    return true;
  }
}