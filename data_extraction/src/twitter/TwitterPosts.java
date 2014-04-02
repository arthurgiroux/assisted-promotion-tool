package twitter;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import common.DBHelper;
import twitter4j.HashtagEntity;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterPosts extends Thread {

  public void run() {
    Twitter twitter = TwitterFactory.getSingleton();

    System.out.println("starting twitter");

    DBHelper db = DBHelper.getInstance();

    DBCursor cursor = db.findAllArtists();

    try {

      while (cursor.hasNext()) {

        DBObject item = cursor.next();

        System.out.println("Twitter: treating " + item.get("name"));
        BasicDBList twitter_id = (BasicDBList) item.get("twitter_id");

        if (twitter_id != null) {
          for (Object id : twitter_id) {
            String str_id = (String) id;

            for (int i = 1; i < 34; i++) {
              Paging paging = new Paging(i, 100);// 2 is ‘page 2 of the results’, 100 is the number of results in the page
              
              List<Status> statuses = new ArrayList<Status>();
              try {
                statuses = twitter.getUserTimeline(str_id, paging);
                System.out.println("Page " + i + " for " + item.get("name"));
                
              } catch (TwitterException e) {
                System.out.println("Rate limit");
                try {
                  sleep(900001); //15min
                } catch (InterruptedException e2) {
                  System.err.println("Something went wrong while sleeping on twitter");
                  e2.printStackTrace();
                  
                }
                
                try {
                  statuses = twitter.getUserTimeline(str_id, paging);
                } catch (TwitterException e1) {
                  e1.printStackTrace();
                }
                System.out.println("Page " + i + " for " + item.get("name"));
              }
              
              if (statuses.isEmpty()) {
                System.out.println("Finished parsing for " + item.get("name"));
                break;
              }
              System.out.println("Inserting " + statuses.size() + " tweets into the database for " + item.get("name"));
              for (Status status : statuses) {
                ArrayList<String> hashtags = new ArrayList<String>();
                HashtagEntity[] curHash = status.getHashtagEntities();

                for (int j = 0; j < curHash.length; ++j) {
                  hashtags.add(curHash[j].getText());
                }

                db.insertTweet((ObjectId) item.get("_id"), status.getCreatedAt(), status.getUser().getScreenName(), status.getText(), status.getRetweetCount(), hashtags);
              }
            }
          }
        }
        else {
          System.out.println("Discarding because no twitter id found");
        }
      }
    }
    finally {
      cursor.close();
    }
  }
}