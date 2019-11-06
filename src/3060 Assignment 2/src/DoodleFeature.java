import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DoodleFeature
{
    final int GRID_SIZE = 50;
    int[][] _data = new int[GRID_SIZE][GRID_SIZE];
    String _doodleFilepath;

    String _label = "";
    String _index = "";
    int _neigh1 = 0;
    int _neigh5 = 0;
    int _left2tile = 0;
    int _right2tile = 0;
    double _verticalness = 0;
    int _top2tile = 0;
    int _bottom2tile = 0;
    double _horizontalness = 0;
    int _horizontal3tile = 0;
    int _vertical3tile = 0;
    int _nrEyes = 0;
    double _hollowness = 0;
    double _imageFill = 0;

    public DoodleFeature(int[][] csvData, String filepath)
    {
        for (int rowIndex = 0; rowIndex < csvData.length; rowIndex++)
    {
        for (int columnIndex = 0; columnIndex < csvData[rowIndex].length; columnIndex++)
        {
            _data[columnIndex][rowIndex] = csvData[columnIndex][rowIndex];
        }
    }
        _doodleFilepath = filepath;
        GetDoodleName();
        GetPixelNeighbours();
        CountTwoTiles();
        CountHorizontalThreeTiles();
        CountVerticalThreeTiles();
        CountEyes();
    }

    public String GetDoodleFeatures()
    {
        String features = "";
        features += _label + "\t";
        features += _index + "\t";
        features += NumberOfPixels() + "\t";
        features += Height() + "\t";
        features += Width() + "\t";
        features += Span() + "\t";
        features += RowsWithFivePlus() + "\t";
        features += ColumnsWithFivePlus() + "\t";
        features += _neigh1 + "\t";
        features += _neigh5 + "\t";
        features += _left2tile + "\t";
        features += _right2tile + "\t";
        features += _verticalness + "\t";
        features += _top2tile + "\t";
        features += _bottom2tile + "\t";
        features += _horizontalness + "\t";
        features += _horizontal3tile + "\t";
        features += _vertical3tile + "\t";
        features += CountRegions() + "\t";
        features += _nrEyes + "\t";
        features += _hollowness + "\t";
        features += _imageFill;

        return features;
    }

    private void GetDoodleName()
    {
        Pattern pattern = Pattern.compile("(\\Q40178464_\\E)(.*)([.])");
        Matcher _matcher = pattern.matcher(_doodleFilepath);

        if (_matcher.find())
        {
            String[] name = _matcher.group(2).split("_");
            _label = name[0];
            _index = name[1];
        }

    }

    private void CountEyes()
    {
        boolean[][] markedPixels = new boolean[GRID_SIZE][GRID_SIZE];
        int eyeCount = 0;
        ArrayList<Integer> whiteInEyes = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < _data.length; rowIndex++)
        {
            for (int columnIndex = 0; columnIndex < _data[rowIndex].length; columnIndex++)
            {
                boolean isWhite = _data[columnIndex][rowIndex] == 0;
                boolean isMarked = markedPixels[columnIndex][rowIndex];
                if (isWhite && !isMarked)
                {
                    markedPixels[columnIndex][rowIndex] = true;
                    whiteInEyes.add(MarkEyes(markedPixels, rowIndex, columnIndex));
                    eyeCount++;
                }
            }
        }
        _nrEyes = eyeCount-1;

        int unusedArea = whiteInEyes.get(0);
        whiteInEyes.remove(0);
        int whitePixelCount = 0;
        for (int i = 0; i < whiteInEyes.size(); i++)
        {
            whitePixelCount += whiteInEyes.get(i);
        }
        _hollowness = (double) whitePixelCount / NumberOfPixels();
        _imageFill = (double) (whitePixelCount + NumberOfPixels()) / unusedArea;
    }

    private int MarkEyes(boolean[][] markedPixels, int currentRow, int currentColumn)
    {
        int size = 1;
        for (int rowIndex = currentRow - 1; rowIndex <= (currentRow + 1); rowIndex++)
        {
            for (int columnIndex = currentColumn - 1; columnIndex <= (currentColumn + 1) ; columnIndex++)
            {
                if (IsNeighbourValid(rowIndex, columnIndex))
                {
                    boolean isMarked = markedPixels[columnIndex][rowIndex];
                    boolean isWhite = _data[columnIndex][rowIndex] == 0;
                    if (!isMarked && isWhite)
                    {
                        if (!IsNotDiagonalNeighbour(currentRow, currentColumn, rowIndex, columnIndex))
                        {
                            int adjacentOne = _data[currentColumn + (columnIndex - currentColumn)][currentRow];
                            int adjacentTwo = _data[currentColumn][currentRow + (rowIndex - currentRow)];
                            if (adjacentOne != 1 && adjacentTwo != 1)
                            {
                                markedPixels[columnIndex][rowIndex] = true;
                                size += MarkEyes(markedPixels, rowIndex, columnIndex);
                            }
                        }
                        else
                        {
                            markedPixels[columnIndex][rowIndex] = true;
                            size += MarkEyes(markedPixels, rowIndex, columnIndex);
                        }
                    }
                }
            }
        }
        return size;
    }

    private int CountRegions()
    {
        boolean[][] markedPixels = new boolean[GRID_SIZE][GRID_SIZE];
        int regionCount = 0;
        for (int rowIndex = 0; rowIndex < _data.length; rowIndex++)
        {
            for (int columnIndex = 0; columnIndex < _data[rowIndex].length; columnIndex++)
            {
                boolean isBlack = _data[columnIndex][rowIndex] == 1;
                boolean isMarked = markedPixels[columnIndex][rowIndex];
                if (isBlack && !isMarked)
                {
                    markedPixels[columnIndex][rowIndex] = true;
                    MarkAllBlackNeighbours(markedPixels, rowIndex, columnIndex);
                    regionCount++;
                }
            }
        }
        return regionCount;
    }

    public void CountHorizontalThreeTiles()
    {
        int horizontal3tiles = 0;
        List<int[]> blackPixels = GetBlackPixelIndexes();
        boolean[][] markedTopTiles = new boolean[GRID_SIZE][GRID_SIZE];
        int selectedRow = 0, selectedColumn = 0;

        //Loop through all the black pixels
        for (int[] pixel : blackPixels)
        {
            selectedRow = pixel[0];
            selectedColumn = pixel[1];

            //Check for a horizontal 3tile
            boolean isLeftValid = IsNeighbourValid(selectedRow, selectedColumn-1);
            boolean isRightValud = IsNeighbourValid(selectedRow, selectedColumn+1);
            if (isLeftValid && isRightValud)
            {
                //Check if adjacent neighbours are black
                if (_data[selectedColumn-1][selectedRow] == 0 || _data[selectedColumn+1][selectedRow] == 0)
                    continue;
                //Checks if the three black tiles are already marked
                if (markedTopTiles[selectedColumn][selectedRow]
                        || markedTopTiles[selectedColumn-1][selectedRow]
                        || markedTopTiles[selectedColumn+1][selectedRow])
                {
                    continue;
                }
                //Indicates if any of the adjacent neighbours are invalid i.e. white/out of bounds
                boolean isNeighbourValid = false;
                rowloop:
                for (int rowIndex = selectedRow-1; rowIndex <= selectedRow + 1; rowIndex++)
                {
                    for (int columnIndex = selectedColumn-1; columnIndex <= selectedColumn + 1; columnIndex++)
                    {
                        boolean isSelectedPixel = rowIndex == selectedRow;
                        if (!isSelectedPixel)
                        {
                            if (!IsNeighbourValid(rowIndex, columnIndex))
                            {
                                isNeighbourValid = false;
                                break rowloop;
                            }
                            if (_data[columnIndex][rowIndex] == 1)
                            {
                                isNeighbourValid = false;
                                break rowloop;
                            }
                            isNeighbourValid = true;
                        }
                    }
                }
                if (isNeighbourValid)
                {
                    horizontal3tiles++;
                    for (int columnIndex = selectedColumn - 1; columnIndex <= selectedColumn + 1; columnIndex++)
                    {
                        markedTopTiles[columnIndex][selectedRow] = true;
                    }
                }
            }

        }
        _horizontal3tile = horizontal3tiles;
    }

    public void CountVerticalThreeTiles()
    {
        int verticalThreeTiles = 0;
        List<int[]> blackPixels = GetBlackPixelIndexes();
        boolean[][] markedVerticlTiles = new boolean[GRID_SIZE][GRID_SIZE];
        int selectedRow = 0, selectedColumn = 0;

        //Loop through all the black pixels
        for (int[] pixel : blackPixels)
        {
            selectedRow = pixel[0];
            selectedColumn = pixel[1];

            //Check for a vertical 3tile
            boolean isTopValid = IsNeighbourValid(selectedRow - 1, selectedColumn);
            boolean isBottomValid = IsNeighbourValid(selectedRow + 1, selectedColumn);
            if (isTopValid && isBottomValid)
            {
                //Check if adjacent neighbours are black
                if (_data[selectedColumn][selectedRow-1] == 0 || _data[selectedColumn][selectedRow + 1] == 0)
                    continue;
                //Checks if the three black tiles are already marked
                if (markedVerticlTiles[selectedColumn][selectedRow]
                        || markedVerticlTiles[selectedColumn][selectedRow - 1]
                        || markedVerticlTiles[selectedColumn][selectedRow + 1])
                {
                    continue;
                }
                //Indicates if any of the adjacent neighbours are invalid i.e. white/out of bounds
                boolean isNeighbourValid = false;
                rowloop:
                for (int rowIndex = selectedRow-1; rowIndex <= selectedRow + 1; rowIndex++)
                {
                    for (int columnIndex = selectedColumn-1; columnIndex <= selectedColumn + 1; columnIndex++)
                    {
                        boolean isSelectedPixel = selectedColumn == columnIndex;
                        if (!isSelectedPixel)
                        {
                            if (!IsNeighbourValid(rowIndex, columnIndex))
                            {
                                isNeighbourValid = false;
                                break rowloop;
                            }
                            if (_data[columnIndex][rowIndex] == 1)
                            {
                                isNeighbourValid = false;
                                break rowloop;
                            }
                            isNeighbourValid = true;
                        }
                    }
                }
                if (isNeighbourValid)
                {
                    verticalThreeTiles++;
                    for (int rowIndex = selectedRow - 1; rowIndex <= selectedRow + 1; rowIndex++)
                    {
                        markedVerticlTiles[selectedColumn][rowIndex] = true;
                    }
                }
            }

        }
        _vertical3tile = verticalThreeTiles;
    }

    private void MarkAllBlackNeighbours(boolean[][] markedPixels, int currentRow, int currentColumn)
    {
        for (int rowIndex = currentRow - 1; rowIndex <= (currentRow + 1); rowIndex++)
        {
            for (int columnIndex = currentColumn - 1; columnIndex <= (currentColumn + 1) ; columnIndex++)
            {
                if (IsNeighbourValid(rowIndex, columnIndex))
                {
                    boolean isMarked = markedPixels[columnIndex][rowIndex];
                    if (!isMarked && _data[columnIndex][rowIndex] == 1)
                    {
                        markedPixels[columnIndex][rowIndex] = true;
                        MarkAllBlackNeighbours(markedPixels, rowIndex, columnIndex);
                    }
                }
            }
        }
    }

    private void CountTwoTiles()
    {
        int left2Tiles = 0, right2Tiles = 0, top2Tiles = 0, bottom2Tile = 0;
        List<int[]> blackPixels = GetBlackPixelIndexes();
        boolean[][] leftTiles = new boolean[GRID_SIZE][GRID_SIZE];
        boolean[][] rightTiles = new boolean[GRID_SIZE][GRID_SIZE];
        boolean[][] topTiles = new boolean[GRID_SIZE][GRID_SIZE];
        boolean[][] bottomTiles = new boolean[GRID_SIZE][GRID_SIZE];
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
                                if (_data[currentColumn][currentRow] == 1)
                                {
                                    //Checks for start of Left/Right tile
                                    if (Math.abs(currentRow - selectedRow) == 1)
                                    {
                                        //Check for left tile
                                        boolean leftTileMarked = leftTiles[selectedColumn][selectedRow] && leftTiles[currentColumn][currentRow];
                                        if (IsNeighbourValid(selectedRow, selectedColumn + 1) && !leftTileMarked)
                                        {
                                            if (_data[selectedColumn+1][selectedRow] == 0 && IsNeighbourValid(currentRow, currentColumn + 1))
                                            {
                                                if (_data[currentColumn + 1][currentRow] == 0)
                                                {
                                                    left2Tiles++;
                                                    leftTiles[selectedColumn][selectedRow] = true;
                                                    leftTiles[currentColumn][currentRow] = true;
                                                }
                                            }
                                        }

                                        //Check for right tile
                                        boolean rightTileMarked = rightTiles[selectedColumn][selectedRow] && rightTiles[currentColumn][currentRow];
                                        if (IsNeighbourValid(selectedRow, selectedColumn - 1) && !rightTileMarked)
                                        {
                                            if (_data[selectedColumn - 1][selectedRow] == 0 && IsNeighbourValid(currentRow, currentColumn - 1))
                                            {
                                                if (_data[currentColumn - 1][currentRow] == 0)
                                                {
                                                    right2Tiles++;
                                                    rightTiles[selectedColumn][selectedRow] = true;
                                                    rightTiles[currentColumn][currentRow] = true;
                                                }
                                            }
                                        }
                                    }

                                    if (Math.abs(currentColumn - selectedColumn) == 1)
                                    {

                                        //Check for bottom tile
                                        boolean topTileMarked = topTiles[selectedColumn][selectedRow] && topTiles[currentColumn][currentRow];
                                        if (IsNeighbourValid(selectedRow + 1, selectedColumn) & !topTileMarked)
                                        {
                                            if (_data[selectedColumn][selectedRow + 1] == 0 && IsNeighbourValid(currentRow + 1, currentColumn))
                                            {
                                                if (_data[currentColumn][currentRow + 1] == 0)
                                                {
                                                    top2Tiles++;
                                                    topTiles[selectedColumn][selectedRow] = true;
                                                    topTiles[currentColumn][currentRow] = true;
                                                }
                                            }
                                        }

                                        //Check for top tile
                                        boolean bottomTileMarked = bottomTiles[selectedColumn][selectedRow] && bottomTiles[currentColumn][currentRow];
                                        if (IsNeighbourValid(selectedRow - 1, selectedColumn) && !bottomTileMarked)
                                        {
                                            if (_data[selectedColumn][selectedRow - 1] == 0 && IsNeighbourValid(currentRow - 1, currentColumn))
                                            {
                                                if (_data[currentColumn][currentRow - 1] == 0)
                                                {
                                                    bottom2Tile++;
                                                    bottomTiles[selectedColumn][selectedRow] = true;
                                                    bottomTiles[currentColumn][currentRow] = true;
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

        _right2tile = right2Tiles;
        _left2tile = left2Tiles;
        _verticalness = (double)(left2Tiles + right2Tiles) / NumberOfPixels();


        _bottom2tile = bottom2Tile;
        _top2tile = top2Tiles;
        _horizontalness = (double)(top2Tiles+bottom2Tile) / NumberOfPixels();

    }

    private boolean IsNotDiagonalNeighbour(int currentRow, int currentColumn,
                                                  int checkedRow, int checkedColumn)
    {
        return !(Math.abs(currentRow - checkedRow) > 0)
                || !(Math.abs(currentColumn - checkedColumn) > 0);
    }

    private void GetPixelNeighbours()
    {
        int pixelsWithOneNeighbour = 0;
        int pixelsWithFiveNeighbours = 0;
        for (int rowIndex = 0; rowIndex < GRID_SIZE; rowIndex++)
        {
            for (int columnIndex = 0; columnIndex < GRID_SIZE; columnIndex++)
            {
                if (_data[columnIndex][rowIndex] == 1)
                {
                    int neighbours = CountNeighbours(rowIndex, columnIndex);
                    if (neighbours == 1) pixelsWithOneNeighbour++;
                    if (neighbours >= 5) pixelsWithFiveNeighbours++;
                }
            }
        }

        _neigh1 = pixelsWithOneNeighbour;
        _neigh5 = pixelsWithFiveNeighbours;
    }

    private int CountNeighbours(int currentRow, int currentColumn)
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
                        if (_data[columnIndex][rowIndex] == 1)
                            neighbourCount++;
                    }
                }
            }
        }
        return neighbourCount;
    }

    private boolean IsNeighbourValid(int row, int column)
    {
        if (row < 0 || column < 0)
            return false;
        if (row >= GRID_SIZE || column >= GRID_SIZE)
            return false;
        return true;
    }

    private int ColumnsWithFivePlus()
    {
        int numberOfColumns = 0;
        for (int rowIndex = 0; rowIndex < GRID_SIZE; rowIndex++)
        {
            int blackPixelCount = 0;
            for (int columnIndex = 0; columnIndex < GRID_SIZE; columnIndex++)
            {
                int pixelValue = _data[rowIndex][columnIndex];
                if (pixelValue == 1) blackPixelCount++;
            }
            if (blackPixelCount >= 5) numberOfColumns++;
        }
        return numberOfColumns;
    }

    private int RowsWithFivePlus()
    {
        int numberOfRows = 0;
        for (int rowIndex = 0; rowIndex < GRID_SIZE; rowIndex++)
        {
            int blackPixelCount = 0;
            for (int columnIndex = 0; columnIndex < GRID_SIZE; columnIndex++)
            {
                int pixelValue = _data[columnIndex][rowIndex];
                if (pixelValue == 1) blackPixelCount++;
            }
            if (blackPixelCount >= 5) numberOfRows++;
        }
        return numberOfRows;
    }

    private double Span()
    {

        double euclideanDistance = 0;
        List<int[]> blackPixelData = GetBlackPixelIndexes();

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

    private List<int[]> GetBlackPixelIndexes()
    {
        List<int[]> blackPixelData = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < GRID_SIZE; rowIndex++)
        {
            for (int columnIndex = 0; columnIndex < GRID_SIZE; columnIndex++)
            {
                boolean isBlack = _data[columnIndex][rowIndex] == 1;
                if (isBlack)
                {
                    blackPixelData.add(new int[] {rowIndex, columnIndex});
                }
            }
        }
        return blackPixelData;
    }

    private int Width()
    {
        int leftmost = GRID_SIZE, rightmost = - 1;
        for (int i = 0; i < GRID_SIZE; i++)
        {
            for (int j = 0; j < GRID_SIZE; j++)
            {
                int pixelValue = _data[j][i];
                if (pixelValue == 1)
                {
                    leftmost = j < leftmost ? j : leftmost;
                    rightmost = j > rightmost ? j : rightmost;
                }
            }
        }

        return Math.abs(leftmost - rightmost) + 1;
    }

    private int Height()
    {
        int topmost = -1, bottommost = -1;
        for (int i = 0; i < GRID_SIZE; i++)
        {
            for (int j = 0; j < GRID_SIZE; j++)
            {
                int pixelValue = _data[j][i];
                if (pixelValue == 1)
                {
                    topmost = (topmost >= 0) ? topmost : i;
                    bottommost = (i <= bottommost) ? bottommost : i;
                }
            }
        }
        return Math.abs(bottommost - topmost) + 1;
    }

    private int NumberOfPixels()
    {
        int blackPixelCount = 0;
        for (int i = 0; i < GRID_SIZE; i++)
        {
            for (int j = 0; j < GRID_SIZE; j++)
            {
                if (_data[j][i] == 1)
                    blackPixelCount++;
            }
        }
        return blackPixelCount;
    }
}
