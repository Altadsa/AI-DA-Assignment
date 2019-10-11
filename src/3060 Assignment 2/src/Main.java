import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

public class Main {

    static BufferedImage _img = null;

    static String _path = "C:\\Users\\spark\\Documents\\AI-DA-Assignment\\Assignment 2\\Doodles\\";
    static String _csvFilePath = "C:\\Users\\spark\\Documents\\AI-DA-Assignment\\Assignment 2\\CSV\\";

    static String _testPath = "C:\\Users\\spark\\Documents\\AI-DA-Assignment\\Assignment 2\\TestDirectory\\40178464_Test_02.jpg";
    static String _csvPath = "C:\\Users\\spark\\Documents\\AI-DA-Assignment\\Assignment 2\\TestDirectory\\40178464_Test_02.csv";

    private static final int GRID_SIZE = 50;

    public static void main(String[] args) throws IOException
    {
        //ConvertToCsv(_testPath);
        //System.out.println("Enter the CSV Directory: ");
        int[][] csvData =  LoadCsvData(_csvPath);
        PrintArray2D(csvData);
//        PrintLabelIndex(_csvPath.split("\\\\")[7]);
//        System.out.println("nr_pix: " + BlackPixelCount(csvData));
//        System.out.println("height: " + Height(csvData));
//        System.out.println("width: " + Width(csvData));
//        System.out.println("span: " + Span(csvData));
//        System.out.println("rows_with_5: " + RowsWithFivePlus(csvData));
//        System.out.println("cols_with_5: " + ColumnsWithFivePlus(csvData));
//        GetPixelNeighbours(csvData);
        CountTwoTiles(csvData);
    }


    private static int[][] LoadCsvData(String filePath) throws IOException
    {
        BufferedReader csvReader = new BufferedReader(new FileReader(filePath));
        String row;
        int columnIndex = 0;
        int[][] datas = new int[GRID_SIZE][GRID_SIZE];
        while ((row = csvReader.readLine()) != null)
        {
            String[] data = row.split("\t");
            for (int i = 0; i < data.length; i++)
            {
                datas[i][columnIndex] = Integer.parseInt(data[i]);
            }
            columnIndex++;
        }
        csvReader.close();
        return datas;
    }

    private static void PrintArray2D(int[][] datas)
    {
        for (int i = 0; i < GRID_SIZE; i++)
        {
            for (int j = 0; j < GRID_SIZE; j++)
            {
                System.out.print(datas[j][i]);
            }
            System.out.println();
        }
    }

    private static void CountTwoTiles(int[][] datas)
    {
        int left2Tiles = 0, right2Tiles = 0, top2Tiles = 0, bottom2Tile = 0;
        List<int[]> blackPixels = GetBlackPixelIndexes(datas);
        int selectedRow = 0, selectedColumn = 0;
        for (int[] pixel : blackPixels)
        {
            selectedRow = pixel[0]; selectedColumn = pixel[1];
            for (int currentRow = selectedRow - 1; currentRow <= selectedRow + 1; currentRow++)
            {
                for (int currentColumn = selectedColumn - 1; currentColumn <= selectedColumn + 1; currentColumn++)
                {
                  if (IsNotDiagonalNeighbour(selectedRow, selectedColumn,
                          currentRow, currentColumn))
                  {
                      if (IsNeighbourValid(currentRow, currentColumn))
                      {

                          if (currentRow != selectedRow || currentColumn != selectedColumn)
                          {
                              if (datas[currentColumn][currentRow] == 1)
                              {
                                  //Checks for start of Left/Right tile
                                  if (Math.abs(currentRow - selectedRow) == 1)
                                  {
                                      //Check for left tile

                                      if (IsNeighbourValid(selectedRow, selectedColumn + 1))
                                      {
                                          if (datas[selectedColumn+1][selectedRow] == 0 && IsNeighbourValid(currentRow, currentColumn + 1))
                                          {
                                              if (datas[currentColumn + 1][currentRow] == 0)
                                              {
                                                    left2Tiles++;
                                              }
                                          }
                                      }
                                      //Check for right tile
                                      if (IsNeighbourValid(selectedRow, selectedColumn - 1))
                                      {
                                          if (datas[selectedColumn - 1][selectedRow] == 0 && IsNeighbourValid(currentRow, currentColumn - 1))
                                          {
                                              if (datas[currentColumn - 1][currentRow] == 0)
                                              {
                                                  right2Tiles++;
                                              }
                                          }
                                      }
                                  }

                                  if (Math.abs(currentColumn - selectedColumn) == 1)
                                  {
                                      //Check for bottom tile

                                      if (IsNeighbourValid(selectedRow + 1, selectedColumn))
                                      {
                                          if (datas[selectedColumn][selectedRow + 1] == 0 && IsNeighbourValid(currentRow + 1, currentColumn))
                                          {
                                              if (datas[currentColumn][currentRow + 1] == 0)
                                              {
                                                  top2Tiles++;
                                              }
                                          }
                                      }

                                      //Check for top tile
                                      if (IsNeighbourValid(selectedRow - 1, selectedColumn))
                                      {
                                          if (datas[selectedColumn][selectedRow - 1] == 0 && IsNeighbourValid(currentRow - 1, currentColumn))
                                          {
                                              if (datas[currentColumn][currentRow - 1] == 0)
                                              {
                                                  bottom2Tile++;
                                              }
                                          }
                                      }
                                  }
                              }
                          }
                      }
                  }

                }
            }
        }

        System.out.println("right2tiles: " + right2Tiles);
        System.out.println("left2tiles: " + left2Tiles);
        System.out.println("verticalness: " + (double)(left2Tiles + right2Tiles) / BlackPixelCount(datas));


        System.out.println("bottom2tiles: " + bottom2Tile);
        System.out.println("top2tiles: " + top2Tiles);
        System.out.println("horizontalness: " + (double)(top2Tiles+bottom2Tile) / BlackPixelCount(datas));

    }

    private static boolean IsNotDiagonalNeighbour(int currentRow, int currentColumn,
                                                  int checkedRow, int checkedColumn)
    {
        return !(Math.abs(currentRow - checkedRow) > 0)
                || !(Math.abs(currentColumn - checkedColumn) > 0);
    }

    private static int GetPixelNeighbours(int[][] datas)
    {
        int pixelsWithOneNeighbour = 0;
        int pixelsWithFiveNeighbours = 0;
        for (int rowIndex = 0; rowIndex < GRID_SIZE; rowIndex++)
        {
            for (int columnIndex = 0; columnIndex < GRID_SIZE; columnIndex++)
            {
                int neighbours = CountNeighbours(datas, rowIndex, columnIndex);
                if (neighbours == 1) pixelsWithOneNeighbour++;
                if (neighbours >= 5) pixelsWithFiveNeighbours++;
            }
        }

        System.out.println("neigh1: " + pixelsWithOneNeighbour);
        System.out.println("neigh5: " + pixelsWithFiveNeighbours);

        return 0;
    }

    private static int CountNeighbours(int[][] datas, int currentRow, int currentColumn)
    {
        int neighbourCount = 0;
        for (int rowIndex = currentRow - 1; rowIndex <= (currentRow + 1); rowIndex++)
        {
            for (int columnIndex = currentColumn - 1; columnIndex <= (currentColumn + 1) ; columnIndex++)
            {
                if (IsNeighbourValid(rowIndex, columnIndex))
                {
                    if (rowIndex != currentRow || columnIndex != currentColumn)
                    {
                        if (datas[columnIndex][rowIndex] == 1)
                            neighbourCount++;
                    }
                }
            }
        }
        return neighbourCount;
    }

    private static boolean IsNeighbourValid(int row, int column)
    {
        if (row < 0 || column < 0)
            return false;
        if (row >= GRID_SIZE || column >= GRID_SIZE)
            return false;
        return true;
    }

    private static int ColumnsWithFivePlus(int[][] datas)
    {
        int numberOfColumns = 0;
        for (int rowIndex = 0; rowIndex < GRID_SIZE; rowIndex++)
        {
            int blackPixelCount = 0;
            for (int columnIndex = 0; columnIndex < GRID_SIZE; columnIndex++)
            {
                int pixelValue = datas[rowIndex][columnIndex];
                if (pixelValue == 1) blackPixelCount++;
            }
            if (blackPixelCount >= 5) numberOfColumns++;
        }
        return numberOfColumns;
    }

    private static void PrintLabelIndex(String fileName)
    {
        String[] name = fileName.split("_");
        System.out.println("label: " + name[1]);
        System.out.println("index: " + name[2]);
    }

    //WORKS
    private static int RowsWithFivePlus(int[][] datas)
    {
        int numberOfRows = 0;
        for (int rowIndex = 0; rowIndex < GRID_SIZE; rowIndex++)
        {
            int blackPixelCount = 0;
            for (int columnIndex = 0; columnIndex < GRID_SIZE; columnIndex++)
            {
                int pixelValue = datas[columnIndex][rowIndex];
                if (pixelValue == 1) blackPixelCount++;
            }
            if (blackPixelCount >= 5) numberOfRows++;
        }
        return numberOfRows;
    }


    //WORKS
    private static double Span(int[][] data)
    {

        double euclideanDistance = 0;
        List<int[]> blackPixelData = GetBlackPixelIndexes(data);

        for (int i = 0; i < blackPixelData.size(); i++)
        {
            for (int j = 0; j < blackPixelData.size(); j++)
            {
                double horizontalSq = Math.pow(Math.abs(blackPixelData.get(i)[0] - blackPixelData.get(j)[0]) + 1, 2);
                double verticalSq = Math.pow(Math.abs(blackPixelData.get(i)[1] - blackPixelData.get(j)[1]) + 1, 2);
                double distance = Math.sqrt(horizontalSq + verticalSq);
                euclideanDistance = distance > euclideanDistance ? distance : euclideanDistance;
            }
        }
        return euclideanDistance;
    }

    private static List<int[]> GetBlackPixelIndexes(int[][] data)
    {
        List<int[]> blackPixelData = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < GRID_SIZE; rowIndex++)
        {
            for (int columnIndex = 0; columnIndex < GRID_SIZE; columnIndex++)
            {
                boolean isBlack = data[columnIndex][rowIndex] == 1;
                if (isBlack)
                {
                    blackPixelData.add(new int[] {rowIndex, columnIndex});
                }
            }
        }
        return blackPixelData;
    }


    //WORKS
    private static int Height(int[][] datas)
    {
        int topmost = -1, bottommost = -1;
        for (int i = 0; i < GRID_SIZE; i++)
        {
            for (int j = 0; j < GRID_SIZE; j++)
            {
                  int pixelValue = datas[j][i];
                  if (pixelValue == 1)
                  {
                      topmost = (topmost >= 0) ? topmost : i;
                      bottommost = (i <= bottommost) ? bottommost : i;
                  }
            }
        }
        return Math.abs(bottommost - topmost) + 1;
    }


    //WORKS
    private static int Width(int[][] datas)
    {
        int leftmost = GRID_SIZE, rightmost = - 1;
        for (int i = 0; i < GRID_SIZE; i++)
        {
            for (int j = 0; j < GRID_SIZE; j++)
            {
                int pixelValue = datas[j][i];
                if (pixelValue == 1)
                {
                    leftmost = j < leftmost ? j : leftmost;
                    rightmost = j > rightmost ? j : rightmost;
                }
            }
        }

        return Math.abs(leftmost - rightmost) + 1;
    }


    //WORKS
    private static int BlackPixelCount(int[][] datas)
    {
        int blackCount = 0;
        for (int i = 0; i < GRID_SIZE; i++)
        {
            for (int j = 0; j < GRID_SIZE; j++)
            {
                if (datas[j][i] == 1)
                    blackCount++;
            }
        }
        return blackCount;
    }

    /*
        FOR PART 1
     */


    private static String GetFilePathFromConsole() throws IOException
    {
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        String filePath = bReader.readLine();
        bReader.close();
        return filePath;
    }

    private static void GenerateCsvFromFiles() throws IOException
    {
        Stream<Path> walk = Files.walk(Paths.get(_path));

        List<String> results = walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());
        results.forEach(Main::ConvertToCsv);
    }


    private static void ConvertToCsv(String filePath)
    {
        try
        {
            File newFile = new File(filePath);
            _img = ImageIO.read(newFile);

            if (_img != null)
            {
                int width = _img.getWidth(), height = _img.getHeight();
                String csvName = _csvFilePath + "40178464_" + newFile.getName().replace("jpg", "csv");
                FileWriter fos = new FileWriter(csvName);
                PrintWriter dos = new PrintWriter(fos);
                for (int i = 0; i < height; i++)
                {
                    for (int j = 0; j < width; j++)
                    {
                        dos.print(IsPixelBlack(new Color(_img.getRGB(j, i))) + "\t");
                    }
                    dos.println();
                }
                dos.close();
                fos.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static int IsPixelBlack(Color color)
    {
        if (color.getRed() > 128 ||
            color.getBlue() > 128 ||
                color.getGreen() > 128)
        {
            return 0;
        }
        return 1;
    }

}
