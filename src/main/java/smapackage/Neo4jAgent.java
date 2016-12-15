package smapackage;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import org.neo4j.io.fs.FileUtils;
import twitter4j.*;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.BaseAgent;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by carma12
 * Neo4jAgent: Insert the tweets recovered by the AgentTweet
 *
 */


public class Neo4jAgent extends BaseAgent{

    static String hashtag = "";
	static String DB_PATH=  ""; //"/home/carou/Pruebas/NoTeLoPerdonareJamasCarmena"; //was 'final' before
    static boolean habemus_hashtag = false;
    static ACLMessage main_messages_thread = new ACLMessage(ACLMessage.INFORM);

    boolean already_have_hashtag = false;

	ACLMessage msg = null;
    ACLMessage end_neo4j_agent = null;
    //int cont_tweets;
    int contRest=0;

	Status tweet;
	int cont=1;
    int cont2 = 0;
	String jsonTweet;
    //String hashtag;
    //int tweets_totales=1000;

	//ArrayList<String> listTweets = new ArrayList<String>();
	private boolean finalize = false;


    public static GraphDatabaseService db;
	Node nodoUsuario;
    Node nodoUsuarioMencionado;
	Node nodoTweet;
    Node nodoEvento;
	Relationship relationship_creates; // User to Tweet
    Relationship relationship_has_hashtag; // Tweet to Event
    Relationship relationship_mention; // Tweet to User
    String rowsUser="";
    String rowsEvent="";
    String rowsTweet="";
    String rowsH_H="";
    String rowsMention="";
    String rowsCreate="";

    //List<String> hashtagList = new ArrayList<String>();
    /*boolean inserted_event = false;

    ConfigurationBuilder cb;
    TwitterFactory tf;
    Twitter twitter;*/



    private static enum Tipo implements Label {
        USER, TWEET, EVENT;
    }

	private static enum Relaciones implements RelationshipType {
		HAS_HASHTAG, MENTION, CREATES
	}

	public Neo4jAgent(AgentID aid) throws Exception {
		super(aid);
	}

	/* Methods that allows the agent finalize */
	public boolean isFinalize() {
        return finalize;
	}

	public void setFinalize(boolean finalize) {
        this.finalize = finalize;
    }



	public void execute(){
		System.out.println("\nEnter in execution Neo4j Agent\n");

		while (!finalize) {}

	}


    public void createUserNode(User us){

        System.out.println("\nCREANDO NODO USUARIO...");
        nodoUsuario = db.createNode(Tipo.USER);
        nodoUsuario.setProperty("User_id", us.getId());

        nodoUsuario.setProperty("User_name",us.getName());

        nodoUsuario.setProperty("User_screen_name",us.getScreenName());

        try {
            nodoUsuario.setProperty("User_created_at", us.getCreatedAt().toString());
        }catch(Exception e){
            //pueden haber dos casos: que la fecha exista (que normalmente lo hará) o que se cree cuando el usuario se cree de nuevo (sin recuperarlo)
            //en el último caso se pondrá la fecha actual de creación
            java.util.Date fecha_act = new Date();
            nodoUsuario.setProperty("User_created_at", fecha_act.toString());
        }

        try{
            nodoUsuario.setProperty("User_description", us.getDescription().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_description", "");
        }

        try{
            nodoUsuario.setProperty("User_url", us.getURL().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_url", "");
        }

        try {//
            nodoUsuario.setProperty("User_favorites_count", us.getFavouritesCount());
        }catch(Exception e){
            nodoUsuario.setProperty("User_favorites_count", "");
        }

        try {//
            nodoUsuario.setProperty("User_followers_count", us.getFollowersCount());
        }catch(Exception e){
            nodoUsuario.setProperty("User_followers_count", "");
        }

        try {//
            nodoUsuario.setProperty("User_friends_count", us.getFriendsCount());
        }catch(Exception e){
            nodoUsuario.setProperty("User_friends_count", "");
        }

        try{
            nodoUsuario.setProperty("User_lang", us.getLang().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_lang", "");
        }
        //System.out.println(tweet.getUser().getLang() + "\n");

        try {//
            nodoUsuario.setProperty("User_listed_count", us.getListedCount());
        }catch(Exception e){
            nodoUsuario.setProperty("User_listed_count", "");
        }

        try{
            nodoUsuario.setProperty("User_location", us.getLocation().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_location", "");
        }

        try{
            nodoUsuario.setProperty("User_time_zone", us.getTimeZone().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_time_zone", "");
        }

        nodoUsuario.setProperty("User_utc_offset", us.getUtcOffset());

        try{
            nodoUsuario.setProperty("User_profile_background_color", us.getProfileBackgroundColor().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_profile_background_color", "");
        }

        try{
            nodoUsuario.setProperty("User_profile_background_image_url", us.getProfileBackgroundImageURL().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_profile_background_image_url", "");
        }

        try{
            nodoUsuario.setProperty("User_profile_background_image_url_https", us.getProfileBackgroundImageUrlHttps().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_profile_background_image_url_https", "");
        }

        try{
            nodoUsuario.setProperty("User_profile_banner_url", us.getProfileBannerURL().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_profile_banner_url", "");
        }

        try{
            nodoUsuario.setProperty("User_profile_banner_ipad_retina_url", us.getProfileBannerIPadRetinaURL().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_profile_banner_ipad_retina_url", "");
        }

        try{
            nodoUsuario.setProperty("User_profile_banner_ipad_url", us.getProfileBannerIPadURL().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_profile_banner_ipad_url", "");
        }

        try{
            nodoUsuario.setProperty("User_profile_banner_mobile_url", us.getProfileBannerMobileURL().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_profile_banner_mobile_url", "");
        }

        try{
            nodoUsuario.setProperty("User_profile_image_url", us.getProfileImageURL().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_profile_image_url", "");
        }

        try{
            nodoUsuario.setProperty("User_profile_image_url_https", us.getProfileImageURLHttps().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_profile_image_url_https", "");
        }

        try {//
            nodoUsuario.setProperty("User_profile_link_color", us.getProfileLinkColor().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_profile_link_color", "");
        }

        try {//
            nodoUsuario.setProperty("User_profile_sidebar_border_color", us.getProfileSidebarBorderColor().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_profile_sidebar_border_color", "");
        }

        try {//
            nodoUsuario.setProperty("User_profile_sidebar_fill_color", us.getProfileSidebarFillColor().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_profile_sidebar_fill_color", "");
        }

        try {//
            nodoUsuario.setProperty("User_profile_text_color", us.getProfileTextColor().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_profile_text_color", "");
        }

        try{
            nodoUsuario.setProperty("User_status", us.getStatus().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_status", "");
        }

        try {//
            nodoUsuario.setProperty("User_statuses_count", us.getStatusesCount());
        }catch(Exception e){
            nodoUsuario.setProperty("User_statuses_count", "");
        }

        try{
            nodoUsuario.setProperty("User_description_url_entities", us.getDescriptionURLEntities().toString());
        }catch(Exception e){
            nodoUsuario.setProperty("User_description_url_entities", "");
        }


        System.out.println("\n---> CREATED USER NODE (@" + us.getScreenName() + ")\n");
    }

    public void createNodeTweet(Status tw){

        nodoTweet = db.createNode(Tipo.TWEET);
        nodoTweet.setProperty("tweet_id", tw.getId());

        //pasamos la fecha en formato timestamp
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH); //EE MMM dd HH:mm:ss z yyyy
            Date parsedDate = dateFormat.parse(tw.getCreatedAt().toString());
            Timestamp timestamp_tweet_created_at = new java.sql.Timestamp(parsedDate.getTime());
            long tweet_created_at_long = timestamp_tweet_created_at.getTime();

            nodoTweet.setProperty("tweet_created_at", tweet_created_at_long); //se inserta un long

        } catch (ParseException e) {
            System.out.println("\n No se ha podido añadir la variable tweet_created_at. " + e + "\n Dejando el campo vacío.\n");
            nodoTweet.setProperty("tweet_created_at", "");
        }


        try {//
            nodoTweet.setProperty("tweet_text", tw.getText());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_text", "");
        }

        try{//
            nodoTweet.setProperty("tweet_user", tw.getUser().getScreenName().toString());//puede ser también el ID
        }catch(Exception e){
            nodoTweet.setProperty("tweet_user", "");
        }

        try {//
            nodoTweet.setProperty("tweet_favorite_count", tw.getFavoriteCount());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_favorite_count", "");
        }

        try {//
            nodoTweet.setProperty("tweet_retweet_count", tw.getRetweetCount());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_retweet_count", "");
        }


        try{
            nodoTweet.setProperty("tweet_retweeted_status", tw.getRetweetedStatus().toString());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_retweeted_status", "");
        }


        try{
            nodoTweet.setProperty("tweet_place", tw.getPlace().toString());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_place", "");
        }

        try {//
            nodoTweet.setProperty("tweet_lang", tw.getLang().toString());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_lang", "");
        }


        try{
            nodoTweet.setProperty("tweet_geolocation", tw.getGeoLocation().toString());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_geolocation", "");
        }

        try {//
            nodoTweet.setProperty("tweet_current_user_retweet_id", tw.getCurrentUserRetweetId());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_current_user_retweet_id", "");
        }

        try {//
            nodoTweet.setProperty("tweet_contributors", tw.getContributors().toString());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_contributors", "");
        }

        try {//
            nodoTweet.setProperty("tweet_entended_media_entities", tw.getExtendedMediaEntities().toString());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_entended_media_entities", "");
        }


        try{
            nodoTweet.setProperty("tweet_in_reply_to_screen_name", tw.getInReplyToScreenName().toString());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_in_reply_to_screen_name", "");
        }

        try {//
            nodoTweet.setProperty("tweet_in_reply_to_status_id", tw.getInReplyToStatusId());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_in_reply_to_status_id", "");
        }

        try {//
            nodoTweet.setProperty("tweet_in_reply_to_user_id", tw.getInReplyToUserId());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_in_reply_to_user_id", "");
        }


        try{
            nodoTweet.setProperty("tweet_scopes", tw.getScopes().toString());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_scopes", "");
        }

        try{//
            nodoTweet.setProperty("tweet_source", tw.getSource().toString());
        }catch(Exception e){
            nodoTweet.setProperty("tweet_source", "");
        }

        System.out.println("\n---> CREATED TWEET NODE(" + tw.getText() + ")\n");

    }

    //Test if a User does exists given its Id
    public boolean userExists(long user_id){
        boolean exists_u= true;
        rowsUser = "";

        try (Transaction tx_user = db.beginTx();
             Result result = db.execute( "MATCH (u{User_id: "+user_id+"}) RETURN  u.User_id" )
        ){
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                for ( Map.Entry<String,Object> column : row.entrySet() )
                {
                    rowsUser += column.getKey() + ": " + column.getValue() + "; ";
                }
                //rowsUser += "\n";
            }

            if (!rowsUser.equals("")){
                exists_u = true;
            }else{
                exists_u = false;
            }

            tx_user.success();
            tx_user.close();
            /*try {
                tx_user.success();
            }finally {
                tx_user.close();
            }*/


        }catch(Exception exce){
            System.out.println("ERROR IN TRANSACTION tx_user: "+exce);
        }

        return exists_u;
    }

    //Recover event node
    public Node recoverEventNode(String hash){
        rowsEvent = "";
        Node nodoE = null;

        try (Transaction tx_nodeEvent = db.beginTx();
             //recupera el id de todos los nodos EVENT cuyo hashtag coincida
             Result result = db.execute( "MATCH (e{Hashtag: \'"+hash+"\'}) RETURN  ID(e)" )
        ){
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                for ( Map.Entry<String,Object> column : row.entrySet() )
                {
                    rowsEvent = column.getValue()+"";
                }
                //rowsEvent += "\n";
            }

            if (!rowsEvent.equals("")){
                //se recupera el nodo evento

                long id_node = Long.valueOf(rowsEvent);
                nodoE = db.getNodeById(id_node);


            }else{
                System.out.println("\nQuery recoverEventNode is null.\n");
            }

            tx_nodeEvent.success();
            tx_nodeEvent.close();
            /*try {
                tx_nodeEvent.success();
            }finally {
                tx_nodeEvent.close();
            }*/


        }catch(Exception exce){
            System.out.println("ERROR IN TRANSACTION tx_nodeEvent: "+exce);
        }

        return nodoE;
    }


    //recoverUser
    public Node recoverNodeUser(User us){

        rowsUser = "";
        Node nodoUM = null;

        try (Transaction tx_nodeUser = db.beginTx();
             //recupera el id de todos los nodos USUARIO cuyo User_id coincida con el usuario dado
             Result result = db.execute( "MATCH (u{User_id: "+us.getId()+"}) RETURN  ID(u)" )
        ){
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                for ( Map.Entry<String,Object> column : row.entrySet() )
                {
                    rowsUser = column.getValue()+"";
                }
                //rowsUser += "\n";
            }

            if (!rowsUser.equals("")){
                //se recupera el nodo
                long id_node = Long.valueOf(rowsUser);
                nodoUM = db.getNodeById(id_node);
                System.out.println("---> rowsUser: "+rowsUser);

            }else{
                System.out.println("\nQuery recoverNodeUser is null.\n");
            }

            tx_nodeUser.success();
            tx_nodeUser.close();
            /*try{
                tx_nodeUser.success();
            }finally {
                tx_nodeUser.close();
            }*/

        }catch(Exception exce){
            System.out.println("ERROR IN TRANSACTION tx_nodeUser: "+exce);
        }

        return nodoUM;
    }

    //recoverNodeTweet
    public Node recoverNodeTweet(Status tweet){

        rowsTweet = "";
        Node nodoTweet = null;

        try (Transaction tx_nodeTweet = db.beginTx();
             //recupera el id de todos los nodos USUARIO cuyo User_id coincida con el usuario dado
             Result result = db.execute( "MATCH (t{tweet_id: "+tweet.getId()+"}) RETURN  ID(t)" );
        ){
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                for ( Map.Entry<String,Object> column : row.entrySet() )
                {
                    rowsTweet = column.getValue()+"";
                }
                //rowsUser += "\n";
            }

            if (!rowsTweet.equals("")){
                //se recupera el nodo

                long id_node = Long.valueOf(rowsTweet);
                nodoTweet = db.getNodeById(id_node);

            }else{
                System.out.println("\nQuery recoverNodeTweet is null.\n");
            }

            tx_nodeTweet.success();
            tx_nodeTweet.close();
            /*try {
                tx_nodeTweet.success();
            }finally {
                tx_nodeTweet.close();
            }*/

        }catch(Exception exce_t){
            System.out.println("ERROR IN TRANSACTION tx_nodeTweet: "+exce_t);
        }

        return nodoTweet;
    }

    //Test if a Tweet does exists given its Id
    public boolean tweetExists(long tweet_id){
        boolean exists_t= true;

        rowsTweet = "";
        try (Transaction tx_tweet = db.beginTx();
             Result result = db.execute( "MATCH (t{tweet_id: "+tweet_id+"}) RETURN  t.tweet_id" )
        ){
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                for ( Map.Entry<String,Object> column : row.entrySet() )
                {
                    rowsTweet += column.getKey() + ": " + column.getValue() + "; ";
                }
                //rowsTweet += "\n";
            }

            if (!rowsTweet.equals("")){
                exists_t = true;
            }else{
                exists_t = false;
            }

            tx_tweet.success();
            tx_tweet.close();
            /*try {
                tx_tweet.success();
            }finally {
                tx_tweet.close();
            }*/

        }catch(Exception excep){
            System.out.println("ERROR IN TRANSACTION tx_tweet: "+excep);
        }

        return exists_t;
    }


    //Testing if event (hashtag) does exist
    public boolean eventExists(String ev){
        boolean exists_e = true;

        rowsEvent = "";
        try (Transaction tx_event = db.beginTx();
             Result result = db.execute( "MATCH (e{Hashtag: \'"+ev+"\'}) RETURN  e.Hashtag" )
        ){
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                for ( Map.Entry<String,Object> column : row.entrySet() )
                {
                    rowsEvent += column.getKey() + ": " + column.getValue() + "; ";
                }
                rowsEvent += "\n";
            }

            if (!rowsEvent.equals("")){
                exists_e = true;
            }else{
                exists_e = false;
            }

            tx_event.success();
            tx_event.close();
            /*try {
                tx_event.success();
            }finally {
                tx_event.close();
            }*/

        }catch(Exception exce){
            System.out.println("ERROR IN TRANSACTION tx_event: "+exce);
        }

        return exists_e;
    }


    //test if has_hashtag is not repeated, by tweet_id and hashtag
    public boolean hasHashtagExist(long tweet_id, String hash){
        boolean exists_h_h = false;
        rowsH_H = "";

        try (Transaction tx_h_h = db.beginTx();
             Result result = db.execute( "MATCH (u{User_id: "+tweet_id+"})-[r:HAS_HASHTAG]->(e{Hashtag: '"+hash+"'}) RETURN  r" ) //e{Hashtag:"+hash+"}
        ){
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                for ( Map.Entry<String,Object> column : row.entrySet() )
                {
                    rowsH_H += column.getKey() + ": " + column.getValue() + "; ";
                }
                //rowsH_H += "\n";
            }

            if (!rowsH_H.equals("")){
                exists_h_h = true;
            }

            tx_h_h.success();
            tx_h_h.close();
            /*try {
                tx_h_h.success();
            }finally {
                tx_h_h.close();
            }*/

        }catch(Exception exce){
            System.out.println("ERROR IN TRANSACTION tx_h_h: "+exce);
        }

        return exists_h_h;
    }


    //test if mention is not repeated, by tweet_id and user_id
    public boolean mentionsExist (long tweet_id, long user_id){
        boolean exists_mention = false;
        rowsMention = "";


        try (Transaction tx_mention = db.beginTx();
             Result result = db.execute( "MATCH (t{tweet_id: "+tweet_id+"})-[r:MENTION]->(u{User_id: "+user_id+"}) RETURN  r" ) //added the 'OPTIONAL' flag recently
        ){
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                for ( Map.Entry<String,Object> column : row.entrySet() )
                {
                    rowsMention += column.getKey() + ": " + column.getValue() + "; ";
                }
                //rowsMention += "\n";
            }

            System.out.println(" ----------------_> rowsMention: "+rowsMention);
            if (!rowsMention.equals("") /*|| (!rowsMention.equals(null))*/ ){
                exists_mention = true;
            }else{
                exists_mention = false;
            }

            tx_mention.success();
            tx_mention.close();
            /*try {
                tx_mention.success();
            }finally {
                tx_mention.close();
            }*/

        }catch(Exception excep){
            System.out.println("ERROR IN TRANSACTION tx_mention: "+excep);
        }

        return exists_mention;
    }


    //test if create is not repeated, by user_id and tweet_id
    public boolean createExists(long user_id, long tweet_id){
        boolean exists_create = false;
        rowsCreate = "";

        try (Transaction tx_create = db.beginTx();
             Result result = db.execute( "MATCH (u{User_id: "+user_id+"})-[r:CREATES]->(t{tweet_id: "+tweet_id+"}) RETURN  r" )
        ){
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                for ( Map.Entry<String,Object> column : row.entrySet() )
                {
                    rowsCreate += column.getKey() + ": " + column.getValue() + "; ";
                }
                //rowsCreate += "\n";
            }
            if (!rowsCreate.equals("")){
                exists_create = true;
            }

            tx_create.success();
            tx_create.close();
            /*try {
                tx_create.success();
            }finally {
                tx_create.close();
            }*/


        }catch(Exception except){
            System.out.println("ERROR IN TRANSACTION tx_create: " + except);
        }

        return exists_create;
    }

    /* Usado para recuperar usuario en MENTION */
    public User giveMeUser(final long idd, final String name, final String screenname) {
        User u = new User() {
            @Override
            public long getId() {
                return idd;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getScreenName() {
                return screenname;
            }

            @Override
            public String getLocation() {
                return "";
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public boolean isContributorsEnabled() {
                return false;
            }

            @Override
            public String getProfileImageURL() {
                return "";
            }

            @Override
            public String getBiggerProfileImageURL() {
                return "";
            }

            @Override
            public String getMiniProfileImageURL() {
                return "";
            }

            @Override
            public String getOriginalProfileImageURL() {
                return "";
            }

            @Override
            public String getProfileImageURLHttps() {
                return "";
            }

            @Override
            public String getBiggerProfileImageURLHttps() {
                return "";
            }

            @Override
            public String getMiniProfileImageURLHttps() {
                return "";
            }

            @Override
            public String getOriginalProfileImageURLHttps() {
                return "";
            }

            @Override
            public boolean isDefaultProfileImage() {
                return false;
            }

            @Override
            public String getURL() {
                return "";
            }

            @Override
            public boolean isProtected() {
                return false;
            }

            @Override
            public int getFollowersCount() {
                return 0;
            }

            @Override
            public Status getStatus() {
                return null;
            }

            @Override
            public String getProfileBackgroundColor() {
                return "";
            }

            @Override
            public String getProfileTextColor() {
                return "";
            }

            @Override
            public String getProfileLinkColor() {
                return "";
            }

            @Override
            public String getProfileSidebarFillColor() {
                return "";
            }

            @Override
            public String getProfileSidebarBorderColor() {
                return "";
            }

            @Override
            public boolean isProfileUseBackgroundImage() {
                return false;
            }

            @Override
            public boolean isDefaultProfile() {
                return false;
            }

            @Override
            public boolean isShowAllInlineMedia() {
                return false;
            }

            @Override
            public int getFriendsCount() {
                return 0;
            }

            @Override
            public Date getCreatedAt() {
                return null;
            }

            @Override
            public int getFavouritesCount() {
                return 0;
            }

            @Override
            public int getUtcOffset() {
                return 0;
            }

            @Override
            public String getTimeZone() {
                return "";
            }

            @Override
            public String getProfileBackgroundImageURL() {
                return "";
            }

            @Override
            public String getProfileBackgroundImageUrlHttps() {
                return "";
            }

            @Override
            public String getProfileBannerURL() {
                return "";
            }

            @Override
            public String getProfileBannerRetinaURL() {
                return "";
            }

            @Override
            public String getProfileBannerIPadURL() {
                return "";
            }

            @Override
            public String getProfileBannerIPadRetinaURL() {
                return "";
            }

            @Override
            public String getProfileBannerMobileURL() {
                return "";
            }

            @Override
            public String getProfileBannerMobileRetinaURL() {
                return "";
            }

            @Override
            public boolean isProfileBackgroundTiled() {
                return false;
            }

            @Override
            public String getLang() {
                return "";
            }

            @Override
            public int getStatusesCount() {
                return 0;
            }

            @Override
            public boolean isGeoEnabled() {
                return false;
            }

            @Override
            public boolean isVerified() {
                return false;
            }

            @Override
            public boolean isTranslator() {
                return false;
            }

            @Override
            public int getListedCount() {
                return 0;
            }

            @Override
            public boolean isFollowRequestSent() {
                return false;
            }

            @Override
            public URLEntity[] getDescriptionURLEntities() {
                return new URLEntity[0];
            }

            @Override
            public URLEntity getURLEntity() {
                return null;
            }

            @Override
            public String[] getWithheldInCountries() {
                return new String[0];
            }

            @Override
            public int compareTo(User user) {
                return 0;
            }

            @Override
            public RateLimitStatus getRateLimitStatus() {
                return null;
            }

            @Override
            public int getAccessLevel() {
                return 0;
            }
        };

        return u;
    }




    /* - onMessage()
     * When agent receives a message by other agent
     * */
    public void onMessage(ACLMessage msg){

        nodoUsuario = null;
        nodoUsuarioMencionado = null;
        nodoTweet = null;
        relationship_creates = null; // User to Tweet
        relationship_has_hashtag = null; // Tweet to Event
        relationship_mention = null; // Tweet to User

        //HASHTAG OBTAINED
        if (msg.getOntology().equals("spreading hashtag")){
            if (!already_have_hashtag) {
                hashtag = msg.getContent();
                habemus_hashtag = true;
                System.out.println("(Neo4jAgent) -> Hashtag recibido: " + hashtag);
                DB_PATH = "/home/carou/tests_sin_hashs/" + hashtag + "";
                System.out.println("DB_PATH: " + DB_PATH);

                //create database
                try {
                    db = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
                    registerShutdownHook(db);
                    //delete recursively at beginning
                    //FileUtils.deleteRecursively(new File(DB_PATH));
                    System.out.println("\nCREADA LA DB CORRECTAMENTE\n");
                } catch (Exception e_create_db) {
                    System.out.println("ERROR: database cannot be created -> " + e_create_db);

                }

                already_have_hashtag = true;
            }

        }

        //TWEET OBTAINED
        if (msg.getOntology().equals("Tweet") /*&& already_have_hashtag*/) {

            jsonTweet = msg.getContent();



            //System.out.println("\n\tJSONTWEET: "+jsonTweet+"\n");


            // END OF TWEETS RECEIVED. It returns a number of total tweets retreived.
            if (!jsonTweet.startsWith("{")) {
                System.out.println("el json tweet no empieza por { -> " + jsonTweet + "\n");
                int numero_fin = Integer.parseInt(jsonTweet);
                System.out.println("\nnumero_fin: " + numero_fin + "\n");
                System.out.println("\ncont: " + (cont - 1) + "\n");

                if ((cont - 1) == numero_fin) { //si ya ha recibido todos los tweets que ha enviado AgentTweet --> Envía señal a Boss \ before was cont - 1

                    System.out.println("\n---> TERMINADA LA INSERCIÓN DE TWEETS. PROCEDIENDO A CERRAR...\n");

                    //// message to VisualAgent
                    try {
                        main_messages_thread.setSender(this.getAid());
                        main_messages_thread.addReceiver(new AgentID("AgentVisual"));
                        main_messages_thread.setContent("--> TWEETS INSERTION FINISHED!. "+numero_fin+" TWEETS INSERTED IN DB. \nCLOSED TASK.\n");
                        main_messages_thread.setOntology("Show this! - form Neo4j");
                        this.send(main_messages_thread);

                    }catch(Exception e_mes_va){
                        System.out.println("Error sending message to VisualAgent: "+e_mes_va);
                    }
                    //// end

                    //A message for the ManagerAgent is sent
                    end_neo4j_agent = new ACLMessage(ACLMessage.INFORM);
                    end_neo4j_agent.setSender(this.getAid());
                    end_neo4j_agent.addReceiver(new AgentID("BossAgent"));
                    end_neo4j_agent.setContent("END: Neo4jAgent");

                    try {
                        this.send(end_neo4j_agent); //---->(ManagerAgent)
                        System.out.println("\nNeo4jAgent ya ha enviado el mensaje de FIN\n");
                    } catch (Exception e) {
                        System.out.println("\nERROR AL ENVIAR: " + e + "\n");
                    }

                    db.shutdown();

                } else {
                    System.out.println("\nSomething wrong is hapenning with the variables: cont: " + cont + " | numero_fin: " + numero_fin + "\n");
                }

                //no es FIN
            } else {
                //TWEET OBTAINED



                try {
                    tweet = TwitterObjectFactory.createStatus(jsonTweet);

                    contRest++;
                    System.out.println("\n\n\nTWEET RECIBIDO(" + (cont++) + "): " + tweet.getText() + "\n");

                } catch (TwitterException ex) {
                    ex.printStackTrace();
                }


                System.out.println("\n------------------------------------------------------------ ADDING ELEMENTS TO BD...\n");

                //Main transaction:
                try (Transaction tx = db.beginTx()) {

                    //Se debe comprobar si cualquier nodo existe en la BD antes de insertar (Hay retweets y menciones de tweets que el texto es el mismo pero cambia el Id)

                    System.out.println("TESTING IF USER "+tweet.getUser().getId()+" EXISTS...");
                    if (!userExists(tweet.getUser().getId())) {
                        // si no existe ---> se crea
                        //Creates node USER
                        System.out.println("--> Creating user...");

                        createUserNode(tweet.getUser());
                    } else {
                        // si ya existe ---> se recupera el nodo usuario
                        nodoUsuario = recoverNodeUser(tweet.getUser());
                        System.out.println("recovered nodoUsuario: "+nodoUsuario.toString());
                    }


                    //Creates node TWEET
                    if (!tweetExists(tweet.getId())) {
                        // si no existe ---> se crea
                        System.out.println("--> CREATING TWEET NODE...");
                        createNodeTweet(tweet);
                    }else {
                        // si ya existe ---> se recupera el nodo tweet
                        System.out.println("--> Tweet already exist on DB");
                        nodoTweet = recoverNodeTweet(tweet);
                    }

                    //Creates node EVENT (if it does not exist...)
                    if (!eventExists(hashtag)) {
                        // si no existe ---> se crea
                        nodoEvento = db.createNode(Tipo.EVENT);
                        nodoEvento.setProperty("Hashtag", hashtag);
                        System.out.println("--> CREATED EVENT NODE (" + nodoEvento.getProperty("Hashtag") + ")\n");

                    } else {
                        //si existe ---> lo recuperamos para poder usarlo en las relaciones
                        nodoEvento = recoverEventNode(hashtag);
                    }

                    //TWEET to EVENT ---> HAS_HASHTAG
                    if (!hasHashtagExist(tweet.getId(), hashtag)) {
                        //si no existe ---> se crea la relación HAS_HASHTAG
                        try {
                            relationship_has_hashtag = nodoTweet.createRelationshipTo(nodoEvento, Relaciones.HAS_HASHTAG);
                            System.out.println("---> CREATED RELATIONSHIP: (" + tweet.getId() + ")------ HAS_HASHTAG ------->(" + hashtag + ")");
                        }catch(Exception e_hh){
                            System.out.println("ERROR EN H_H ->"+e_hh+" | tweet.getId(): "+tweet.getId()+" | hashtag: "+hashtag+"\n");
                        }



                    }


                    //comprobar también si la relación de los usuarios existe??
                    // USER to TWEET ---> CREATES
                    //System.out.println("\nCOMPROBACIÓN CREATES...........");
                    if (!createExists(tweet.getUser().getId(), tweet.getId())) {
                        //System.out.println("\nRealizando la operación de CREATES...\n");
                        relationship_creates = nodoUsuario.createRelationshipTo(nodoTweet, Relaciones.CREATES);
                        System.out.println("---> CREATED RELATIONSHIP: (@" + tweet.getUser().getScreenName() + ")------ CREATES ----->(" + tweet.getText() + ")\n");
                    }


                    //TWEET to USER --> MENTION
                    //Primero hay que crear el nodo que menciona (usuario)
                    System.out.println("\nRECOVERING MENTIONED USERS.........");
                    UserMentionEntity[] users_mention = tweet.getUserMentionEntities();

                    for (int ji = 0; ji < users_mention.length; ji++) {
                        System.out.println("Usuario mencionado nº" + (ji + 1) + ": " + users_mention[ji].getId() + ", " + users_mention[ji].getName()
                                + ", "
                                + users_mention[ji].getScreenName());

                        //Necesitamos nuevo user--> lo creamos

                        try {
                            User u = giveMeUser(users_mention[ji].getId(), users_mention[ji].getName(), users_mention[ji].getScreenName());

                            //System.out.println("----------------> USER: "+u.toString()+"\n");

                            System.out.println("\nCreating and saving new user mentioned");

                            if (!userExists(u.getId())) { // si no existe ---> se crea

                                try {//TEST IF IT WORKS!
                                    //createUserNode(u); //createUser trabaja con nodoUsuario
                                    nodoUsuarioMencionado = db.createNode(Tipo.USER);
                                    nodoUsuarioMencionado.setProperty("User_id", u.getId());
                                    nodoUsuarioMencionado.setProperty("User_name", u.getName());
                                    nodoUsuarioMencionado.setProperty("User_screen_name", u.getScreenName());

                                    //nodoUsuarioMencionado = recoverNodeUser(u);

                                    if (!mentionsExist(tweet.getId(), u.getId())) { //comprueba que la mención no existe y la crea
                                        relationship_mention = nodoTweet.createRelationshipTo(nodoUsuarioMencionado, Relaciones.MENTION);
                                        System.out.println("\n---> CREATED RELATIONSHIP: (" + tweet.getText() + ")----- MENTION ----->(@" + u.getScreenName() + ")");
                                        //tx.success();
                                    }

                                } catch (Exception except) {
                                    System.out.println("\nSe ha intentado crear usuario mencionado, pero falla al crearlo (User) o al recuperar el nodo\n\t" + except + "");
                                }

                            } else {
                                // si existe ---> Recupera el nodo usuario para crear la relación MENTION -----------------
                                //System.out.println("\nEl nodo existe. Vamos a Recuperarlo... \n");
                                nodoUsuarioMencionado = recoverNodeUser(u);
                                if (nodoUsuarioMencionado != null) {
                                    //System.out.println("Nodo recuperado!\n");

                                    if (!mentionsExist(tweet.getId(), u.getId())) { //comprueba que la mención no existe y la crea
                                        relationship_mention = nodoTweet.createRelationshipTo(nodoUsuarioMencionado, Relaciones.MENTION);
                                        System.out.println("\n---> CREATED RELATIONSHIP: (" + tweet.getText() + ")----- MENTION ----->(@" + u.getScreenName() + ")");
                                        //tx.success();
                                    }
                                } else {
                                    System.out.println("El nodo es null. No se inserta MENTION :(\n");
                                }

                            }
                        } catch (Exception e_us_mention) {
                            System.out.println("\nError creating User for Mention relationship: " + e_us_mention + "");
                        }

                    }


                    System.out.println("\n---------------------------------------------------------- TWEET ADDED TO DB! (id: " + tweet.getId() + ") \n");
                    //cont2++;
                    //try {
                       /* if (cont2 % 100 == 0) {
                        //relationship_mention = nodoTweet.createRelationshipTo(nodoUsuarioMencionado, Relaciones.MENTION);
                            tx.success();
                            //tx.close();
                            //cont2 = 0;

                        }*/

                    tx.success();
                    tx.close();

                    /*try {
                        tx.success();
                        tx.close();
                    } catch (Exception exceptt) {
                        System.out.println("\nError closing transaction: " + exceptt + "\n");
                    }*/


                    /*try {
                        tx.success();
                    }finally {
                        tx.close();
                    }*/

                } catch (Exception exc) {
                    System.out.println("\nERROR CREATING MAIN TRANSACTION: " + exc + "\n");
                    //String mens = exc.getMessage();
                    //System.out.println("\nMensaje de error: " + mens + "\n\t" + exc.getLocalizedMessage() + "\n");

                }


                //db.shutdown();

            }//end try-catch inicial
        }//end if-else


	}//end onMessage()

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
