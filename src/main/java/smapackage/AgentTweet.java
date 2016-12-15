package smapackage;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.conf.ConfigurationBuilder;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.BaseAgent;

/**
 * Created by carma12
 * AgentTweet: Recover tweets in base of an event (#hashtag)
 *
 */

public class AgentTweet extends BaseAgent {

    static boolean executed_once = false;
    static String hashtag = "";

    //MESSAGE CONTAINERS
    //static ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    //static ACLMessage main_messages_thread = new ACLMessage(ACLMessage.INFORM);

    public AgentTweet(AgentID aid) throws Exception {
        super(aid);
    }

    public void onMessage(ACLMessage menstop) {

        System.out.println("\nMESSAGE (from Neo4jAgent or ManagerAgent) -> "+menstop.getContent()+"\n");

        if (menstop.getOntology().equals("spreading hashtag")){
            hashtag = menstop.getContent();
            System.out.println("Intentando ejecutar execute()...");
            System.out.println("executed_once: "+executed_once);
            if (!executed_once) {
                //this.execute();
                executed_once = true;
            }
        }
    }



	public void execute(){

        do{
            System.out.println("(AgentTweet)-> Waiting...");
        }while(!executed_once);

        System.out.println("----> EXECUTING!");
        System.out.println(this.getName());

        /*try {
            System.out.println("\n(AgentTweet)-> Taking a rest at the beginning of the execution... ");
            Thread.sleep(6000);                 //1000 milliseconds is one second. 900000 is 15 minutes | 60000 | 15000 | 7000
            System.out.println("\nWaking up. Time to work!");
        } catch (InterruptedException ex) {
            System.out.println("\nError while sleeping (AgentTweet)\n");
            Thread.currentThread().interrupt();
        }*/

        //// message to VisualAgent
        /*try {
            ACLMessage main_messages_thread = new ACLMessage(ACLMessage.INFORM);
            main_messages_thread.setSender(this.getAid());
            main_messages_thread.addReceiver(new AgentID("AgentVisual"));
            main_messages_thread.setContent("STARTING EVENT SEARCH... ");
            main_messages_thread.setOntology("Show this! - from AgentTweet");
            this.send(main_messages_thread);

        }catch(Exception e_mes_va){
            System.out.println("Error sending message to VisualAgent: "+e_mes_va);
        }*/
        //// end

        boolean loop_exit = false;
        System.out.println("(AgentTweet) -> Ready to start execute!");
        do {
            if (!hashtag.equals("")) {
                int cont;
                int cont2;
                String jsonTweet = "";
                System.out.println("(AgentTweet) -> Inside execute()");
                System.out.println("\nHi! Here agent " + this.getName() + " ready to start my execution :)\n");




                try {

                    //Iniciate Twitter4J configurations...
                    ConfigurationBuilder cb = new ConfigurationBuilder();
                    cb.setJSONStoreEnabled(true);

                    cb.setDebugEnabled(true)
                            .setOAuthConsumerKey("cXIDOUrrhhx0pp9HmNOkINNA3")
                            .setOAuthConsumerSecret("nPCvFQk6SJBJ4ddydz8IheJ95rJnkF5QAGDDeZOj0u4CBSdMNN")
                            .setOAuthAccessToken("3081707931-ftj5HZMom2e7rdyku0JqGXZrH8LmuItPLsQMUWm")
                            .setOAuthAccessTokenSecret("qGkovoYGGoQtW2gR8CfZVRdfb0gy0oeFm3ihsm9fYixhS");


                    TwitterFactory tf = new TwitterFactory(cb.build());
                    Twitter twitter = tf.getInstance();


                    //Setting #hashtag to search
                    Query query = new Query(hashtag);
                    //query.count(1); //100 is the max allowed
                    QueryResult result;

                    cont = 1;
                    cont2 = 0;


                    //Recovering all tweets and sending to Neo4jAgent (the JSON file)...
                    System.out.println("\nRecuperando tweets del hashtag " + query.getQuery() + "... (AgentTweet)\n");

                    searchloop:
                    do {
                        result = twitter.search(query);
                        System.out.println("result.getCount(): "+result.getCount());
                        List<Status> tweets = result.getTweets();

                        if (cont2 < 100) { // 50 | 150 (120000 ms /2mins) | 50 (15000 ms/15segs) | 30 (7000ms / 7segs) | 100 ()

                            for (Status tweet : tweets) {

                                try {//

                                    System.out.println("\n---> nº de tweets: " + (cont++) + " (AgentTweet)\n" + tweet.getText() + "\n");

                                    jsonTweet = TwitterObjectFactory.getRawJSON(tweet);

                                    //try {
                                        try {
                                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                                            msg.setSender(this.getAid());
                                            msg.addReceiver(new AgentID("Neo4jAgent"));
                                            msg.setContent(jsonTweet);
                                            msg.setOntology("Tweet");
                                            this.send(msg); //-------->(Neo4jAgent)

                                            /*try {
                                                System.out.println("\nSleep while insert \n");
                                                Thread.sleep(6000);                 //1000 milliseconds is one second.| 780000 -> 13 mins (for completing 15 with)
                                                cont2 = 0;
                                                System.out.println("\n\t(AgentTweet-Qpid)->I woke up! Back to work...\n");
                                            } catch (InterruptedException ex) {
                                                System.out.println("\nError while sleeping (AgentTweet)\n");
                                                Thread.currentThread().interrupt();
                                            }*/

                                        }catch(Exception e_insend){
                                            System.out.println("Error during sending message: "+e_insend);
                                            loop_exit = true;
                                            System.out.println("Breaking...");
                                            break searchloop;
                                        }

                                    /*}catch(Exception e_send){
                                        System.out.println("Error setting message to send: "+e_send);
                                        loop_exit = true;
                                        System.out.println("Breaking...");
                                        break searchloop; //exit from loop
                                    }*/

                                    cont2++;


                                } catch (Exception exc) {//
                                    System.out.println("\n(AgentTweet)->ERROR PROCESANDO EL ENVÍO DE DATOS: " + exc + "\n");
                                    System.out.println("\'" + exc + "\'");
                                    System.out.println("\nWE ALREADY KNOW THAT'S A QPID ERROR\n");

                                    try {
                                        System.out.println("\n\t(AgentTweet-Qpid)->It has been an Qpid error. Sleep, damn it!\n");
                                        Thread.sleep(12000);                 //1000 milliseconds is one second.| 780000 -> 13 mins (for completing 15 with)
                                        cont2 = 0;
                                        System.out.println("\n\t(AgentTweet-Qpid)->I woke up! Back to work...\n");
                                    } catch (InterruptedException ex) {
                                        System.out.println("\nError while sleeping (AgentTweet)\n");
                                        Thread.currentThread().interrupt();
                                    }
                                }
                            }

                        } else {
                            //Recupera x tweets y espera 15 minutos y vuelta al trabajo...
                            try {
                                System.out.println("\n\t(AgentTweet)->Taking a tiny rest... \n");
                                //// message to VisualAgent
                                /*try {
                                    //main_messages_thread = new ACLMessage(ACLMessage.INFORM);
                                    main_messages_thread.setSender(this.getAid());
                                    main_messages_thread.addReceiver(new AgentID("AgentVisual"));
                                    main_messages_thread.setContent("\n\t(AgentTweet)->Taking a tiny rest... \n");
                                    main_messages_thread.setOntology("Show this! - from AgentTweet");
                                    this.send(main_messages_thread);

                                }catch(Exception e_mes_va){
                                    System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                                }*/
                                //// end
                                Thread.sleep(60000);                 //1000 milliseconds is one second. 900000 is 15 minutes | 60000 | 15000 | 7000
                                cont2 = 0;
                                System.out.println("\n\t(AgentTweet)->I woke up! Back to work...\n");
                                //// message to VisualAgent
                                /*try {
                                    //main_messages_thread = new ACLMessage(ACLMessage.INFORM);
                                    main_messages_thread.setSender(this.getAid());
                                    main_messages_thread.addReceiver(new AgentID("AgentVisual"));
                                    main_messages_thread.setContent("\n\t(AgentTweet)->I woke up! Back to work...\n");
                                    main_messages_thread.setOntology("Show this! - from AgentTweet");
                                    this.send(main_messages_thread);

                                }catch(Exception e_mes_va){
                                    System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                                }*/
                                //// end
                            } catch (InterruptedException ex) {
                                System.out.println("\nError while sleeping (AgentTweet)\n");
                                Thread.currentThread().interrupt();
                            }
                        }
                    } while ((query = result.nextQuery()) != null || loop_exit);


                    System.out.println("\nSuccessfull recovery of all tweets! (" + (cont - 1) + ")\n");

                    //// message to VisualAgent
                    /*try {
                        ACLMessage main_messages_thread = new ACLMessage(ACLMessage.INFORM);
                        main_messages_thread.setSender(this.getAid());
                        main_messages_thread.addReceiver(new AgentID("AgentVisual"));
                        main_messages_thread.setContent("\nSuccessfull recovery of all tweets! (" + (cont - 1) + ")\n-------------------------------------");
                        main_messages_thread.setOntology("Show this! - from AgentTweet");
                        this.send(main_messages_thread);

                    }catch(Exception e_mes_va){
                        System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                    }*/
                    //// end

                    //Enviar señal de fin --->
                    //System.out.println("\nSending end signal...\n");
                    //msg.setInReplyTo("FIN");

                    //Enviar nº total de tweets (servirá como referencia)
                    try {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setSender(this.getAid());
                        msg.addReceiver(new AgentID("Neo4jAgent"));
                        msg.setOntology("Tweet");
                        msg.setContent(String.valueOf(cont - 1));
                        this.send(msg);//---> Neo4jAgent

                    } catch (Exception e_success) {
                        System.out.println("(AgentTweet)->Error sending total number of messages! " + e_success);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setSender(this.getAid());
                        msg.addReceiver(new AgentID("Neo4jAgent"));
                        msg.setOntology("Tweet");
                        msg.setContent(String.valueOf(cont - 1));
                        this.send(msg);//---> Neo4jAgent
                    }

                    //y enviar mensaje de fin al ManagerAgent <------------------------------
                    try {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setSender(this.getAid());
                        msg.addReceiver(new AgentID("BossAgent"));
                        msg.setOntology("End");
                        msg.setContent("End: AgentTweet");
                        this.send(msg);//---> ManagerAgent

                    } catch (Exception e_success) {
                        System.out.println("(AgentTweet)->Error sending end message! " + e_success);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setSender(this.getAid());
                        msg.addReceiver(new AgentID("BossAgent"));
                        msg.setOntology("End");
                        msg.setContent("End: AgentTweet");
                        this.send(msg);//---> ManagerAgent
                    }


                } catch (Exception e) {
                    System.out.println("ERROR (related to AgentTweet): " + e.getMessage());
                    if (e.getMessage().equals("api.twitter.com")) {
                        System.out.println("\nError related to Twitter. \n");
                        try {
                            System.out.println("\n\t(AgentTweet - From Error)->Resting... \n");
                            Thread.sleep(600000);                 //1000 milliseconds is one second. 900000 is 15 minutes | 60000 | 15000 | 7000
                            System.out.println("\n\t(AgentTweet - From Error)->Waking up!\n");
                        } catch (InterruptedException ex) {
                            System.out.println("\nError while sleeping (AgentTweet)\n");
                            Thread.currentThread().interrupt();
                        }
                    }else{
                        System.out.println("\nError related to Twitter Limit exception. \n");
                        try {
                            System.out.println("\n\t(AgentTweet - From Error)->Resting... \n");
                            Thread.sleep(600000);                 //1000 milliseconds is one second. 900000 is 15 minutes | 60000 | 15000 | 7000
                            System.out.println("\n\t(AgentTweet - From Error)->Waking up!\n");
                        } catch (InterruptedException ex) {
                            System.out.println("\nError while sleeping (AgentTweet)\n");
                            Thread.currentThread().interrupt();
                        }
                    }
                }

            }
        }while(hashtag.equals(""));
		
	}

}
