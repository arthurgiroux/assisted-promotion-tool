package main;

import common.DBHelper;
import echonest.EchoNest;
import facebook.FacebookPosts;

public class DataExtraction {

  public static void main(String args[]) {
    if (args.length > 0 && args[0] != null && args[0].equals("empty")){
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
  }
}
