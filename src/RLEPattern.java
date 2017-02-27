import java.io.*;
import java.util.Scanner;

/**
 * Denver Quane
 * ID#101611184
 * CS-351
 * last Rev. 2/15/17
 * This class is responsible for loading and creating .rle files for the purpose of saving and loading
 * stored patterns onto the board
 * I'm warning you, this class is ugly, and I hate text parsing
 */


public class RLEPattern
{
  private static final char LIVE_CHAR = 'o';
  private static final char DEAD_CHAR = 'b';

  private int x = 0;
  private int y = 0;

  private String patternString;
  private byte[][] patternArr;

  /**
   *
   * @return the minimum/bounded width of the pattern
   */
  public int getX(){return x;}

  /**
   *
   * @return The minimum/bounded height of the pattern
   */
  public int getY(){return y;}

  private boolean invalid = false;

  /**
   * If the file was not found successfully (esp. in terms of .jar loading paths)
   * This indicates that it will try a different file path for loading the .rle file
   * @return If rlepattern is not valid
   */
  public boolean isInvalid(){return invalid;}

  RLEPattern(String filepath)
  {
    filepath = filepath.replace(" ", "");

      File file = getResourceAsFile(filepath);

      if(file == null)
      {
        file = new File(filepath);
      }

    Scanner sc = null;
    try
    {
      sc = new Scanner(file);
    }
    catch (FileNotFoundException e)
    {
      //e.printStackTrace();
      invalid = true;
    }

    if(sc != null)
    {
      parseFile(sc);
      patternArr = new byte[y][x];
      parsePatternString();
    }
    else
    {

      System.out.println("file not found!");
    }
  }

  /**
   * Code for this method was found on http://stackoverflow.com/questions/14089146/file-loading-by-getclass-getresource.
   * This method converts/copies a resource path to a file so it can be read (and parsed) by the RLEpattern methods
   * @param resourcePath path to resource (an rle file)
   * @return File
   */
  public File getResourceAsFile(String resourcePath) {
    try {
      InputStream in = this.getClass().getResourceAsStream(resourcePath);
      if (in == null) {
        return null;
      }

      File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
      tempFile.deleteOnExit();

      try (FileOutputStream out = new FileOutputStream(tempFile)) {
        //copy stream
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
        }
      }
      return tempFile;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Takes a row of the board, and converts it to an RLE String (Run Length Encoded)
   * @param row Row of cells from the game board
   * @return Encoded RLE String of the row
   */
  public static String convertRowToEncodedString(byte[] row)
  {
    StringBuilder stringBuilder = new StringBuilder();

    for(int i = 0; i < row.length; i++)
    {
      boolean isCurCellDead = row[i] == Grid.DEAD;

      int longestSequence = longestLengthOfSameStatusCells(row, i, row.length);

      //appends a number followed by the character
      if(longestSequence > 1 && (longestSequence + i != row.length))
        stringBuilder.append(Integer.toString(longestSequence));

      stringBuilder.append(isCurCellDead ? DEAD_CHAR : LIVE_CHAR);

      i += longestSequence - 1;
    }
    stringBuilder.append('$');
    return stringBuilder.toString();
  }

  /**
   * Determines the longest length of cells that are the same status (dead, or age larger than 0) from the current index
   * @param row Row of the gameboard to analyze
   * @param pos starting index on the received row to start searching from
   * @param width maximum index for the received row
   * @return integer representing the number of continuous same cells from the received index
   */
  public static int longestLengthOfSameStatusCells(byte[] row, int pos, int width)
  {
    boolean refValue = (row[pos] == Grid.DEAD);
    int total = 0;

    for(int i = pos; i < width; i++)
    {
      if(refValue == (row[i] == Grid.DEAD)) { total++; }
      else { return total; }
    }
    return total;
  }

  /**
   *
   * @return byte array of the cells comprising a loaded patern
   */
  public byte[][] getArray(){return patternArr;}

  private void parsePatternString()
  {
    int curRow = 0;
    int curCol = 0;

    Scanner sc = new Scanner(patternString);

    sc.useDelimiter("[$!]");

    while(sc.hasNext()) //fetches the next row of the array
    {
      curCol = 0; //beginning of row
      String line = sc.next();
      String expandedLine = new String();

      char curChar;
      int sequentialBlankRows = 0;

      while(expandedLine.length() < x) //not fully constructed string
      {
        if(curCol >= line.length()) //if end of line, but not enough characters for the row
        {
          while(expandedLine.length() < x) //keep repeating the last character until end of row
          {
            expandedLine += DEAD_CHAR;
          }
          break; //next row
        }
        else
        {
          curChar = line.charAt(curCol);

          if(curChar == DEAD_CHAR || curChar == LIVE_CHAR)
          {
            expandedLine += curChar;
          }
          else //found a digit
          {
            String subSequence = "";

            char tempChar = line.charAt(curCol);

            while(tempChar != DEAD_CHAR && tempChar != LIVE_CHAR)
            {
              subSequence += tempChar;
              curCol++;

              if(curCol  < line.length())
                tempChar = line.charAt(curCol);
              else
              {
                sequentialBlankRows = tempChar - '0';
                break;
              }
            }
            //traverse characters until we have the whole number

            if(sequentialBlankRows == 0)
            {
              int numToRepeat = Integer.parseInt(subSequence);

              curChar = line.charAt(curCol);
              for (int i = 0; i < numToRepeat; i++)
              {
                expandedLine += curChar;
              }
              //repeat the character for a time
            }
          }
          curCol++;
        }
      }
      char lastCharOnLine = line.charAt(line.length() - 1);
      //System.out.println(lastCharOnLine);
      if(lastCharOnLine >= '0' && lastCharOnLine <= '9')
      {
        sequentialBlankRows = lastCharOnLine - '0';
        char secondLast = line.charAt(line.length() - 2);
        if(secondLast > '0' && secondLast < '9')
        {
          sequentialBlankRows += 10 * (secondLast - '0');
        }
      }


      //System.out.println(expandedLine);
      for(int i = 0; i < expandedLine.length(); i++)
      {
        patternArr[curRow][i] = (expandedLine.charAt(i) == DEAD_CHAR) ? (byte)0 : 1;
      }

      //System.out.println(expandedLine);
      if(sequentialBlankRows > 0)
        curRow += sequentialBlankRows;
      else
        curRow++;
    }
  }

  private void parseFile(Scanner sc)
  {
    sc.useDelimiter("\n");

    while(sc.hasNext() && (x == 0 || y == 0))
    {
      String total = sc.next();
      total = total.replaceAll("[ xyz=]","");

      if(total.charAt(0) == '#')
        continue;

      String[] arr = total.split("[,\n ]");
      x = Integer.parseInt(arr[0]);
      y = Integer.parseInt(arr[1]);
    }
    sc.reset();
    patternString = sc.next();
    while(sc.hasNext())
    {
      String temp = sc.next();
      patternString = patternString + temp;
    }

    //System.out.println(patternString);
  }
}
