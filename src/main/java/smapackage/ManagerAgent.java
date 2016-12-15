package smapackage;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.BaseAgent;

import javax.swing.*;

/**
 * Created by carma12
 * ManagerAgent: One agent to rull them all... :D
 */

public class ManagerAgent extends BaseAgent {

    // Hashtag is not set (yet)
    static String hashtag = "";
    // All agents
    AgentTweet at;
    Neo4jAgent en4j;
    //Neo4jAgent en4j2;
    QueryNeo4jAgent queryn4j;
    GraphAnalyzerAgent graphAg;
    VisualAgent mvisual;


    String message;

    private boolean finalize = false;

    public ManagerAgent(AgentID aid) throws Exception {
        super(aid);

    }

    public void setFinalize(boolean finalize) {
        this.finalize = finalize;
    }


    /* Methods that allows the agent finalize */
    public boolean isFinalize() {
        return finalize;
    }

    public void execute() {


        System.out.println("\nHello, I am the " + this.getName() + " and I am the Boss!");
        try {
            /* By default, it will be one group of agent --> (AgentTweet, Neo4jAgent, QueryNeo4jAgent and GraphAnalyzerAgent).
              * If it is required, that group can increase */





            /* AGENT INITIALIZATION */
            mvisual = new VisualAgent(new AgentID("AgentVisual"));
            //System.out.println("----> "+mvisual.panel_main);


            JFrame frame = new JFrame();
            //frame.setContentPane(new main2_visual(new AgentID("AgentVisual")).panel_main);
            frame.setContentPane(mvisual.panel_main);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);

            at = new AgentTweet(new AgentID("AgentTweet"));
            // Agent that creates/insert in Neo4j DataBase - RECEPTOR
            en4j = new Neo4jAgent(new AgentID("Neo4jAgent"));
            //Agent that gets the Neo4j Query from the Database
            queryn4j = new QueryNeo4jAgent(new AgentID("queryNeo4j"));
            //Agent that gets the graph properties
            graphAg = new GraphAnalyzerAgent(new AgentID(("graphAnalyzer")));
            //InterfazAgent

            /* START AGENTS */
            mvisual.start();

        } catch (Exception e) {
            logger.error("Error (ManagerAgent): " + e.getMessage() + "\n");
        }

        System.out.println("\nEstamos antes del while\n");
        while (!finalize) {

            //System.out.println("\nDENTRO DEL WHILE\n");


        }
        System.out.println("\nEstamos después del while\n");
    }

    public void onMessage(ACLMessage msg_boss) { //cada vez que reciba un mensaje...

        System.out.println("\n(ManagerAgent)-> Mensaje recibido!\n");
        message = msg_boss.getContent();
        System.out.println("\nContent: '" + message + "\'\n");
        System.out.println("Ontology: "+msg_boss.getOntology());
        //System.out.println("\nmsg_boss.getReplyBy(): " + msg_boss.getReplyBy()+"\n");

        //System.out.println("\nIntentando entrar al if-else de ManagerAgent...\n");

        ACLMessage m_hashtag = new ACLMessage(ACLMessage.INFORM);



        if (msg_boss.getOntology().equals("visualAgent")) { // (bloque1) --> AgentTweet & Neo4jAgent

            try {
                en4j.start();
            }catch(Exception e_en4j){
                System.out.println("en4j agent failed to start: "+e_en4j);
            }
            try {
                at.start();
            }catch(Exception e_at){
                System.out.println("at agent failed to start: "+e_at);
            }
            hashtag = msg_boss.getContent();

            //sending hashtag to agentTweet
            m_hashtag.addReceiver(new AgentID("Neo4jAgent"));
            m_hashtag.setContent(hashtag);
            m_hashtag.setOntology("spreading hashtag");
            this.send(m_hashtag);

            m_hashtag.addReceiver(new AgentID("AgentTweet"));
            m_hashtag.setContent(hashtag);
            m_hashtag.setOntology("spreading hashtag");
            this.send(m_hashtag);

            System.out.println("(ManagerAgent) -> Sending hashtag to AgentTweet and Neo4jAgent");

            // from NEO4JAGENT | throws QueryNeo4JAgent
        } else if (msg_boss.getContent().equals("END: Neo4jAgent")) {//(msg_boss.getSender().name == "Neo4jAgent"){ //message == "END: Neo4jAgent"
           /*System.out.println("\nQueryNeo4jAgent va a comenzar su ejecución...\n");
            queryn4j.start();

            m_hashtag.setContent(hashtag);
            m_hashtag.setOntology("spreading hashtag");
            m_hashtag.addReceiver(new AgentID("queryNeo4j"));
            this.send(m_hashtag);*/

            // from QUERYNEO4J | throws GrahAnalyzerAgent (Last)
        }/*else if (msg_boss.getContent().equals("End: AgentTweet") && msg_boss.getSender().name.equals("AgentTweet") ){
            at.finalize();

        }*/else if (msg_boss.getOntology().equals("visualAgent-analyze")) { //(bloque2) --> QueryNeo4j
            System.out.println("Se ha lanzando visualAgent-analyze!");
            queryn4j.start();

            hashtag = msg_boss.getContent();
            //sending hashtag to agentTweet

            m_hashtag.addReceiver(new AgentID("queryNeo4j"));
            m_hashtag.setContent(hashtag);
            m_hashtag.setOntology("spreading hashtag");
            this.send(m_hashtag);


        }else if (msg_boss.getOntology().equals("queryNeo4jAgent | END")){ // --> GraphAnalyzer
            System.out.println("Lanzando GrapAnalyzerAgent...");
            graphAg.start();

            hashtag = msg_boss.getContent();
            //sending hashtag to agentTweet

            m_hashtag.addReceiver(new AgentID("graphAnalyzer"));
            m_hashtag.setContent(hashtag);
            m_hashtag.setOntology("spreading hashtag");
            this.send(m_hashtag);
        }

    }

}
