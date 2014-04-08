package data_extraction.main;

import common.DBHelper;
import data_extraction.echonest.EchoNest;
import data_extraction.facebook.FacebookPosts;
import data_extraction.twitter.TwitterPosts;

public class DataExtraction {

  public static void main(String args[]) {
    
    if (args.length > 0 && args[0] != null && args[0].equals("empty")) {
      System.out.println("Emptying database");
      DBHelper dbHelper = DBHelper.getInstance();
      dbHelper.emptyAll();
    }
    // EchoNest + Freebase phase:
    EchoNest echonest = new EchoNest();
    echonest.run();
    
    
    // Facebook and Twitter are thread because we can execute them in parallel
    FacebookPosts fb = new FacebookPosts();
    fb.start();
    
    TwitterPosts tw = new TwitterPosts();
    tw.start();
  }
}
