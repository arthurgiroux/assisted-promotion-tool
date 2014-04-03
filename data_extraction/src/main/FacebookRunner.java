package main;

import common.DBHelper;

import facebook.FacebookPosts;

public class FacebookRunner {

  public static void main(String[] args) {
    System.out.println("Emptying facebook database");
    DBHelper dbHelper = DBHelper.getInstance();
    dbHelper.emptyFacebookCollection();
    
    System.out.println("Starting facebook data gathering");
    FacebookPosts fb = new FacebookPosts();
    fb.start();
  }

}
