public class Pixel
{

    int _value = 0;
    int _row, _column;

    boolean _marked = false;

    public Pixel(int row, int column, int value)
    {
        _row = row;
        _column = column;
        _value = value;
    }

    public boolean IsBlack()
    {
        return _value == 1;
    }

    public boolean IsMarked()
    {
        return _marked;
    }

    public void Mark()
    {
        _marked = true;
    }

    public void Unmark()
    {
        _marked = false;
    }

}
