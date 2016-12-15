package smapackage;

import edu.uci.ics.jung.algorithms.layout.SpringLayout;//FRLayout;
import edu.uci.ics.jung.graph.*;
//import edu.uci.ics.jung.graph.DirectedGraph; //
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.io.GraphMLReader;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.BaseAgent;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.graph.GiantComponentBuilder;
import org.gephi.graph.api.*;
//import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.io.importer.api.*;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.*;
import org.openide.util.Lookup;
import org.xml.sax.SAXException;
//import smapackage.Graph.Node;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import smapackage.Metric;


/**
 * Created by carma12
 * GraphAnalyzerAgent; Creates a graphical graph based in the graphml file.
 *
 */
public class GraphAnalyzerAgent extends BaseAgent {

    static String hashtag = "";
    static String graphFile =  ""; // "/home/carou/IdeaProjects/smatwitter/NoTeLoPerdonareJamasCarmena.graphml";
    static boolean start_now = false;

    Map<String, String> nodesGiantComponent;
    int component;
    GraphModel graphModel;
    AttributeModel attributeModel;
    int completeNodes;
    int completeEdges;
    //org.gephi.graph.api.Graph completeGraph;
    Graph completeGraph;
    Workspace workspace;
    org.gephi.io.importer.api.Container container;
    PrintWriter pwFile;
    String event;

    ACLMessage stats_message = new ACLMessage(ACLMessage.INFORM);


    /*VertexFactory vertexFactory;
    EdgeFactory edgeFactory;*/

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<Number, Number> vv;

    public GraphAnalyzerAgent (AgentID aid) throws Exception {
        super(aid);
    }

    public void onMessage(ACLMessage msg_ga){
        if (msg_ga.getOntology().equals("spreading hashtag")){
            System.out.println("(GraphAnalyzerAgent)-> Recibido hashtag!");
            hashtag = msg_ga.getContent();
            graphFile = "/home/carou/IdeaProjects/smatwitter/"+hashtag+".graphml";
            //creates folder in order to store data
            File folder = new File("Statistics/#"+hashtag);
            folder.mkdir();
            start_now = true;
        }
    }

    /**
     * A nested class to demo the GraphMouseListener finding the
     * right vertices after zoom/pan
     */
    static class TestGraphMouseListener<V> implements GraphMouseListener<V> {

        public void graphClicked(V v, MouseEvent me) {
            System.err.println("Vertex "+v+" was clicked at ("+me.getX()+","+me.getY()+")");
        }
        public void graphPressed(V v, MouseEvent me) {
            System.err.println("Vertex "+v+" was pressed at ("+me.getX()+","+me.getY()+")");
        }
        public void graphReleased(V v, MouseEvent me) {
            System.err.println("Vertex "+v+" was released at ("+me.getX()+","+me.getY()+")");
        }
    }


    public void fromGraphML2graph(String filename) throws ParserConfigurationException, SAXException, IOException {

        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\n--> GENERATING STATS... \n");
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end


            Factory<Number> vertexFactory = new Factory<Number>() {
                int n = 0;

                public Number create() {
                    return n++;
                }
            };
            Factory<Number> edgeFactory = new Factory<Number>() {
                int n = 0;

                public Number create() {
                    return n++;
                }
            };


            GraphMLReader<DirectedGraph<Number,Number>, Number, Number> gmlr =
                    new GraphMLReader<DirectedGraph<Number,Number>, Number, Number>(vertexFactory, edgeFactory);
            final DirectedGraph<Number,Number> graph = new DirectedSparseMultigraph<Number,Number>();
            gmlr.load(filename, graph);


        if (graph.getVertices().equals(null) || graph.equals("")) {
            System.out.println("\nGrafo vacío. No sé porqué :S\n");
            System.out.println("\nGRAPH: " + graph.toString() + "\n");
        }else {

            System.out.println("\nGRAPH: "+graph.toString()+"\n");
            // create a simple graph for the demo
            vv = new VisualizationViewer<Number, Number>(new SpringLayout<Number, Number>(graph));

            vv.addGraphMouseListener(new TestGraphMouseListener<Number>());
            vv.getRenderer().setVertexRenderer(
                    new GradientVertexRenderer<Number, Number>(
                            Color.white, Color.red,
                            Color.white, Color.blue,
                            vv.getPickedVertexState(),
                            false));

            // add my listeners for ToolTips
            vv.setVertexToolTipTransformer(new ToStringLabeller<Number>());
            vv.setEdgeToolTipTransformer(new Transformer<Number, String>() {
                public String transform(Number edge) {
                    return "E" + graph.getEndpoints(edge).toString();
                }
            });

            vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Number>());
            vv.getRenderer().getVertexLabelRenderer().setPositioner(new BasicVertexLabelRenderer.InsidePositioner());
            vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);
            //own
            //vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<Number>());
            vv.getRenderer().getVertexRenderer().toString();

            // create a frome to hold the graph
            final JFrame frame = new JFrame();
            Container content = (Container) frame.getContentPane();
            final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
            content.add(panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<Number, Number>();
            vv.setGraphMouse(graphMouse);
            vv.addKeyListener(graphMouse.getModeKeyListener());

            JMenuBar menubar = new JMenuBar();
            menubar.add(graphMouse.getModeMenu());
            panel.setCorner(menubar);


            vv.addKeyListener(graphMouse.getModeKeyListener());
            vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");

            final ScalingControl scaler = new CrossoverScalingControl();

            JButton plus = new JButton("+");
            plus.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scaler.scale(vv, 1.1f, vv.getCenter());
                }
            });
            JButton minus = new JButton("-");
            minus.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scaler.scale(vv, 1 / 1.1f, vv.getCenter());
                }
            });

            JPanel controls = new JPanel();
            controls.add(plus);
            controls.add(minus);
            content.add(controls, BorderLayout.SOUTH);

            frame.pack();
            frame.setVisible(true);

            //a partir de aquí, obtener estadísticas
            //LIBRERÍAS: Betweennes Centrality, Closeness Centrality, Dijkstra Shortest Path, EIgenvector Centrality, Page Rank, Shortest Path
            //A PARTE DE ESO: Sacar el nº total de vértices y de ejes del grafo. Buscar centralidad.


            //---------------------------------------------------------------
            //TESTING WITH GEPHI


            //USING GEPHI
            ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
            pc.newProject();
            workspace = pc.getCurrentWorkspace();

            //Import first file
            ImportController importController = Lookup.getDefault().lookup(ImportController.class);
            container = null;
            try {
                File file = new File(graphFile);
                container = importController.importFile(file);

            } catch (Exception ex) {
                ex.printStackTrace();

            }

            try {
                //Append imported data to GraphAPI
                importController.process(container, new DefaultProcessor(), workspace);
            }catch(Exception e_append_idata){
                System.out.println("ERROR APPENDING IMPORTED DATA: "+e_append_idata);
                //// message to VisualAgent
                try {
                    stats_message = new ACLMessage(ACLMessage.INFORM);
                    stats_message.setSender(this.getAid());
                    stats_message.addReceiver(new AgentID("AgentVisual"));
                    stats_message.setContent("\nERROR APPENDING IMPORTED DATA: "+e_append_idata);
                    stats_message.setOntology("Show this! - from GraphAnalyzer");
                    this.send(stats_message);  //---->(Visual agent)

                }catch(Exception e_mes_va){
                    System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                }
                //// end
            }

            try {

                try {
                    //Get a graph model - it exists because we have a workspace
                    graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
                }catch(Exception e_gm){
                    System.out.println("ERROR GETTING GRAPH MODEL: "+e_gm);
                    //// message to VisualAgent
                    try {
                        stats_message = new ACLMessage(ACLMessage.INFORM);
                        stats_message.setSender(this.getAid());
                        stats_message.addReceiver(new AgentID("AgentVisual"));
                        stats_message.setContent("\nERROR GETTING GRAPH MODEL: "+e_gm);
                        stats_message.setOntology("Show this! - from GraphAnalyzer");
                        this.send(stats_message);  //---->(Visual agent)

                    }catch(Exception e_mes_va){
                        System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                    }
                    //// end
                }

                try {
                    AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
                    attributeModel = attributeController.getModel();
                }catch(Exception e_am){
                    System.out.println("ERROR GETTING ATTRIBUTE MODEL: "+e_am);
                    //// message to VisualAgent
                    try {
                        stats_message = new ACLMessage(ACLMessage.INFORM);
                        stats_message.setSender(this.getAid());
                        stats_message.addReceiver(new AgentID("AgentVisual"));
                        stats_message.setContent("\nERROR GETTING ATTRIBUTE MODEL: "+e_am);
                        stats_message.setOntology("Show this! - from GraphAnalyzer");
                        this.send(stats_message);  //---->(Visual agent)

                    }catch(Exception e_mes_va){
                        System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                    }
                    //// end
                }

                try {
                    completeGraph = graphModel.getGraph();//(DirectedGraph) graphModel.getGraph();
                    //System.out.println("COMPLETE GRAPH: "+completeGraph);
                    //sortedAdjacencyMatrix(completeGraph);
                }catch(Exception e_cg){
                    System.out.println("ERROR GETTING COMPLETE GRAPH: "+e_cg);
                    //// message to VisualAgent
                    try {
                        stats_message = new ACLMessage(ACLMessage.INFORM);
                        stats_message.setSender(this.getAid());
                        stats_message.addReceiver(new AgentID("AgentVisual"));
                        stats_message.setContent("\nERROR GETTING COMPLETE GRAPH: "+e_cg);
                        stats_message.setOntology("Show this! - from GraphAnalyzer");
                        this.send(stats_message);  //---->(Visual agent)

                    }catch(Exception e_mes_va){
                        System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                    }
                    //// end
                }


                try {
                    completeNodes = completeGraph.getNodes().toArray().length;
                    System.out.println("Num nodes del grafo completo: "+completeNodes);
                    //// message to VisualAgent
                    try {
                        stats_message = new ACLMessage(ACLMessage.INFORM);
                        stats_message.setSender(this.getAid());
                        stats_message.addReceiver(new AgentID("AgentVisual"));
                        stats_message.setContent("\nNum nodes del grafo completo: "+completeNodes);
                        stats_message.setOntology("Show this! - from GraphAnalyzer");
                        this.send(stats_message);  //---->(Visual agent)

                    }catch(Exception e_mes_va){
                        System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                    }
                    //// end
                }catch(Exception e_cn){
                    System.out.println("ERROR GETTING COMPLETE NODES: "+e_cn);

                }

                try {
                    completeEdges = completeGraph.getEdges().toArray().length; //completeGraph.getVertexs().toArray()
                    System.out.println("Num edges del grafo completo: " + completeEdges);
                    //// message to VisualAgent
                    try {
                        stats_message = new ACLMessage(ACLMessage.INFORM);
                        stats_message.setSender(this.getAid());
                        stats_message.addReceiver(new AgentID("AgentVisual"));
                        stats_message.setContent("\nNum edges del grafo completo: " + completeEdges);
                        stats_message.setOntology("Show this! - from GraphAnalyzer");
                        this.send(stats_message);  //---->(Visual agent)

                    }catch(Exception e_mes_va){
                        System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                    }
                    //// end
                }catch(Exception e_ce){
                    System.out.println("ERROR GETTING COMPLETE EDGES: "+e_ce);
                }



                try { //org.gephi.graph.dhns.graph.HierarchicalDirectedGraphImpl cannot be cast to edu.uci.ics.jung.graph.DirectedGraph
                    //System.out.println("Network level properties");
                    //networkLevelProperties(completeGraph);
                    //System.out.println("GRAPHMODEL: "+graphModel);
                    //System.out.println("DIRECTED GRAPH MODEL VIEW: "+graphModel.getDirectedGraph().getView().getGraphModel());

                    org.gephi.graph.api.DirectedGraph giantGraph = (org.gephi.graph.api.DirectedGraph) getGigantSubGraph(graphModel);

                    Node[] nodesGiantGraph = (Node[]) giantGraph.getNodes().toArray();

                    //System.out.println("nodesGiantGraph: "+ Arrays.toString(giantGraph.getNodes().toArray()));

                    nodesGiantComponent = new HashMap<String, String>();
                    for (Node aNodesGiantGraph : nodesGiantGraph) {
                        nodesGiantComponent.put(aNodesGiantGraph.getNodeData().getLabel(), aNodesGiantGraph.getNodeData().getId());
                        //System.out.println("aNodesGiantGraph: "+aNodesGiantGraph);
                    }


                } catch (Exception e_network_level_props) {
                    System.out.println("ERROR GETTING network level properties: " + e_network_level_props);
                    //// message to VisualAgent
                    try {
                        stats_message = new ACLMessage(ACLMessage.INFORM);
                        stats_message.setSender(this.getAid());
                        stats_message.addReceiver(new AgentID("AgentVisual"));
                        stats_message.setContent("\nERROR GETTING network level properties: " + e_network_level_props);
                        stats_message.setOntology("Show this! - from GraphAnalyzer");
                        this.send(stats_message);  //---->(Visual agent)

                    }catch(Exception e_mes_va){
                        System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                    }
                    //// end
                }


                nodeLevelProperties();
                networkLevelProperties((org.gephi.graph.api.DirectedGraph)completeGraph);

            }catch(Exception e_data){
                System.out.println("ERROR WITH DATA: "+e_data);
                //// message to VisualAgent
                try {
                    stats_message = new ACLMessage(ACLMessage.INFORM);
                    stats_message.setSender(this.getAid());
                    stats_message.addReceiver(new AgentID("AgentVisual"));
                    stats_message.setContent("\nERROR WITH DATA: "+e_data);
                    stats_message.setOntology("Show this! - from GraphAnalyzer");
                    this.send(stats_message);  //---->(Visual agent)

                }catch(Exception e_mes_va){
                    System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                }
                //// end
            }

            //--------------------------------------------------------------------------------------
            //BETWEENNESS CENTRALITY
            /*try {
                BetweennessCentrality ranker = new BetweennessCentrality(graph);
                ranker.evaluate();
                //System.out.println("\nRanker.getRankScoreKey() -> "+ranker.getRankScoreKey()+"\n");
                ranker.printRankings(true, true); //boolean verbose, boolean printscore
            }catch(Exception e_bc){
                System.out.println("\nError in Betweenness Centrality: "+e_bc+"\n");
            }

            //CLOSENESS CENTRALITY
            FileWriter fichero_closeness = null;
            PrintWriter pw_closeness = null;

            try {
                fichero_closeness = new FileWriter("closeness_centrality.txt");
                pw_closeness = new PrintWriter(fichero_closeness);
                ClosenessCentrality<Number, Number> cc = new ClosenessCentrality<Number, Number>(graph, new UnweightedShortestPath<Number, Number>(graph));
                for (int i = 0; i < num_edges; i++) {
                    pw_closeness.println("\nCloseness Centrality edge : " + i + ": " + cc.getVertexScore((Number) i) + "\n");
                }
                fichero_closeness.close();
                System.out.println("\nCloseness Centrality Calculated!\n");
            }catch(Exception e_clos){
                System.out.println("\nError in closeness centrality: "+e_clos+"\n");
            }

            //DIJKSTRA SHORTEST PATH
            //cómo usar esta función?
            FileWriter fichero_dijkstra = null;
            PrintWriter pw_dijkstra = null;
            try {
                //comprobar que el grafo no está aislado. (necesita estar conectado a más vértices para tener camino mínimo. Si no, será infinito)
                //buscar info sobre la componente gigante
                fichero_dijkstra = new FileWriter("Dijkstra_path.txt");
                pw_dijkstra = new PrintWriter(fichero_dijkstra);

                DijkstraShortestPath shortestPathAlg = new DijkstraShortestPath(graph);

                Collection<Number> verts = graph.getVertices();
                System.out.println("---> Vértices para Dijkstra: "+verts+"\n");
                for (int i = 0; i < verts.size(); i++) {
                    for (int j = 0; j < verts.size(); j++) {
                        //usar getPath u otro método?
                        List edgeList = (List) shortestPathAlg.getPath(i, j);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              ;
                        pw_dijkstra.println("Nodo " + i + " a " + j + ": " + edgeList + "\n");

                    }
                    pw_dijkstra.println("\n\n");
                }
                fichero_dijkstra.close();
                System.out.println("\nDijkstra Shortest Path Calculated!\n");
            }catch(Exception e){
                System.out.println("\nError in Dijkstra Shortest Path: "+e+"\n");
            }


            //PAGE RANK
            FileWriter fichero_page_rank = null;
            PrintWriter pw_page_rank = null;
            Collection<Number> verts = graph.getVertices();

            double alpha = 0.6; //probability of following any outgoing edge from a given vertex
            double tolerance =  0.001; //Minimum change from one step to the next; if all changes are <= tolerance, no further updates will occur. Defaults to 0.001.
            int max_iterations = 100;  //Maximum number of iterations to use before terminating. Defaults to 100.
            PageRank ranker = new PageRank(graph, alpha);

            try {
                long start = System.currentTimeMillis() ;
                fichero_page_rank = new FileWriter("Page_rank.txt");
                pw_page_rank = new PrintWriter(fichero_page_rank);

                ranker.setTolerance(tolerance) ;
                ranker.setMaxIterations(max_iterations);
                ranker.evaluate();

                pw_page_rank.println("Tolerance = " + ranker.getTolerance());
                pw_page_rank.println("Dump factor = " + (1.00d - ranker.getAlpha())) ;
                pw_page_rank.println("Max iterations = " + ranker.getMaxIterations());
                pw_page_rank.println("PageRank computed in " + (System.currentTimeMillis()-start) + " ms");

                Map<Node, Object> result = new HashMap<Node, Object>();
                for (int i=0; i<verts.size(); i++){
                        result.put(new Node(i), ranker.getVertexScore(i));
                        //result.put(new Node(i), i);
                    //pw_page_rank.println(ranker.getVertexScore(i));
                }
                pw_page_rank.println(result);
                System.out.println("\nPage Rank calculated!");

            }catch(Exception e_pager){
                System.out.println("\nError evaluating graph for PageRank statistics: "+e_pager+"\n");
            }

            //SHORTEST PATH

            try {
                ShortestPath sp = null;
                Map<Node, Object> map_sp = sp.getIncomingEdgeMap(verts);
                System.out.println("\nMAP: "+map_sp+"\n");
            }catch(Exception e_sp){
                System.out.println("\nError during extracting Shortest Path: "+e_sp+"\n");
            }*/



        }

    }




    public org.gephi.graph.api.DirectedGraph getGigantSubGraph(GraphModel graphModel) {

        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        //System.out.println("filterController: "+filterController);
        GiantComponentBuilder.GiantComponentFilter giantComponentFilter = new GiantComponentBuilder.GiantComponentFilter();

        Query queryGiantComponent = filterController.createQuery(giantComponentFilter);
        //System.out.println("queryGiantComponent: "+queryGiantComponent);
        GraphView viewGiantComponent = filterController.filter(queryGiantComponent);
        //System.out.println("viewGiantComponent: "+viewGiantComponent);

        //DirectedGraph giantSubGraph = (DirectedGraph) graphModel.getDirectedGraph(viewGiantComponent);
        //System.out.println("[getGigantSubGraph] Nodes in the giant component: " + giantSubGraph.getVertices().toArray().length);
        org.gephi.graph.api.DirectedGraph giantSubGraph = graphModel.getDirectedGraph(viewGiantComponent);
        //System.out.println("[getGigantSubGraph] Nodes in the giant component: " + giantSubGraph.getNodes().toArray().length);
        graphModel.setVisibleView(viewGiantComponent);
        //return (giantSubGraph);
        return giantSubGraph;

    }

    // NODE LEVEL PROPERTIES ----------------------------------------------------------------- v
    public void nodeLevelProperties() throws IOException {

        topTenDegreeNodes((org.gephi.graph.api.DirectedGraph)completeGraph);
        topTenInDegreeNodes((org.gephi.graph.api.DirectedGraph)completeGraph);
        topTenOutDegreeNodes((org.gephi.graph.api.DirectedGraph)completeGraph);
        topTenClosenessAndEccentricityAndBetweennessNodes((org.gephi.graph.api.DirectedGraph)completeGraph); // Metric ??

        try {
            topTenPageRankNodes((org.gephi.graph.api.DirectedGraph) completeGraph);
        }catch(Exception e_PageRankNodes){
            System.out.println("ERROR DURING PAGE RANK NODES: "+e_PageRankNodes);
            //// message to VisualAgent
            try {
                stats_message = new ACLMessage(ACLMessage.INFORM);
                stats_message.setSender(this.getAid());
                stats_message.addReceiver(new AgentID("AgentVisual"));
                stats_message.setContent("\nERROR DURING PAGE RANK NODES: "+e_PageRankNodes);
                stats_message.setOntology("Show this! - from GraphAnalyzer");
                this.send(stats_message);  //---->(Visual agent)

            }catch(Exception e_mes_va){
                System.out.println("Error sending message to VisualAgent: "+e_mes_va);
            }
            //// end
        }
        try {
            topTenEigenvectorCentralityNodes((org.gephi.graph.api.DirectedGraph) completeGraph);
        }catch(Exception e_CentralityNodes){
            System.out.println("ERROR DURING GETTING CENTRALITY NODES: "+e_CentralityNodes);
            //// message to VisualAgent
            try {
                stats_message = new ACLMessage(ACLMessage.INFORM);
                stats_message.setSender(this.getAid());
                stats_message.addReceiver(new AgentID("AgentVisual"));
                stats_message.setContent("\nERROR DURING GETTING CENTRALITY NODES: "+e_CentralityNodes);
                stats_message.setOntology("Show this! - from GraphAnalyzer");
                this.send(stats_message);  //---->(Visual agent)

            }catch(Exception e_mes_va){
                System.out.println("Error sending message to VisualAgent: "+e_mes_va);
            }
            //// end
        }
    }

    /*
    * Java method to sort Map in Java by value e.g. HashMap or Hashtable
    * throw NullPointerException if Map contains null values
    * It also sort values even if they are duplicates
    */
    //public <K extends Comparable, V extends Comparable> Map<K, V> sortByValues(Map<K, V> map) {
    public <K extends Comparable, V extends Comparable> Map<K, V> sortByValues(Map<K, V> map) {
        List<Map.Entry<K, V>> entries = new LinkedList<Map.Entry<K, V>>(map.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K, V> sortedMap = new LinkedHashMap<K, V>();

        for (Map.Entry<K, V> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public ArrayList topTenDegreeNodes(org.gephi.graph.api.DirectedGraph graph) throws IOException {

        //Map<Integer, NodeIterable> nodesDegree = new HashMap<>();
        Map<Integer, Integer> nodesDegree = new HashMap<>();

        for (int i = 1; i <= graph.getNodeCount(); i++) {
            //System.out.println("graph.getNode("+i+"): "+graph.getNode(i));
            //nodesDegree.put(i, graph.getNeighbors(graph.getNode(i)));
            nodesDegree.put(i, graph.getNeighbors(graph.getNode(i)).toArray().length);
        }

        //printing hashtable without sorting
        //System.out.println("Unsorted Map in Java : " + nodesDegree);

        //sorting Map like Hashtable and HashMap by values in Java
        //org.gephi.graph.api.NodeIterable
        //Map<Integer, NodeIterable> nodesDegreeOrdered = sortByValues(nodesDegree);
        Map<Integer, Integer> nodesDegreeOrdered = sortByValues(nodesDegree);
        System.out.println("Sorted Map in Java by values: " + nodesDegreeOrdered);



        //TOP TEN
        String topTenFile = "Statistics/#"+hashtag+"/TopTenDegreeTracePerNode_"+hashtag+"_"+graph.getNodeCount()+".txt"; //hashtag+tamaño_muestra
        FileWriter fwTopTenFile = new FileWriter(topTenFile);
        PrintWriter pwTopTenFile = new PrintWriter(fwTopTenFile, true);


        pwTopTenFile.print("(");
        int i = 0;
        ArrayList topTen = new ArrayList();

        //System.out.println("TOP TEN DEGREE NODES!");
        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\n\n----- TOP TEN DEGREE NODES -----");
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end

        //Iterator it = nodesDegree.keySet().iterator();
        Iterator it = nodesDegreeOrdered.keySet().iterator();
        while (it.hasNext() && i < 10) {
            int id = (Integer) it.next();
            topTen.add(id);
            pwTopTenFile.print("[\"" + id + "\"]=\"" + graph.getNode(id).getNodeData().getLabel() + "\" ");
            //System.out.println("id: " + id + " Label: " + graph.getNode(id).getNodeData().getLabel() + " Degree:" + graph.getNeighbors(graph.getNode(id)).toArray().length);
            //// message to VisualAgent
            try {
                stats_message = new ACLMessage(ACLMessage.INFORM);
                stats_message.setSender(this.getAid());
                stats_message.addReceiver(new AgentID("AgentVisual"));
                stats_message.setContent("\nid: " + id + " Label: " + graph.getNode(id).getNodeData().getLabel() + " Degree:" + graph.getNeighbors(graph.getNode(id)).toArray().length);
                stats_message.setOntology("Show this! - from GraphAnalyzer");
                this.send(stats_message);  //---->(Visual agent)

            }catch(Exception e_mes_va){
                System.out.println("Error sending message to VisualAgent: "+e_mes_va);
            }
            //// end

            i++;
        }
        pwTopTenFile.print(")\n");
        pwTopTenFile.close();

        return topTen;
    }


    public ArrayList topTenInDegreeNodes (org.gephi.graph.api.DirectedGraph graph) throws IOException {

        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\n\n------ TOP TEN IN DEGREE NODES ------");
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end

        //TOP TEN
        String topTenFile = "Statistics/#"+hashtag+"/TopTenInDegreeTracePerNode_"+hashtag+"_"+graph.getNodeCount()+".txt";
        FileWriter fwTopTenFile = new FileWriter(topTenFile);
        PrintWriter pwTopTenFile = new PrintWriter(fwTopTenFile, true);


        Map<Integer, Integer> nodesDegree = new HashMap<Integer, Integer>();

        for (int i = 1; i <= graph.getNodeCount(); i++) {
            nodesDegree.put(i, graph.getInDegree(graph.getNode(i)));
        }

        //printing hashtable without sorting
        //System.out.println("Unsorted Map in Java : " + nodesDegree);

        //sorting Map like Hashtable and HashMap by values in Java
        Map<Integer, Integer> nodesInDegreeOrdered = sortByValues(nodesDegree);

        pwTopTenFile.print("(");
        int i = 0;
        ArrayList topTen = new ArrayList();

        //System.out.println("TOP TEN IN DEGREE NODES!");
        Iterator it = nodesInDegreeOrdered.keySet().iterator();
        while (it.hasNext() && i < 10) {
            int id = (Integer) it.next();
            topTen.add(id);
            pwTopTenFile.print("[\"" + id + "\"]=\"" + graph.getNode(id).getNodeData().getLabel() + "\" ");
            //System.out.println("id: " + id + " Label: " + graph.getNode(id).getNodeData().getLabel() + " InDegree:" + graph.getInDegree(graph.getNode(id)) + " OutDegree:" + graph.getOutDegree(graph.getNode(id)));
            //// message to VisualAgent
            try {
                stats_message = new ACLMessage(ACLMessage.INFORM);
                stats_message.setSender(this.getAid());
                stats_message.addReceiver(new AgentID("AgentVisual"));
                stats_message.setContent("\nid: " + id + " Label: " + graph.getNode(id).getNodeData().getLabel() + " InDegree:" + graph.getInDegree(graph.getNode(id)) + " OutDegree:" + graph.getOutDegree(graph.getNode(id)));
                stats_message.setOntology("Show this! - from GraphAnalyzer");
                this.send(stats_message);  //---->(Visual agent)

            }catch(Exception e_mes_va){
                System.out.println("Error sending message to VisualAgent: "+e_mes_va);
            }
            //// end

            i++;
        }
        pwTopTenFile.print(")\n");
        pwTopTenFile.close();

        return topTen;
    }


    public ArrayList topTenOutDegreeNodes (org.gephi.graph.api.DirectedGraph graph) throws IOException {

        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\n\n------ TOP TEN OUT DEGREE NODES -----");
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end

        //TOP TEN
        String topTenFile = "Statistics/#"+hashtag+"/TopTenOutDegreeTracePerNode_"+hashtag+"_"+graph.getNodeCount()+".txt";
        FileWriter fwTopTenFile = new FileWriter(topTenFile);
        PrintWriter pwTopTenFile = new PrintWriter(fwTopTenFile, true);


        Map<Integer, Integer> nodesDegree = new HashMap<Integer, Integer>();

        for (int i = 1; i <= graph.getNodeCount(); i++) {
            nodesDegree.put(i, graph.getOutDegree(graph.getNode(i)));
        }

        //printing hashtable without sorting
        //System.out.println("Unsorted Map in Java : " + nodesDegree);

        //sorting Map like Hashtable and HashMap by values in Java
        Map<Integer, Integer> nodesOutDegreeOrdered = sortByValues(nodesDegree);

        pwTopTenFile.print("(");
        int i = 0;
        ArrayList topTen = new ArrayList();

        //System.out.println("TOP TEN IN DEGREE NODES!");
        Iterator it = nodesOutDegreeOrdered.keySet().iterator();
        while (it.hasNext() && i < 10) {
            int id = (Integer) it.next();
            topTen.add(id);
            pwTopTenFile.print("[\"" + id + "\"]=\"" + graph.getNode(id).getNodeData().getLabel() + "\" ");
            //System.out.println("id: " + id + " Label: " + graph.getNode(id).getNodeData().getLabel() + " InDegree:" + graph.getInDegree(graph.getNode(id)) + " OutDegree:" + graph.getOutDegree(graph.getNode(id)));
            //// message to VisualAgent
            try {
                stats_message = new ACLMessage(ACLMessage.INFORM);
                stats_message.setSender(this.getAid());
                stats_message.addReceiver(new AgentID("AgentVisual"));
                stats_message.setContent("\nid: " + id + " Label: " + graph.getNode(id).getNodeData().getLabel() + " InDegree:" + graph.getInDegree(graph.getNode(id)) + " OutDegree:" + graph.getOutDegree(graph.getNode(id)));
                stats_message.setOntology("Show this! - from GraphAnalyzer");
                this.send(stats_message);  //---->(Visual agent)

            }catch(Exception e_mes_va){
                System.out.println("Error sending message to VisualAgent: "+e_mes_va);
            }
            //// end

            i++;
        }
        pwTopTenFile.print(")\n");
        pwTopTenFile.close();

        return topTen;
    }

    public static <K extends Comparable, V extends Comparable> Map<K, Metric> sortByValuesMetric(Map<K, Metric> map) {
        List<Map.Entry<K, Metric>> entries = new LinkedList<Map.Entry<K, Metric>>(map.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<K, Metric>>() {
            @Override
            public int compare(Map.Entry<K, Metric> o1, Map.Entry<K, Metric> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K, Metric> sortedMap = new LinkedHashMap<K, Metric>();

        for (Map.Entry<K, Metric> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public void topTenClosenessAndEccentricityAndBetweennessNodes(org.gephi.graph.api.DirectedGraph graph) throws IOException {

        System.out.println("TOP TEN CLOSENESS AND ECCENTRICITY NODES!");

        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\n\n------ TOP TEN CLOSENESS AND ECCENTRICITY NODES -----");
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end


        //Node[] nodeArray = giantSubGraph.getNodes().toArray();
        //System.out.println("[topTenClosenessNodes] giantSubGraph.node "+ giantSubGraph.getNodes().toArray().length);
        //System.out.println("[topTenClosenessNodes] graph.nodes: "+ graph.getNodes().toArray().length);

        //UndirectedGraph giantSubGraph = getGigantSubGraph(graph.getGraphModel());
        GraphDistance graphDistance = new GraphDistance();
        //graphDistance.setDirected(directed);
        graphDistance.setNormalized(false);
        System.out.println("Antes execute");
        graphDistance.execute(graph.getGraphModel(), attributeModel);
        System.out.println("Dps execute");

        Map<Integer, Metric> nodesCloseness = new HashMap<Integer, Metric>();
        Map<Integer, Metric> nodesEccentricity = new HashMap<Integer, Metric>();
        Map<Integer, Double> nodesBetweenness = new HashMap<Integer, Double>();

        Node[] nodeArray = graph.getNodes().toArray();
        for (int i = 0; i < (graph.getNodes().toArray().length); i++) {
            //Node node = (Node)it.next();
            //System.out.println("dps de node");
            Node node = nodeArray[i];
            //System.out.println("localBetweenness[i]:" +myGraphDistance.getBetweenness()[i]);
            //System.out.print("node.getId(): " + node.getId());
            //System.out.println(" closeness: "+myGraphDistance.getCloseness()[i]);
            nodesCloseness.put(node.getId(), new Metric((Double) node.getAttributes().getValue("Closeness Centrality"), (double) graph.getNeighbors(graph.getNode(node.getId())).toArray().length));

        }


        for (int i = 0; i < (graph.getNodes().toArray().length); i++) {
            Node node = nodeArray[i];
            nodesEccentricity.put(node.getId(), new Metric((Double) node.getAttributes().getValue("Eccentricity"), (double) graph.getNeighbors(graph.getNode(node.getId())).toArray().length));
        }

        for (int i = 0; i < (graph.getNodes().toArray().length); i++) {
            Node node = nodeArray[i];
            nodesBetweenness.put(node.getId(), (Double) node.getAttributes().getValue("Betweenness Centrality"));
        }


        //printing hashtable without sorting
        //System.out.println("Unsorted Map in Java : " + nodesCloseness);

        //sorting Map like Hashtable and HashMap by values in Java
        Map<Integer, Metric> nodesClosenessOrdered = sortByValuesMetric(nodesCloseness);
        Map<Integer, Metric> nodesEccentricityOrdered = sortByValuesMetric(nodesEccentricity);

        //sorting Map like Hashtable and HashMap by values in Java
        Map<Integer, Double> nodesBetweenessOrdered = sortByValues(nodesBetweenness);
        //System.out.println("Sorted Map in Java by values: " + nodesClosenessOrdered);

        String topTenFile = "Statistics/#"+hashtag+"/TopTenClosenessTracePerNode_"+hashtag+"_"+graph.getNodeCount()+".txt";
        FileWriter fwTopTenFile = new FileWriter(topTenFile);
        PrintWriter pwTopTenFile = new PrintWriter(fwTopTenFile, true);

        String topTenFileEccentricty = "Statistics/#"+hashtag+"/TopTenEccentricityTracePerNode_"+hashtag+"_"+graph.getNodeCount()+".txt";
        FileWriter fwTopTenFileEccentricity = new FileWriter(topTenFileEccentricty);
        PrintWriter pwTopTenFileEccentricity = new PrintWriter(fwTopTenFileEccentricity, true);

        String topTenFileBetweenness = "Statistics/#"+hashtag+"/TopTenBetweennessTracePerNode_"+hashtag+"_"+graph.getNodeCount()+".txt";
        FileWriter fwTopTenFileBetweenness = new FileWriter(topTenFileBetweenness);
        PrintWriter pwTopTenFileBetweenness = new PrintWriter(fwTopTenFileBetweenness, true);


        pwTopTenFile.print("(");
        pwTopTenFileEccentricity.print("(");
        pwTopTenFileBetweenness.print("(");
        int i = 0;
        ArrayList topTenClosseness = new ArrayList();
        ArrayList topTenEccentricity = new ArrayList();
        ArrayList topTenBetweenness = new ArrayList();



        Integer[] keysClosseness = new Integer[nodesClosenessOrdered.size()];
        int index = 0;
        for (Map.Entry<Integer, Metric> mapEntry : nodesClosenessOrdered.entrySet()) {
            keysClosseness[index] = mapEntry.getKey();
            index++;
        }

        Integer[] keysEccentricity = new Integer[nodesEccentricityOrdered.size()];
        index = 0;
        for (Map.Entry<Integer, Metric> mapEntry : nodesEccentricityOrdered.entrySet()) {
            keysEccentricity[index] = mapEntry.getKey();
            index++;
        }

        System.out.println("TOP TEN CLOSENESS!");
        int i_start = nodesClosenessOrdered.keySet().toArray().length - 1;
        int numIterations = 10;

        while ((numIterations > 0) && (i_start > 0)) {
            if ((nodesCloseness.get(keysClosseness[i_start]).getM() > 0)
                    && (nodesGiantComponent.containsKey(graph.getNode(keysClosseness[i_start]).getNodeData().getLabel()))) {

                topTenClosseness.add(keysClosseness[i_start]);
                pwTopTenFile.print("[\"" + keysClosseness[i_start] + "\"]=\"" + graph.getNode(keysClosseness[i_start]).getNodeData().getLabel() + "\" ");
                //// message to VisualAgent
                try {
                    stats_message = new ACLMessage(ACLMessage.INFORM);
                    stats_message.setSender(this.getAid());
                    stats_message.addReceiver(new AgentID("AgentVisual"));
                    stats_message.setContent("\nid: " + keysClosseness[i_start] + " Label: " + graph.getNode(keysClosseness[i_start]).getNodeData().getLabel() + " Closeness:" + nodesCloseness.get(keysClosseness[i_start]).getM() + " Degree: " + graph.getNeighbors(graph.getNode(keysClosseness[i_start])).toArray().length + " Component: " + (Integer) graph.getNode(keysClosseness[i_start]).getAttributes().getValue("Component ID"));
                    stats_message.setOntology("Show this! - from GraphAnalyzer");
                    this.send(stats_message);  //---->(Visual agent)

                }catch(Exception e_mes_va){
                    System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                }
                //// end
                System.out.println("id: " + keysClosseness[i_start] + " Label: " + graph.getNode(keysClosseness[i_start]).getNodeData().getLabel() + " Closeness:" + nodesCloseness.get(keysClosseness[i_start]).getM() + " Degree: " + graph.getNeighbors(graph.getNode(keysClosseness[i_start])).toArray().length + " Component: " + (Integer) graph.getNode(keysClosseness[i_start]).getAttributes().getValue("Component ID"));
                numIterations--;

            }
            i_start--;
        }

        System.out.println("TOP TEN ECCENTRICITY!");
        i_start = nodesEccentricityOrdered.keySet().toArray().length - 1;
        numIterations = 20;

        while ((numIterations > 0) && (i_start > 0)) {
            if ((nodesEccentricity.get(keysEccentricity[i_start]).getM() > 0)
                    && (nodesGiantComponent.containsKey(graph.getNode(keysEccentricity[i_start]).getNodeData().getLabel()))) {
                topTenEccentricity.add(keysEccentricity[i_start]);

                pwTopTenFileEccentricity.print("[\"" + keysEccentricity[i_start] + "\"]=\"" + graph.getNode(keysEccentricity[i_start]).getNodeData().getLabel() + "\" ");
                //// message to VisualAgent
                try {
                    stats_message = new ACLMessage(ACLMessage.INFORM);
                    stats_message.setSender(this.getAid());
                    stats_message.addReceiver(new AgentID("AgentVisual"));
                    stats_message.setContent("\nid: " + keysEccentricity[i_start] + " Label: " + graph.getNode(keysEccentricity[i_start]).getNodeData().getLabel() + " Eccentricity:" + nodesEccentricity.get(keysEccentricity[i_start]).getM() + " Degree:" + graph.getNeighbors(graph.getNode(keysEccentricity[i_start])).toArray().length + " Component: " + (Integer) graph.getNode(keysEccentricity[i_start]).getAttributes().getValue("Component ID"));
                    stats_message.setOntology("Show this! - from GraphAnalyzer");
                    this.send(stats_message);  //---->(Visual agent)

                }catch(Exception e_mes_va){
                    System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                }
                //// end
                System.out.println("id: " + keysEccentricity[i_start] + " Label: " + graph.getNode(keysEccentricity[i_start]).getNodeData().getLabel() + " Eccentricity:" + nodesEccentricity.get(keysEccentricity[i_start]).getM() + " Degree:" + graph.getNeighbors(graph.getNode(keysEccentricity[i_start])).toArray().length + " Component: " + (Integer) graph.getNode(keysEccentricity[i_start]).getAttributes().getValue("Component ID"));
                numIterations--;



            } //else {
            //System.out.println("NO VALIDO id: " + keysEccentricity[i_start] + " Label: " + giantSubGraph.getNode(keysEccentricity[i_start]).getNodeData().getLabel() + " Eccentricity:" + nodesEccentricity.get(keysEccentricity[i_start]).getM() + " Component: " + (Integer)giantSubGraph.getNode(keysEccentricity[i_start]).getAttributes().getValue("Component ID"));
            //}
            i_start--;
        }


        System.out.println("TOP TEN BETWEENNESS!");
        Iterator it = nodesBetweenessOrdered.keySet().iterator();
        while (it.hasNext() && i < 10) {

            int id = (Integer) it.next();
            if (nodesGiantComponent.containsKey(graph.getNode(id).getNodeData().getLabel())) {
                topTenBetweenness.add(id);
                pwTopTenFileBetweenness.print("[\"" + id + "\"]=\"" + graph.getNode(id).getNodeData().getLabel() + "\" ");
                //// message to VisualAgent
                try {
                    stats_message = new ACLMessage(ACLMessage.INFORM);
                    stats_message.setSender(this.getAid());
                    stats_message.addReceiver(new AgentID("AgentVisual"));
                    stats_message.setContent("\nid: " + id + " Label: " + graph.getNode(id).getNodeData().getLabel() + " Betweenness:" + (Double) graph.getNode(id).getAttributes().getValue("Betweenness Centrality"));
                    stats_message.setOntology("Show this! - from GraphAnalyzer");
                    this.send(stats_message);  //---->(Visual agent)

                }catch(Exception e_mes_va){
                    System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                }
                //// end
                System.out.println("id: " + id + " Label: " + graph.getNode(id).getNodeData().getLabel() + " Betweenness:" + (Double) graph.getNode(id).getAttributes().getValue("Betweenness Centrality"));
                i++;
            }
        }
//        Iterator it = nodesClosenessOrdered.keySet().iterator();
//         while (it.hasNext() && i < 10) {
//         int id = (Integer) it.next();
//         topTen.add(id);
//         pwTopTenFile.print("[\"" + id + "\"]=\"" + graph.getNode(id).getNodeData().getLabel() + "\" ");
//         //System.out.println("id: " + id + " Label: " + graph.getNode(id).getNodeData().getLabel() + " Closeness:" + localCloseness[id]);
//         i++;
//         }

        pwTopTenFile.print(")\n");
        pwTopTenFile.close();

        pwTopTenFileEccentricity.print(")\n");
        pwTopTenFileEccentricity.close();

        pwTopTenFileBetweenness.print(")\n");
        pwTopTenFileBetweenness.close();


    }


    public ArrayList topTenPageRankNodes(org.gephi.graph.api.DirectedGraph graph) throws IOException {

        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\n\n------ TOP TEN PAGE RANK NODES ------");
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end

        try {
            PageRank myPageRank = new PageRank();
            //myPageRank.setDirected(directed);
            myPageRank.setEpsilon(0.0010);
            myPageRank.setProbability(0.85);
            myPageRank.execute(graphModel, attributeModel);
        }catch(Exception e_page_rank){
            System.out.println("Error executing myPageRank");
        }

        //TOP TEN
        //System.out.println("TOP TEN PAGERANK NODES!");

        Map<Integer, Double> nodesPageRank = new HashMap<Integer, Double>();
        Node[] nodeArray = graph.getNodes().toArray();
        for (int i = 0; i < graph.getNodeCount(); i++) {

            Node node = nodeArray[i];


            nodesPageRank.put(node.getId(), (Double) node.getAttributes().getValue("PageRank"));
        }

        //sorting Map like Hashtable and HashMap by values in Java
        Map<Integer, Double> nodesPageRankOrdered = sortByValues(nodesPageRank);

        //System.out.println("Sorted Map in Java by values: " + nodesPageRank);

        String topTenFile = "Statistics/#"+hashtag+"/TopTenPageRankTracePerNode_"+hashtag+"_"+graph.getNodeCount()+".txt";
        FileWriter fwTopTenFile = new FileWriter(topTenFile);
        PrintWriter pwTopTenFile = new PrintWriter(fwTopTenFile, true);


        pwTopTenFile.print("(");
        int i = 0;
        ArrayList topTen = new ArrayList();

        //System.out.println("TOP TEN PAGERANK NODES!");
        Iterator it = nodesPageRankOrdered.keySet().iterator();
        while (it.hasNext() && i < 10) {
            int id = (Integer) it.next();
            if (nodesGiantComponent.containsKey(graph.getNode(id).getNodeData().getLabel())) {
                topTen.add(id);
                if (graph.getNode(id) != null) {
                    pwTopTenFile.print("[\"" + id + "\"]=\"" + graph.getNode(id).getNodeData().getLabel() + "\" ");
                    //System.out.println("id: " + id + " Label: " + graph.getNode(id).getNodeData().getLabel() + " PageRank:" + nodesPageRank.get(id) + " Component: " + ((Integer) graph.getNode(id).getAttributes().getValue("Component ID")).intValue());
                    //// message to VisualAgent
                    try {
                        stats_message = new ACLMessage(ACLMessage.INFORM);
                        stats_message.setSender(this.getAid());
                        stats_message.addReceiver(new AgentID("AgentVisual"));
                        stats_message.setContent("\nid: " + id + " Label: " + graph.getNode(id).getNodeData().getLabel() + " PageRank:" + nodesPageRank.get(id) + " Component: " + ((Integer) graph.getNode(id).getAttributes().getValue("Component ID")).intValue());
                        stats_message.setOntology("Show this! - from GraphAnalyzer");
                        this.send(stats_message);  //---->(Visual agent)

                    }catch(Exception e_mes_va){
                        System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                    }
                    //// end

                    i++;
                }
            }
        }
        pwTopTenFile.print(")\n");
        pwTopTenFile.close();

        return topTen;
    }


    public ArrayList topTenEigenvectorCentralityNodes(org.gephi.graph.api.DirectedGraph graph) throws IOException {

        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\n\n------ TOP TEN EIGEN VECTOR CENTRALITY NODES ------");
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end

        //TOP TEN
        try {
            EigenvectorCentrality myEigenvectorCentrality = new EigenvectorCentrality();
            //myEigenvectorCentrality.setDirected(directed);
            //myEigenvectorCentrality.setNumRuns(100);
            myEigenvectorCentrality.setNumRuns(3);
            myEigenvectorCentrality.execute(graphModel, attributeModel);
        }catch(Exception e_centr){
            System.out.println("Error executing centrality");
        }

        //System.out.println("TOP TEN EIGENVECTOR CENTRALITY NODES!");

        Map<Integer, Double> nodesEigenvectorCentrality = new HashMap<Integer, Double>();

        Node[] nodeArray = graph.getNodes().toArray();
        for (int i = 0; i < graph.getNodeCount(); i++) {

            Node node = nodeArray[i];
            nodesEigenvectorCentrality.put(node.getId(), (Double) node.getAttributes().getValue("Eigenvector Centrality"));
        }

        //System.out.println("nodesEigenvectorCentrality: "+nodesEigenvectorCentrality.toString());


        //sorting Map like Hashtable and HashMap by values in Java
        Map<Integer, Double> nodesEigenvectorCentralityOrdered = sortByValues(nodesEigenvectorCentrality);



        String topTenFile = "Statistics/#"+hashtag+"/TopTenEigenvectorCentralityTracePerNode_"+hashtag+"_"+graph.getNodeCount()+".txt";
        FileWriter fwTopTenFile = new FileWriter(topTenFile);
        PrintWriter pwTopTenFile = new PrintWriter(fwTopTenFile, true);


        pwTopTenFile.print("(");
        int i = 0;
        ArrayList topTen = new ArrayList();

        Iterator it = nodesEigenvectorCentralityOrdered.keySet().iterator();
        while (it.hasNext() && i < 10) {
            int id = (Integer) it.next();

            if (nodesGiantComponent.containsKey(graph.getNode(id).getNodeData().getLabel())) {

                topTen.add(id);
                //System.out.println("id: " + id + " Label: " + graph.getNode(id).getNodeData().getLabel() + " EigenvectorCentrality:" + (Double) graph.getNode(id).getAttributes().getValue("Eigenvector Centrality") + " Component: " + ((Integer) graph.getNode(id).getAttributes().getValue("Component ID")).intValue());
                pwTopTenFile.print("[\"" + id + "\"]=\"" + graph.getNode(id).getNodeData().getLabel() + "\" ");

                //// message to VisualAgent
                try {
                    stats_message = new ACLMessage(ACLMessage.INFORM);
                    stats_message.setSender(this.getAid());
                    stats_message.addReceiver(new AgentID("AgentVisual"));
                    stats_message.setContent("\nid: " + id + " Label: " + graph.getNode(id).getNodeData().getLabel() + " EigenvectorCentrality:" + (Double) graph.getNode(id).getAttributes().getValue("Eigenvector Centrality") + " Component: " + ((Integer) graph.getNode(id).getAttributes().getValue("Component ID")).intValue());
                    stats_message.setOntology("Show this! - from GraphAnalyzer");
                    this.send(stats_message);  //---->(Visual agent)

                }catch(Exception e_mes_va){
                    System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                }
                //// end

                i++;
            }
        }
        pwTopTenFile.print(")\n");
        pwTopTenFile.close();

        return topTen;
    }

    // -------------------------------- ^


    public void networkLevelProperties(org.gephi.graph.api.DirectedGraph graph) {

        /**
         * *********************
         */
        /*NETWORK LEVEL**********/
        /**
         * *********************
         */

        System.out.println("");
        System.out.println("---------- NETWORK LEVEL -----------");

        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());

        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\n\n---------- NETWORK LEVEL -----------");
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end


        //////////////////////////////////////
        //CLUSTERING
        //////////////////////////////////////
        ClusteringCoefficient iclustering = new ClusteringCoefficient();
        //iclustering.setDirected(directed);
        iclustering.execute(graphModel, attributeModel);
        float clustering = (float) iclustering.getAverageClusteringCoefficient();
        System.out.println("preclustering: "+clustering);
        if (Double.isNaN(clustering)) {
            clustering = 0;
        }
        System.out.println("Clustering: " + clustering);

        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\nClustering: " + clustering);
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end

        //////////////////////////////////////


        //////////////////////////////////////
        //DIAMETER PATH LENGTH
        //////////////////////////////////////
        GraphDistance graphDistance = new GraphDistance();
        //graphDistance.setDirected(directed);
        graphDistance.execute(graphModel, attributeModel);
        float diameter = (float) graphDistance.getDiameter();
        float pathLength = (float) graphDistance.getPathLength();
        if (Double.isNaN(diameter)) {
            diameter = 0;
        }
        if (Double.isNaN(pathLength)) {
            pathLength = 0;
        }

        System.out.println("Diameter: " + diameter);
        System.out.println("PathLength: " + pathLength);

        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\nDiameter: " + diameter+"\nPathLength: " + pathLength);
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end

        //////////////////////////////////////



        //////////////////////////////////////
        //CONNECTED COMPONENTS
        //////////////////////////////////////
        ConnectedComponents connectedComponents = new ConnectedComponents();
        //connectedComponents.setDirected(directed);
        connectedComponents.execute(graphModel, attributeModel);
        float graphComponents = (float) connectedComponents.getConnectedComponentsCount();
        int[] componentsSize = connectedComponents.getComponentsSize();
        int giantComponent = 0;
        if (componentsSize.length != 0) {
            Arrays.sort(componentsSize);
            int index = componentsSize.length - 1;
            //System.out.println("index: " + index);
            giantComponent = componentsSize[index];
            System.out.println("Giant component size: " + componentsSize[index]);

            //// message to VisualAgent
            try {
                stats_message = new ACLMessage(ACLMessage.INFORM);
                stats_message.setSender(this.getAid());
                stats_message.addReceiver(new AgentID("AgentVisual"));
                stats_message.setContent("\nGiant component size: " + componentsSize[index]);
                stats_message.setOntology("Show this! - from GraphAnalyzer");
                this.send(stats_message);  //---->(Visual agent)

            }catch(Exception e_mes_va){
                System.out.println("Error sending message to VisualAgent: "+e_mes_va);
            }
            //// end

        }
        System.out.println("graphComponents: " + graphComponents);

        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\ngraphComponents: " + graphComponents);
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end


        //////////////////////////////////////
        //AVERAGE DEGREE
        //////////////////////////////////////
        Degree degree = new Degree();

        degree.execute(graphModel, attributeModel);
        double avDegree = degree.getAverageDegree();
        if (Double.isNaN(avDegree)) {
            avDegree = 0;
        }
        System.out.println("avDegree: " + avDegree);

        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\navDegree: " + avDegree);
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end


        //////////////////////////////////////
        //MODULARITY
        //////////////////////////////////////
        Modularity modularity = new Modularity();

        modularity.execute(graphModel, attributeModel);
        double modul = modularity.getModularity();
        if (Double.isNaN(modul)) {
            modul = 0;
        }
        System.out.println("modularity: " + modul);

        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\nmodularity: " + modul);
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end


        //////////////////////////////////////
        //GRAHP DENSITY
        //////////////////////////////////////
        GraphDensity graphDensity = new GraphDensity();
        //graphDensity.setDirected(directed);
        graphDensity.execute(graphModel, attributeModel);
        double dens = graphDensity.getDensity();
        if (Double.isNaN(dens)) {
            dens = 0;
        }
        System.out.println("Density: " + dens);

        //// message to VisualAgent
        try {
            stats_message = new ACLMessage(ACLMessage.INFORM);
            stats_message.setSender(this.getAid());
            stats_message.addReceiver(new AgentID("AgentVisual"));
            stats_message.setContent("\nDensity: " + dens);
            stats_message.setOntology("Show this! - from GraphAnalyzer");
            this.send(stats_message);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end

        System.out.println("------------------------------------");


        //////////////////////////////////////
        //FILE FOR NETWORK LEVEL METRICS
        //////////////////////////////////////
        //#timestamp    nodes    edges    clustering    diameter    pathLength    graphComponents    pageRank     avDegree    modularity  Density
        /*pwFile.println(
                "nodes: " + nodes
                        + "\nedges: " + edges
                        + "\nclustering: " + clustering
                        + "\ndiameter: " + diameter
                        + "\npathLength: " + pathLength
                        + "\ngraphComponents: " + graphComponents
                        + "\navDegree: " + avDegree
                        + "\nmodularity: " + modul
                        + "\ndensity: " + dens
                        + "\ngiantComponent: " + giantComponent);*/

    }



    //---------------------------------------

    public void execute(){


        System.out.println("\nHi! Graph Analyzer Agent is ready to analyze\n");

        /*try {
            System.out.println("\nTaking a tiny rest... (GraphAnalyzerAgent) \n");
            Thread.sleep(65000);                 //1000 milliseconds is one second. 8000 | 30000 | 55000
        } catch(InterruptedException ex) {
            System.out.println("\nError while sleeping (GraphAnalyzerAgent)\n");
            Thread.currentThread().interrupt();
        }

        System.out.println("\nI woke up! :D (GraphAnalyzerAgent)\n");*/


        do{
            System.out.println("Waiting data...");
        }while(!start_now);

        System.out.println("\nLoading Graph...\n");


            try {

                fromGraphML2graph(graphFile);


                //System.out.println("\nEstamos dentro...\n");

            /*vertexFactory = new VertexFactory();
            edgeFactory = new EdgeFactory();*/



            /*PajekNetReader<UndirectedGraph<Node, Edge>, Node, Edge> pnr =
                  new PajekNetReader<UndirectedGraph<Node, Edge>, Node, Edge>(vertexFactory, edgeFactory);
            //PajekNetReader pnr = new PajekNetReader(vertexFactory, edgeFactory);
            //PajekNetReader pnr = new PajekNetReader(FactoryUtils.instantiateFactory(Object.class));

            System.out.println("\n---> pnr: "+pnr.toString()+"\n");


            final UndirectedGraph graph = new UndirectedSparseMultigraph();
            //graph.addEdge("edge1", "vertice1", "vertice2");
            //Graph<Number,Number> graph = new SparseMultigraph<>();
            //Graph graph = new SparseMultigraph<Node,Edge>();
            //Graph graph = new UndirectedSparseGraph(); //no tiene que ser nulo
            System.out.println("\n---> graph: "+graph.toString()+"\n");


            try {

                pnr.load("pajekFile.net", graph);
                System.out.println("\nGRAFO PAJEK contains: " + graph + "\n-------------------------------\n");
            }catch(Exception e){
                System.out.println("\nERROR CREANDO EL PAJEKREADER: "+e+"\n");
            }*/






            /*GraphMLReader<DirectedGraph<Node, Edge>, Node, Edge> gmlr =
                    new GraphMLReader<DirectedGraph<Node, Edge>, Node, Edge>(vertexFactory, edgeFactory);
            System.out.println("\n---> gmlr: "+gmlr.toString()+"\n");


            //final DirectedGraph graph = new DirectedSparseMultigraph();
            final DirectedGraph<Node, Edge> graph = new DirectedSparseMultigraph<Node, Edge>();
            //System.out.println("\n---> graph: "+graph.toString()+"\n");

            gmlr.load("/home/carou/IdeaProjects/smatwitter/jungFile.graphml", graph);
            System.out.println("\nGRAFO GRAPHML contains: " + graph.toString() + "\n-------------------------------\n");*/





            /*System.out.println("\n---> getDest(): " + g.getDest(g.getVertices()) + "\n");
            System.out.println("\n---> getEndPoints(): " + g.getEndpoints(g.getEdges()) + "\n");
            System.out.println("\n---> getInEdges(): " + g.getInEdges(g.getVertices() + "\n"));
            System.out.println("\n---> getOpposite(): " + g.getOpposite(g.getVertices(), g.getEdges()) + "\n");
            System.out.println("\n---> getOutEdges(): " + g.getOutEdges(g.getVertices()) + "\n");
            System.out.println("\n---> getPredecessorCount(): " + g.getPredecessorCount(g.getVertices()) + "\n");
            System.out.println("\n---> getPredecessors(): "+g.getPredecessors(g.getVertices()) +"\n");
            System.out.println("\n---> getSource(): "+g.getSource(g.getEdges()));
            System.out.println("\n---> getSuccessorCount(): "+g.getSuccessorCount(g.getVertices()) +"\n");
            System.out.println("\n---> getSuccessors(): "+g.getSuccessors(g.getVertices()) +"\n");
            System.out.println("\n---> number of incoming edges incident to vertex("+g.getVertices()+"): "+g.inDegree(g.getVertices()) +"\n");
            System.out.println("\n---> true if vertex ("+g.getVertices()+") is the destination of edge ("+g.getEdges()+"): "+g.isDest(g.getVertices(), g.getEdges()) +"\n");
            System.out.println("\n---> true if vertex ("+g.getVertices()+") is the source of edge ("+g.getEdges()+"): "+g.isSource(g.getVertices(), g.getEdges()) +"\n");
            System.out.println("\n---> number of outgoing edges incident to vertex ("+g.getVertices()+"): "+g.outDegree(g.getVertices()) +"\n");*/

            } catch (Exception e) {
                System.out.println("\n ERROR A LA HORA DE OBTENER DATOS: " + e + "\n");
            }
        /*catch (IOException e) {
            System.out.println("\n ERROR A LA HORA DE OBTENER DATOS: " + e + "n");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();*/
            //}

        /*GraphMLReader<DirectedGraph<node, edge>, node, edge> gmlr =
                new GraphMLReader<DirectedGraph<node, edge>, node, edge>(new VertexFactory(), new EdgeFactory());

        final DirectedGraph<node, edge> graph = new DirectedSparseMultigraph<node, edge>();

        gmlr.load(filename, graph);*/




    }
}
