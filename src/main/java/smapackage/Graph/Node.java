package smapackage.Graph;

/**
 * Created by carma12
 */
public class Node {

    private final int mID;
    private String mValue;
    private String mColor;

    public Node(int x)
    {
        mID = x;
    }

    public int getID()
    {
        return mID;
    }

    public void setValue(String value)
    {
        this.mValue = value;
    }

    public String getValue()
    {
        return (mValue);
    }

    public void setColor(String color)
    {
        mColor = color;
    }

    public String getColor()
    {
        return (mColor);
    }

    public String toString()
    {
        return (this.getValue());
    }


}
