package smapackage.Graph;

/**
 * Created by carma12
 */
public class Edge {

    private final int mID;
    private String mValue;

    public Edge(int x)
    {
        mID = x;
    }

    public int getID()
    {
        return mID;
    }

    public void setValue(String mValue)
    {
        this.mValue = mValue;
    }

    public String getValue()
    {
        return mValue;
    }

    public String toString()
    {
        return (this.getValue());
    }

}
