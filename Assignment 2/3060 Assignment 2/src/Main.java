import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.imageio.ImageIO;

public class Main {

    static BufferedImage _img = null;

    static String _path = "C:\\Users\\spark\\Documents\\Assignment_1\\Images\\Test_Image.jpg";
    static String _tsvFilePath = "C:\\Users\\spark\\Documents\\Assignment_1\\CSV\\";

    public static void main(String[] args) {

        ConvertJpgToCsv();
        try (Stream<Path> walk = Files.walk(Paths.get("D:\\Assignment_Images")))
        {
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());

            result.forEach(System.out::println);
        }
    }

    private static void ConvertJpgToCsv() {
        try
        {
            File newFile = new File(_path);
            System.out.println(newFile);
            _img = ImageIO.read(newFile);
            System.out.println("Image Imported!");

            if (_img != null)
            {
                int width = _img.getWidth(), height = _img.getHeight();
                FileWriter fos = new FileWriter(_tsvFilePath + newFile.getName().replace("jpg", "csv"));
                PrintWriter dos = new PrintWriter(fos);
                for (int i = 0; i < height; i++)
                {
                    for (int j = 0; j < width; j++)
                    {
                        dos.print(IsWhite(new Color(_img.getRGB(j, i))) + "\t");
                    }
                    dos.println();
                }
                dos.close();
                fos.close();
            }
            else
            {
                System.out.println("Fuck you Eclipse.");
            }


        }
        catch (IOException e)
        {
            System.out.print("Error with Image import");
            e.printStackTrace();
        }
    }


    private static int IsWhite(Color color)
    {
        if (color.getRed() > 127 || color.getGreen() > 127 || color.getBlue() > 127)
        {
            return 1;
        }
        return 0;
    }

}
