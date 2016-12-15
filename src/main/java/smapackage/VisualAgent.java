package smapackage;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import es.upv.dsic.gti_ia.core.BaseAgent;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by carma12
 */
public class VisualAgent extends BaseAgent {
    private JTextField textField1;
    private JButton searchButton;
    private JTextField textField2;
    public JPanel panel_main; //private before
    private JPanel field_events;
    private JPanel textarea_panel;
    private JTextArea textArea1;
    private JPanel field_stats;
    private JScrollPane scroll;
    private JTextArea textArea2;
    private JScrollPane scroll2;
    private JLabel from_date;
    private JTextField textField3;
    private JTextField textField4;
    private JButton analyze_button;
    private JLabel format_date;
    static boolean start = false;
    static boolean start2 = false;
    static boolean stop_messages = false;
    static boolean stop_messages2 = false;
    public boolean visualize = true;
    boolean all_data_fine = false;
    boolean all_data_fine2 = false;
    boolean flag_one = false;

    static String all_output = "";
    //JScrollPane scroll = new JScrollPane(textArea1);



    boolean hashtag_flag = false;
    boolean hashtag_flag2 = false;
    boolean field_not_empty_flag = false;
    //static JFrame frame;
    static JFrame jf_error = new JFrame("Error");


    ACLMessage hashtag_msg = new ACLMessage(ACLMessage.INFORM);

    public VisualAgent(AgentID aid) throws Exception {

        super(aid);
        //textarea_panel.add(scroll);
        //panel_main.add(textarea_panel);

        /*JFrame frame = new JFrame("main2_visual");
        try {
            frame.setContentPane(new main2_visual(new AgentID("AgentVisual")).panel_main);
        } catch (Exception e) {
            e.printStackTrace();
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);*/

        //evento_search();
        /*String hashtag = textField1.getText();
        //searchButton.addActionListener(new searchAction(hashtag));
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

            }
        });*/



    }

    boolean isValidDate(String input) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            format.parse(input);
            return true;
        }
        catch(ParseException e){
            return false;
        }
    }

    /* Event analyze - Second button event */
    public void evento_analyze(){
        analyze_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String hashtag = textField2.getText();
                String date_ini = textField3.getText();
                String date_end = textField4.getText();

                if (!hashtag.equals("")) {  //hashtag not empty
                    if (!hashtag.substring(0, 1).equals("#")) { // hashtag begins with #
                       if (date_ini.matches("\\d{4}-\\d{2}-\\d{2}") && date_end.matches("\\d{4}-\\d{2}-\\d{2}") && isValidDate(date_ini) && isValidDate(date_end)) {
                           if (!stop_messages2) {
                               System.out.println("hashtag not empty");
                               System.out.println("hashtag does not begins with #");
                               System.out.println("hashtag has correct format");
                               System.out.println("stop_messages2: "+stop_messages2);
                               try {
                                   // Send back message to BossAgent -> Agents AgentTweet and neo4JAgent can start!
                                   System.out.println("Evento analyze -> Data ok! ");
                                   hashtag_msg.addReceiver(new AgentID("BossAgent"));
                                   hashtag_msg.setContent(hashtag + " " + date_ini + " " + date_end);
                                   hashtag_msg.setOntology("visualAgent-analyze");
                                   start2 = true;
                                   all_data_fine2 = true;
                                   //System.out.println("(main2) -> Configurando mensaje y poniendo start a TRUE");
                                   //cómo se le envía si está en el constructor? desde fuera?
                               } catch (Exception e_non_error_panel) {
                                   System.out.println("Error within non-error panel: " + e_non_error_panel);
                               }
                               stop_messages2 = true;
                           }
                       }else{
                           JOptionPane.showMessageDialog(jf_error, "Date format is not correct");
                       }
                    }else{
                        if (!hashtag_flag2) {
                            JOptionPane.showMessageDialog(jf_error, "No need to begin with hashtag symbol (#)");
                            hashtag_flag2 = true;
                        }
                    }
                }else{
                    JOptionPane.showMessageDialog(jf_error, "Empty field (test)");
                }


            }
        });
    }


/* Evento Search - First button event */
        public void evento_search(){
            //System.out.println("(main2) -> Dentro de evento_search()");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                String hashtag = textField1.getText();

                if (!hashtag.equals("")) {  //hashtag not empty
                    if (!hashtag.substring(0, 1).equals("#")) { // hashtag begins with #
                        //aquí el hashtag es correcto. ACLMEssage que envíe el hashtag al AgentTweet
                        //ACLMessage hashtag_msg = new ACLMessage(ACLMessage.INFORM);
                        if (!stop_messages) {
                            System.out.println("Hashtag non-empty and does not begin with # symbol :D");
                            try {
                                // Send back message to BossAgent -> Agents AgentTweet and neo4JAgent can start!
                                hashtag_msg.addReceiver(new AgentID("BossAgent"));
                                hashtag_msg.setContent(hashtag);
                                hashtag_msg.setOntology("visualAgent");
                                start = true;
                                all_data_fine = true;
                                //System.out.println("(main2) -> Configurando mensaje y poniendo start a TRUE");
                                //cómo se le envía si está en el constructor? desde fuera?
                            } catch (Exception e_non_error_panel) {
                                System.out.println("Error within non-error panel: " + e_non_error_panel);
                            }
                            stop_messages = true;
                        }
                    } else {
                        //System.out.println("Hashtag doesn't begins with # symbol...");

                        /*JDialog jl_err_hash = new JDialog(jf_error, "Event does not begin with hashtag symbol (#)", true);
                        jl_err_hash.setLocationRelativeTo(jf_error);
                        jl_err_hash.setVisible(true);
                        //jl_err_hash.setText("Event does not begin with hashtag symbol (#)");
                        jf_error.setContentPane(jl_err_hash);*/


                        //testing line ....
                        if (!hashtag_flag) {
                            JOptionPane.showMessageDialog(jf_error, "No need to begin with hashtag symbol (#)");
                            hashtag_flag = true;
                        }


                    }
                } else {
                    //System.out.println("Hashtag empty...");

                   /* JDialog jl_err_empty = new JDialog(jf_error, "Empty field", true);
                    jl_err_empty.setLocationRelativeTo(jf_error);
                    Button err_empty_close = new Button();
                    jl_err_empty.add(err_empty_close);

                    try {
                        jl_err_empty.setVisible(true);
                        //jl_err_empty.setText("Empty field");
                        jf_error.setContentPane(jl_err_empty);
                    }catch(Exception e_error_panel){
                        System.out.println("Error showing panel: "+e_error_panel);
                    }*/

                    //testing line ....
                    JOptionPane.showMessageDialog(jf_error, "Empty field (test)");
                }
            }
        });
    }

    /* It shows all output messages */
    public void onMessage(ACLMessage message_visual){

        String n4j_content = message_visual.getContent();
        String query_stats_content = message_visual.getContent();

        if (message_visual.getOntology().equals("Show this! - form Neo4j") || message_visual.getOntology().equals("Show this! - from AgentTweet")){

            try {
                all_output += n4j_content;
                textArea1.setText(all_output+"\n");
            }catch (Exception e_show){
                System.out.println("Error al mostrar en textarea: "+e_show);
                textArea1.setText("Error al mostrar en textarea: "+e_show+"\n");
            }



        }else if (message_visual.getOntology().equals("Show this! - from QueryNeo4J") || message_visual.getOntology().equals("Show this! - from GraphAnalyzer")){

            try {
                all_output += query_stats_content;
                textArea2.setText(all_output+"\n");
            }catch (Exception e_show){
                System.out.println("Error al mostrar en textarea: "+e_show);
                textArea2.setText("Error al mostrar en textarea: "+e_show+"\n");
            }
        }
    }

    public void execute(){
        //hasta que no tengamos los datos bien, la función evento_search se ejecutará las veces que haga falta



        while (visualize) {
            /*scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            textarea_panel.add(scroll);
            panel_main.add(textarea_panel);*/

            evento_search();
            evento_analyze();
            /*if (hashtag_flag){
                hashtag_flag = false;
            }*/

            //all data is fine
            if (start || start2){
                visualize = false;
                System.out.println("Changed visualize to true");
            }
        }

        if (start && !visualize) {
            this.send(hashtag_msg); // -> AgentTweet & Neo4jAgent
            System.out.println("Lanzando mensaje a ManagerAgent!");
            //visualize = false;
            all_data_fine = true;
            all_output += "\n";

         }else if (start2 && !visualize && stop_messages2) { // -> QueryNeo4jAgent & GraphAnalyzerAgent
            this.send(hashtag_msg);
            System.out.println("Lanzando mensaje a ManagerAgent!");
            //visualize = false;
            all_data_fine = true;
            all_output += "\n";
        }

        while (!visualize) {} //we need VisualAgent until process shuts down

        System.out.println("main2_visual shutting down...");


    }






    /*public static void main(String[] args) {
        JFrame frame = new JFrame("main2_visual");
        try {
            frame.setContentPane(new main2_visual(new AgentID("AgentVisual")).panel_main);
        } catch (Exception e) {
            e.printStackTrace();
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);


    }*/
}
