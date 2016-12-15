package smapackage;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.BaseAgent;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by carma12
 * QueryNeo4jAgent: Creates a query for a event between a t_ini and a t_end. Also a graphml file is created.
 *
 */
public class QueryNeo4jAgent extends BaseAgent {

    static String hashtag = "";
    static String DB_PATH = ""; //final
    static boolean start_now = false;
    //List<String> content_message = new ArrayList<String>();
    String content_message[] = {};

    //GraphDatabaseService db;
    String rows = "";

    //FileWriter pajekNodes = new FileWriter ("pajekNodes.txt");
    //FileWriter pajekEdges = new FileWriter ("pajekEdges.txt");


    static String t_s = ""; //"2016-07-01 00:00:00.0"; //yyyy-MM-dd HH:mm:ss.S
    static Timestamp time_start_ts;
    static long time_start; //el de menor valor (fecha anterior)
    static String t_e = ""; //"2016-07-05 00:00:00.0";
    static Timestamp time_end_ts;
    static long time_end; //el de mayor valor (fecha posterior)


    //String event = ""; //#NoTeLoPerdonareJamasCarmena
    //FileWriter pajekFile = new FileWriter ("pajekFile.txt");
    static FileWriter GMLGraph;
    PrintWriter pw = null;
    int cont_nodes;

    //Map<Integer, String> lista_nodos = new HashMap<Integer, String>();
    List<String> listaNodos = new ArrayList<String>();
    List<String> listaEdges = new ArrayList<String>();
    List<String> edgeElements = new ArrayList<String>();

    public QueryNeo4jAgent (AgentID aid) throws Exception {
        super(aid);
    }

    public void onMessage(ACLMessage msg_q){
        System.out.println("(QueryNeo4jAgent)-> Mensaje recibido");
        if (msg_q.getOntology().equals("spreading hashtag")){
            System.out.println("(QueryNeo4jAgent)-> Obteniendo datos...");
            System.out.println("content: "+msg_q.getContent());
            String split_char = "\\s+";

            content_message = msg_q.getContent().split(split_char); // Content = hashtag $ date_ini $ date_end
            hashtag = content_message[0];
            t_s = content_message[1]; //+" 00:00:00.0";
            t_e = content_message[2]; //+" 00:00:00.0";
            System.out.println("----> Hashtag: "+hashtag+" | t_s: "+t_s+" | t_e: "+t_e);

            DB_PATH = "/home/carou/tests_sin_hashs/"+hashtag+"";
            System.out.println("DB_PATH: "+DB_PATH);
            try {
                GMLGraph = new FileWriter (""+hashtag+".graphml");
                System.out.println("GMLGraph: "+hashtag+".graphml");
            } catch (IOException e) {
                e.printStackTrace();
            }
            start_now = true;
        }
    }

    public void execute(){

        ACLMessage end_neo4j_agent = new ACLMessage(ACLMessage.INFORM);

        //// message to VisualAgent
        try {
            //ACLMessage query_message = new ACLMessage(ACLMessage.INFORM);
            end_neo4j_agent = new ACLMessage(ACLMessage.INFORM);
            end_neo4j_agent.setSender(this.getAid());
            end_neo4j_agent.addReceiver(new AgentID("AgentVisual"));
            end_neo4j_agent.setContent("\n--> GENERATING GRAPH... \n");
            end_neo4j_agent.setOntology("Show this! - from QueryNeo4J");
            this.send(end_neo4j_agent);  //---->(Visual agent)

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }
        //// end

        System.out.println("start_now: "+start_now);
        do{
            System.out.println("datos? ");
        }while(!start_now);//wait until hashtag and dates inputs appears
        System.out.println("start_now: "+start_now);
        System.out.println("Tenemos datos! :D");

        System.out.println("----> Hashtag: "+hashtag+" | t_s: "+t_s+" | t_e: "+t_e);
            //start_now == true
            if (!hashtag.equals("") && !t_s.equals("") && !t_e.equals("")) {

                System.out.println("\nHello! Agent QueryNeo4j broadcasting news ;P\n");

                //First, take a rest :) (for giving time to the other agents to make things work)
            try {
                System.out.println("\nTaking a tiny rest... \n");
                Thread.sleep(6000);                 //1000 milliseconds is one second. 8000 | 21000 | 35000
            } catch(InterruptedException ex) {
                System.out.println("\nError while sleeping\n");
                Thread.currentThread().interrupt();
            }
            System.out.println("\nI woke up! :D\n");



            try{
                Neo4jAgent.db = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
                registerShutdownHook(Neo4jAgent.db);
                System.out.println("\nDB CREATED CORRECTLY (QueryNeo4jAgent)\n");

            }catch(Exception e){
                System.out.println("\nCANNOT CREATE/ACCESS TO THE DB: "+e+"\n");
            }



                //Preparamos el formato TimeStamp
                try {
                    System.out.println("Parsing data...");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH); // HH:mm:ss.S
                    Date parsedDate_start = dateFormat.parse(t_s);
                    time_start_ts = new java.sql.Timestamp(parsedDate_start.getTime());
                    time_start = time_start_ts.getTime();
                    //String string_time_start = new SimpleDateFormat("yyyy-MM-dd").format(time_start);

                    //string_time_start = string_time_start.substring(0, string_time_start.length()-3);

                    System.out.println("parsedDate_start: "+parsedDate_start);
                    System.out.println("time_start_ts: "+time_start_ts);
                    System.out.println("time_start: "+time_start);
                    //System.out.println("---> string_time_start: "+string_time_start);

                    Date parsedDate_end = dateFormat.parse(t_e);
                    time_end_ts = new java.sql.Timestamp(parsedDate_end.getTime());
                    time_end = time_end_ts.getTime();
                    //String string_time_end = new SimpleDateFormat("yyyy-MM-dd").format(time_end);
                    //string_time_end = string_time_end.substring(0, string_time_end.length()-3);

                    System.out.println("parsedDate_end: "+parsedDate_end);
                    System.out.println("time_end_ts: "+time_end_ts);
                    System.out.println("time_end: "+time_end);
                    //System.out.println("---> string_time_end: "+string_time_end);

                    //time_start.getTime();
                    //System.out.println("\nTIME_START: "+time_start);
                } catch (Exception e) {
                    System.out.println("\nError parseando timestamp: " + e + "\n");
                }

                //Then, when the data is almost ready, do the query
                //Only time_start
                //System.out.println("\nTrying to recover the query from the event ["+ event +"] since ["+ time_start +"]\n");
                //Given time_start and time_end
                //System.out.println("\nTrying to recover the query from the event [" + event + "] since [" + time_start_ts + "(" + time_start + ")] to [" + time_end_ts + "(" + time_end + ")]\n");
                System.out.println("\nTrying to recover the query from the event [" + hashtag + "] since [" + time_start_ts + "(" + time_start + ")] to [" + time_end_ts + "(" + time_end + ")]\n");

                //no funciona porque lee antes de tener datos??
                //buscar librería timestamp para convertir String a formato fecha (formato tiempo milisegundos)
                try (Transaction ignored = Neo4jAgent.db.beginTx();
                     Result result = Neo4jAgent.db.execute("MATCH (u:USER)-[:CREATES]->(t:TWEET)-[:HAS_HASHTAG]->(e:EVENT{Hashtag:\'" + hashtag + "\'})" +","+
                             "(u2:USER)<-[:MENTION]-(t:TWEET) " +
                             "WHERE t.tweet_created_at>=" + time_start + " AND t.tweet_created_at<=" + time_end + " " +
                             "RETURN u.User_screen_name, u2.User_screen_name, t.tweet_id") //, t.tweet_created_at
                             //"RETURN u.User_screen_name, t.tweet_id")
                ) {
                    System.out.println("\nResult query: " + result.toString() + "\n");
                    cont_nodes = 1;
                    rows = "";
                    //String rowsedges="";
                    String u1 = "";
                    String u2 = "";
                    int i1 = -1;
                    int i2 = -1;

                    int indice;
                    int contador = 0;
                    int contador_e = 0;
                    String nodes = "";
                    String edge = "";
                    String edges = "";


                    while (result.hasNext()) {
                        //System.out.println("\nHa entrado a 'hasNext()'\n");
                        Map<String, Object> row = result.next();
                        //rows += (cont_nodes++);
                        indice = 0;
                        for (Map.Entry<String, Object> column : row.entrySet()) {
                    /* GRAPHML GRAPH */



                    /* PAJEK GRAPH */
                            //System.out.println("\nÍNDICE: "+indice+"\n");
                            //System.out.println("\nCOLUMN.GETVALUE() = "+column.getValue()+"\n");
                            //rows += column.getKey() + ": " + column.getValue() + "; ";

                            if (indice < 2) { // u y u2 | valores índice: 0 y 1

                                rows = "" + column.getValue(); //+=

                                if (!listaNodos.contains(rows)) { //si no está ya, se inserta
                                    listaNodos.add(contador, rows);//rows en la posición contador
                                    contador++;
                                }

                                if (indice == 0) {
                                    u1 = listaNodos.get(listaNodos.indexOf(rows)); //devuelve el elemento de la lista
                                    i1 = listaNodos.lastIndexOf(u1);
                                    System.out.println("\nELEMENTOS U1: " + u1);
                                } else {
                                    u2 = listaNodos.get(listaNodos.indexOf(rows));
                                    i2 = listaNodos.lastIndexOf(u2);
                                    System.out.println("\nELEMENTOS U2: " + u2);

                                    //nodes +=(cont_nodes++)+" "+u1+"\n"+(cont_nodes++)+" "+u2+"\n";

                                    //edge =""+(i1+1)+" "+(i2+1); //En pajek, el enlace entre dos nodos se hacía por el índice
                                    edge = "" + u1 + " " + u2;
                                    edgeElements.add(u1);
                                    edgeElements.add(u2);

                                    if (!listaEdges.contains(edge)) {
                                        //ejesEnlaces.add(u1., u2);
                                        listaEdges.add(edge);

                                    }

                                }


                                //System.out.println("\n-> "+rows);
                                //nodes += (cont_nodes++) + " " + rows + "\n";
                                indice++;
                            } else { //tweet_id
                                rows = "" + column.getValue();
                                //System.out.println("\n ROWS edge: "+rows);
                                //edges += (cont_nodes-2)+" "+(cont_nodes-1)+ " \n";
                                indice++;
                            }

                        }
                        //rows += "\n";


                    }

                    if (!result.hasNext()) {
                        System.out.println("\nNo hay Query. No se puede generar el grafo.\n");
                    } else {
                        System.out.println("\nHabemus query! \n");
                    }

                    for (int ii = 0; ii < listaNodos.size(); ii++) {
                        System.out.println("\nListaNodos: " + listaNodos.get(ii) + "\n");
                        if (ii == (listaNodos.size() - 1))
                            nodes += (ii + 1) + " \"" + listaNodos.get(ii) + "\"";
                        else
                            nodes += (ii + 1) + " \"" + listaNodos.get(ii) + "\" \n";
                    }
                    String[] palabrasSeparadas = null;
                    int conta = 0;

            /*for (int jj=0; jj<listaEdges.size(); jj++){
                System.out.println("\nListaEdges: "+listaEdges.get(jj)+"\n");
                palabrasSeparadas = listaEdges.get(jj).split(" ");
                System.out.println("\nPALABRAS SEPARADAS: "+palabrasSeparadas+"\n");
                //System.out.println("---> e1: "+listaEdges.get(jj).substring(0, 1)+" | e2: "+listaEdges.get(jj).substring(2, 3)+"\n");
                System.out.println("---> e1: "+palabrasSeparadas+" | e2: "+listaEdges.get(jj).substring(2, 3)+"\n");
                edges += listaEdges.get(jj)+"\n";
            }*/
                    for (int jj = 0; jj < edgeElements.size(); jj += 2) {
                        //System.out.println("\nedgeElements: "+edgeElements.get(jj)+"\n");

                        System.out.println("---> e1: " + edgeElements.get(jj) + " | e2: " + edgeElements.get(jj + 1) + "\n");
                        //edges += listaEdges.get(jj)+"\n";
                    }


                    System.out.println("\nNODES: " + nodes + "\n");
                    //System.out.println("\nEDGES: " + edges + "\n");

                    //nodes and edges in the same file --------------------
                    try {
                /*pw = new PrintWriter(pajekFile);
                pw.println("*Vertices "+listaNodos.size());
                //procesar nodos para añadir al fichero (PENDIENTE)
                pw.println(nodes);
                //procesar ejes (PENDIENTE)
                pw.println("*edges");
                //procesar nodos para añadir al fichero (PENDIENTE)
                pw.println(edges);
                //procesar ejes (PENDIENTE)*/

                        pw = new PrintWriter(GMLGraph);
                        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                        pw.println("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"  \n" +
                                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                "    xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n" +
                                "     http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");
                        pw.println("<graph id=\"G\" edgedefault=\"directed\">");
                        for (int n = 0; n < listaNodos.size(); n++) {
                            pw.println("<node id=\"" + listaNodos.get(n) + "\" name=\"" + listaNodos.get(n) + "\" />");
                        }
                        for (int e = 0; e < edgeElements.size(); e += 2) {
                            //pw.println("<edge source=\""+listaEdges.get(e).substring(0, 1)+"\" target=\""+listaEdges.get(e).substring(2, 3)+"\"/>");
                            pw.println("<edge source=\"" + edgeElements.get(e) + "\" target=\"" + edgeElements.get(e + 1) + "\"/>");
                        }
                        pw.println("</graph>\n" +
                                "</graphml>");


                    } catch (Exception e_file) {
                        System.out.println("\n Error creando fichero pajekNodes: " + e_file + "\n");
                    }
                    //pajekFile.close();
                    GMLGraph.close();
                    //-----------------------------------


                    //// message to VisualAgent
                    try {
                        //ACLMessage query_message = new ACLMessage(ACLMessage.INFORM);
                        end_neo4j_agent = new ACLMessage(ACLMessage.INFORM);
                        end_neo4j_agent.setSender(this.getAid());
                        end_neo4j_agent.addReceiver(new AgentID("AgentVisual"));
                        end_neo4j_agent.setContent("\n--> GRAPH GENERATED!\n");
                        end_neo4j_agent.setOntology("Show this! - from QueryNeo4J");
                        this.send(end_neo4j_agent);  //---->(Visual agent)

                    }catch(Exception e_mes_va){
                        System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                    }
                    //// end

                    //A message for the ManagerAgent is sent
                    //ACLMessage end_neo4j_agent = new ACLMessage(ACLMessage.INFORM);
                    end_neo4j_agent.setSender(this.getAid());
                    end_neo4j_agent.setReceiver(new AgentID("BossAgent"));
                    end_neo4j_agent.setOntology("queryNeo4jAgent | END");
                    end_neo4j_agent.setContent(hashtag);
                    this.send(end_neo4j_agent); //---->(ManagerAgent)


                    //System.out.println("\nOUTPUT: \n"+ rows +"\n");

                    //ignored.success();//<----
                    //ignored.close();

                } catch (Exception e) {
                    System.out.println("\nError in Transaction (queryNeo4jAgent): " + e + "\n");
                }
            } // end if (!hashtag.equals("")){}



    }// end execute()

    private static void registerShutdownHook( final GraphDatabaseService db )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()

            {
                db.shutdown();
            }
        } );
    }
}
