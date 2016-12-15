package smapackage.Graph;

import org.apache.commons.collections15.Factory;

/**
 * Created by carma12
 */
public class EdgeFactory implements Factory {

    private int e = 0;
    public Edge create()
    {
        return (new Edge(e++));
    }

}
