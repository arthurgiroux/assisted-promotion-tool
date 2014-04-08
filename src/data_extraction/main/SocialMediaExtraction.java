package data_extraction.main;

import common.DBHelper;
import data_extraction.facebook.FacebookPosts;
import data_extraction.twitter.TwitterPosts;

public class SocialMediaExtraction {

  public static void main(String[] args) {
    System.out.println("Emptying facebook and twitter databases");
    DBHelper dbHelper = DBHelper.getInstance();
    dbHelper.emptyFacebookAndTwitterCollections();
    
    System.out.println("Starting facebook data gathering");
    FacebookPosts fb = new FacebookPosts();
    fb.start();
    
    System.out.println("Starting facebook data gathering");
    TwitterPosts tw = new TwitterPosts();
    tw.start();
  }

}
