package smapackage;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

//import org.neo4j.io.fs.FileUtils;

public class MainSMA {


	
	public static void main(String[] args){
	    	
		/*
	 	* AGENTES
		*
		*
		/**
		* Setting the Logger
		*/
        DOMConfigurator.configure("configuration/loggin.xml");
		Logger logger = Logger.getLogger(MainSMA.class);

		/**
		* Connecting to Qpid Broker
		*/
		AgentsConnection.connect("localhost", 5672, "test", "guest", "guest", false);

            /*
             *  Let's begin!
             * */


			try {

                ManagerAgent boss = new ManagerAgent(new AgentID("BossAgent"));
				// Agent which recovers twitter information - EMISOR

                boss.start(); //Boss begins first


	        
			}catch(Exception e){
				logger.error("Error " + e.getMessage()+"\n");

			}



    }//end main


}
