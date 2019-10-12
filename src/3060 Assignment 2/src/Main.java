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

    static String _imgPath = "C:\\Users\\spark\\Documents\\AI-DA-Assignment\\Assignment 2\\Doodles\\";
    static String _csvFilePath = "C:\\Users\\spark\\Documents\\AI-DA-Assignment\\Assignment 2\\CSV\\";

    static String _testPath = "C:\\Users\\spark\\Documents\\AI-DA-Assignment\\Assignment 2\\TestDirectory\\40178464_Test_02.jpg";
    static String _csvPath = "C:\\Users\\spark\\Documents\\AI-DA-Assignment\\Assignment 2\\TestDirectory\\40178464_Test_02.csv";

    private static final int GRID_SIZE = 50;

    static final String FEATURES_HEADINGS = "label\tindex\tnr_pix\theight\twidth\tspan\trows_with_5\tcols_with_5\tneigh1\tneigh5"
            + "\tleft2tile\tright2tile\tverticalness\ttop2tile\tbottom2tile\thorizontalness\tnewlabel1\tnewlabel2\tnr_regions\t"
            + "nr_eyes\thollowness\tnewlabel3";

    static ArrayList<DoodleFeature> _doodleFeatures = new ArrayList<>();

    public static void main(String[] args) throws IOException
    {
        Stream<Path> walk = Files.walk(Paths.get(_csvFilePath));

        List<String> results = walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());
        for (String result : results)
        {
            var doodleFeature = new DoodleFeature(LoadCsvData(result), result);
            _doodleFeatures.add(doodleFeature);
        }
        String featuresName = "C:\\Users\\spark\\Documents\\AI-DA-Assignment\\Assignment 2\\40178464_features.csv";
        FileWriter fos = new FileWriter(featuresName);
        PrintWriter dos = new PrintWriter(fos);
        dos.println(FEATURES_HEADINGS);
        for (int i = 0; i < _doodleFeatures.size(); i++)
        {
            dos.println(_doodleFeatures.get(i).GetDoodleFeatures());
        }
        dos.close();
        fos.close();

        //ConvertToCsv(_testPath);
        //System.out.println("Enter the CSV Directory: ");
        //int[][] csvData =  LoadCsvData(_csvPath);
        //PrintArray2D(csvData);
//        PrintLabelIndex(_csvPath.split("\\\\")[7]);
//        System.out.println("nr_pix: " + BlackPixelCount(csvData));
//        System.out.println("height: " + Height(csvData));
//        System.out.println("width: " + Width(csvData));
//        System.out.println("span: " + Span(csvData));
//        System.out.println("rows_with_5: " + RowsWithFivePlus(csvData));
//        System.out.println("cols_with_5: " + ColumnsWithFivePlus(csvData));
//        GetPixelNeighbours(csvData);
        //CountTwoTiles(csvData);
        //CountRegions(csvData);
        //CountEyes(csvData);
    }


    private static int[][] LoadCsvData(String filePath)
    {
        try
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
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

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

    /*
        Get all black pixels
        If a pixel isn't marked
            Check if there is a potential loop
                if the check step is 3 or higher then begin checking if any neighbours are the src
                    if so say so and inc eye count by 1
     */
    static int eyeCount = 0;
    private static void CountEyes(int[][] data)
    {
        boolean[][] markedPixels = new boolean[GRID_SIZE][GRID_SIZE];

        for (int rowIndex = 0; rowIndex < data.length; rowIndex++)
        {
            for (int columnIndex = 0; columnIndex < data[rowIndex].length; columnIndex++)
            {
                boolean isBlack = data[columnIndex][rowIndex] == 1;
                boolean isMarked = markedPixels[columnIndex][rowIndex];
                if (isBlack && !isMarked)
                {

                }
            }
        }
        System.out.println("nr_eyes: " + eyeCount);
    }



    //WORKS
    private static void CountRegions(int[][] data)
    {
        boolean[][] markedPixels = new boolean[GRID_SIZE][GRID_SIZE];
        int regionCount = 0;
        for (int rowIndex = 0; rowIndex < data.length; rowIndex++)
        {
            for (int columnIndex = 0; columnIndex < data[rowIndex].length; columnIndex++)
            {
                boolean isBlack = data[columnIndex][rowIndex] == 1;
                boolean isMarked = markedPixels[columnIndex][rowIndex];
                if (isBlack && !isMarked)
                {
                    markedPixels[columnIndex][rowIndex] = true;
                    MarkNeighbours(data, markedPixels, rowIndex, columnIndex);
                    regionCount++;
                }
            }
        }
        System.out.println("nr_regions: " + regionCount);
    }

    private static void MarkNeighbours(int[][] data, boolean[][] markedPixels, int currentRow, int currentColumn)
    {
        for (int rowIndex = currentRow - 1; rowIndex <= (currentRow + 1); rowIndex++)
        {
            for (int columnIndex = currentColumn - 1; columnIndex <= (currentColumn + 1) ; columnIndex++)
            {
                if (IsNeighbourValid(rowIndex, columnIndex))
                {
                    boolean isMarked = markedPixels[columnIndex][rowIndex];
                    if (!isMarked && data[columnIndex][rowIndex] == 1)
                    {
                        markedPixels[columnIndex][rowIndex] = true;
                        MarkNeighbours(data, markedPixels, rowIndex, columnIndex);
                    }
                }
            }
        }
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
        Stream<Path> walk = Files.walk(Paths.get(_imgPath));

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
