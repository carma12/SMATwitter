package smapackage.Graph;

import org.apache.commons.collections15.Factory;

/**
 * Created by carma12
 */
public class VertexFactory implements Factory {

    private int n = 0;
    public Node create()
    {
        return (new Node(n++));
    }

}
