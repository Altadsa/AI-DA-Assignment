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

    static String _imgFilePath = "C:\\Users\\spark\\Documents\\AI-DA-Assignment\\Assignment 2\\Doodles\\";
    static String _csvFilePath = "C:\\Users\\spark\\Documents\\AI-DA-Assignment\\Assignment 2\\CSV\\";
    private static final int GRID_SIZE = 50;

    static final String FEATURES_HEADINGS = "LABEL\tINDEX\tnr_pix\theight\twidth\tspan\trows_with_5\tcols_with_5\tneigh1\tneigh5"
            + "\tleft2tile\tright2tile\tverticalness\ttop2tile\tbottom2tile\thorizontalness\tnr_crosses\tnewlabel2\tnr_regions\t"
            + "nr_eyes\thollowness\timage_fill";

    static ArrayList<DoodleFeature> _doodleFeatures = new ArrayList<>();

    public static void main(String[] args) throws IOException
    {
        LoadFeatures();
        WriteFeaturesToFile();
    }

    private static void WriteFeaturesToFile() throws IOException {
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
    }

    //Loads all of the CSV files at a specified directory and creates new doodle features based on the data in the files.
    private static void LoadFeatures() throws IOException {
        Stream<Path> walk = Files.walk(Paths.get(_csvFilePath));

        List<String> results = walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());
        for (String result : results)
        {
            var doodleFeature = new DoodleFeature(LoadCsvData(result), result);
            _doodleFeatures.add(doodleFeature);
        }
    }

    //Loads a csv file and converts its values into a 2d integer array
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

    /*
        FOR PART 1
     */

    //Get a list of files at a specific directory and convert them to CSV files
    private static void GenerateCsvFromFiles() throws IOException
    {
        System.out.println("Specify the Images FilePath:");
        _imgFilePath = GetFilePathFromConsole();
        Stream<Path> imgDirectory = Files.walk(Paths.get(_imgFilePath));

        List<String> imageFiles = imgDirectory.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());
        System.out.println("Specify the CSV Filepath:");
        _csvFilePath = GetFilePathFromConsole();
        imageFiles.forEach(Main::ConvertToCsv);
    }

    //Get the name of the image directory from the Console
    private static String GetFilePathFromConsole() throws IOException
    {
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        String filePath = bReader.readLine();
        bReader.close();
        return filePath;
    }

    //Gets the file at a path, sets its name and writes 0 or 1 to tab delimited fields
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
                FileWriter fw = new FileWriter(csvName);
                PrintWriter pw = new PrintWriter(fw);
                for (int i = 0; i < height; i++)
                {
                    for (int j = 0; j < width; j++)
                    {
                        pw.print(IsPixelBlack(new Color(_img.getRGB(j, i))) + "\t");
                    }
                    pw.println();
                }
                pw.close();
                fw.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //Gets the value of a field by checking its RGB value
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
