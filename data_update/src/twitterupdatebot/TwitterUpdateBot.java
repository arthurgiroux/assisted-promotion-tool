/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twitterupdatebot;

import com.mongodb.BasicDBList;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import common.DBHelper;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

/**
 *
 * @author mikaelcastellani
 */
public class TwitterUpdateBot {
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Twitter twitter = TwitterFactory.getSingleton();

        System.out.println("starting twitter");

        DBHelper db = DBHelper.getInstance();

        DBCursor cursor = db.findAllArtists().addOption(Bytes.QUERYOPTION_NOTIMEOUT);

        try {

            while (cursor.hasNext()) {

                DBObject item = cursor.next();

                twitter.addRateLimitStatusListener(new RateLimitStatusListener() {

                    @Override
                    public void onRateLimitStatus(RateLimitStatusEvent rlse) {

                    }

                    @Override
                    public void onRateLimitReached(RateLimitStatusEvent rlse) {
                        try {
                            //Sleeping 
                            System.out.println("Twitter sleeping...");
                            sleep(60001); //1min
                        } catch (InterruptedException ex) {
                            Logger.getLogger(TwitterUpdateBot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

                
                if (item.get("twitter_followers") == null) {
                    BasicDBList twitter_id = (BasicDBList) item.get("twitter_id");
                    
                    for (Object id : twitter_id) {
                        try{
                    System.out.println("Twitter: treating " + item.get("name") + " Twitter account : " + id.toString());
                    Integer followers = twitter.showUser(id.toString()).getFollowersCount();
                    db.updateArtistFollowers(item, followers);
                        }catch(Exception e){
                            //Skip record for example if the twitter account doesn't exist anymore
                        }
                    }
                }

            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            cursor.close();
        }

    }

}
