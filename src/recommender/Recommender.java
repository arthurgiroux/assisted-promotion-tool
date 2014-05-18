/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender;

import static common.DBHelper.CATEGORY;
import static common.DBHelper.FACEBOOKLIKES;
import static common.DBHelper.NUMBEROFALBUMS;
import static common.DBHelper.REGION;
import static common.DBHelper.TWITTERFOLLOWERS;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.BasicDBList;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import common.DBHelper;
import common.Event;
import common.Event.TYPE;

/**
 *
 * @author mikaelcastellani
 */
public class Recommender {

  private final static float THRESHOLD = 0.75f;
  private final static int MIN_RESULTS_NB = 5;

  /*private final static float FACEBOOK_LIKES_WEIGHT = 0.2f;
  private final static float TWITTER_FOLLOWERS_WEIGHT = 0.2f;
  private final static float ALBUMS_COUNT_WEIGHT = 0.2f;*/
  private final static float CLUSTER_WEIGHT = 0.2f;
  private final static float REGION_WEIGHT = 0.4f;
  private final static float CATEGORY_WEIGHT = 0.4f;
  
  private DBHelper db;
  private DBObject closestClusterCenter;

  private double[] counters = new double[TYPE.values().length];
  private double[] sums = new double[TYPE.values().length];

  private String region;
  private String[] categories;
  private int facebookLikes;
  private int twitterFollowers;
  private int albumsCount;
  
  private Map<DBObject, Double> results = null;
  private List<DBObject> similarResults = null;
  
  public Recommender(String region, String[] categories, 
      int facebookLikes, int twitterFollowers, int albumsCount) {
    this.region = region;
    this.categories = categories;
    this.facebookLikes = facebookLikes;
    this.twitterFollowers = twitterFollowers;
    this.albumsCount = albumsCount;
    
    db = DBHelper.getInstance();
    
    findClosestCluster();
  }
  
  private void findClosestCluster() {
    DBCursor clusterCursor = db.findClusterCenters();
    
    double minDist = -1;
    closestClusterCenter = null;
    
    while (clusterCursor.hasNext()) {
      DBObject current = clusterCursor.next();
      
      double fl = (Double) current.get("fbLikes");
      double tf = (Double) current.get("twitterFollowers");
      double ac = (Double) current.get("albumCount");
      
      double dist = euclideanDistance(fl, tf, ac);
      
      if (closestClusterCenter == null || dist < minDist) {
        minDist = dist;
        closestClusterCenter = current;
      }
    }
  }

  public Map<String, Double> recommend() {
    if (CLUSTER_WEIGHT + REGION_WEIGHT + CATEGORY_WEIGHT > 1) {
      System.out.println("Check the weights");
      System.exit(0);
    }

    DBCursor cursor = db.findMatrixRows().addOption(Bytes.QUERYOPTION_NOTIMEOUT);

    results = new HashMap<DBObject, Double>();
    
    while (cursor.hasNext()) {
      DBObject obj = cursor.next();
      
      results.put(obj, computeSimilarity(obj));
    }
    
    similarResults = getSimilarResults(results);
    
    String[] sources = {"twitter", "facebook"};

    for(DBObject matrixRow: similarResults){
      for (int i = 0; i < Event.NAMES.length; i++) {
        for (String source : sources) {
          boolean hasAttribute = (matrixRow.get("days_" + source + "_" + Event.NAMES[i]) != null);
          if (hasAttribute) {
            int attributeValue = (Integer) matrixRow.get("days_" + source + "_" + Event.NAMES[i]);
            
            Double eventWeight = (Double) matrixRow.get("event_score_" + Event.NAMES[i]);
            eventWeight = (eventWeight == null ? 0.5 : eventWeight);
            
            System.out.println(eventWeight);
            
            counters[i] += eventWeight;
            sums[i] += eventWeight * attributeValue;
          }
        }
      }
    }
    
    HashMap<String, Double> finalResult = new HashMap<String, Double>();

    for (int i = 0; i < Event.NAMES.length; i++) {
      if (counters[i] > 0) {
        finalResult.put("days_"+Event.NAMES[i], ((double) sums[i]) / counters[i]);
      }
    }
    
    return sortByValue(finalResult, false);
  }
  
  public Map<String, String> getStats() {
    if (results == null || similarResults == null) {
      recommend();
    }
    
    Entry<DBObject, Double> bestMatch = null;
    double simSum = 0;
    
    for (Entry<DBObject, Double> r : results.entrySet()) {
      if (bestMatch == null || bestMatch.getValue() < r.getValue()) {
        bestMatch = r;
      }
      simSum += r.getValue();
    }
    
    DecimalFormat df = new DecimalFormat( "0.000" );
    HashMap<String, String> stats = new HashMap<String, String>();
    
    stats.put("average_sim_overall", df.format(simSum / results.size())+"");
    
    String bestMatchArtistName = (String) bestMatch.getKey().get("artistName");
    
    stats.put("best_match", bestMatchArtistName);
    
    simSum = 0;
    for (DBObject o : similarResults) {
      Double sim = results.get(o);
      simSum += (sim == null) ? 0 : sim;
    }
    
    stats.put("artists_count", similarResults.size() +"");
    stats.put("average_sim", df.format(simSum / similarResults.size())+"");
    
    return stats;
  }
  
  private List<DBObject> getSimilarResults(Map<DBObject, Double> map) {
    List<DBObject> l = new LinkedList<DBObject>();
    
    // Adds all results with sim > THRESHOLD
    for (Entry<DBObject, Double> e : map.entrySet()) {
      if (e.getValue() > THRESHOLD) {
        l.add(e.getKey());
      }
    }
    
    // Fills the list with best objects until it contains enough results
    while (l.size() < MIN_RESULTS_NB) {
      Entry<DBObject, Double> bestEntry = null;
      
      for (Entry<DBObject, Double> e : map.entrySet()) {
        if (bestEntry == null || 
            (e.getValue() > bestEntry.getValue() && !l.contains(e.getKey()))) 
        {
          bestEntry = e;
        }
      }
      
      l.add(bestEntry.getKey());
    }
    
    return l;
  }

  private boolean isOfsameCategory(DBObject obj) {
    BasicDBList categoryList = (BasicDBList) obj.get(CATEGORY);
    List<String> cat = Arrays.asList(categories);
    if (categoryList != null) {
      for (Object category : categoryList) {
        String categoryString = (String) category;
        if (cat.contains(categoryString)) {
          return true;
        }
      }
    }
    return false;
  }

  /*private double computeSim(int n1, int n2) {
    return (n1 == n2) ? 1 : 1 - ((double) Math.abs(n1 - n2)) / Math.max(n1, n2);
  }*/
  
  private double euclideanDistance(double fl, double tf, double ac) {
    return Math.sqrt((this.facebookLikes - fl) * (this.facebookLikes - fl)
         + (this.twitterFollowers - tf) * (this.twitterFollowers - tf)
         + (this.albumsCount - ac) * (this.albumsCount - ac));
  }
  
  private boolean isInSameCluster(DBObject obj) {
    return closestClusterCenter.get("_id").equals(obj.get("cluster_center"));
  }

  public double computeSimilarity(DBObject obj) {
    
    int sameRegion = region.equals((String) obj.get(REGION)) ? 1 : 0;
    
    int sameCategories = (isOfsameCategory(obj) ? 1 : 0);
    
    /*double cossimFb = computeSim(facebookLikes, (Integer) (obj.get(FACEBOOKLIKES)));
    double cossimTw = computeSim(twitterFollowers, (Integer) (obj.get(TWITTERFOLLOWERS)));
    double cossimAlbums = computeSim(albumsCount, (Integer) (obj.get(NUMBEROFALBUMS)));
    */
    
    int sameCluster = (isInSameCluster(obj) ? 1 : 0);
    
    return (CLUSTER_WEIGHT * sameCluster
        /*FACEBOOK_LIKES_WEIGHT * cossimFb
        + TWITTER_FOLLOWERS_WEIGHT * cossimTw
        + ALBUMS_COUNT_WEIGHT * cossimAlbums*/
        + REGION_WEIGHT * sameRegion
        + CATEGORY_WEIGHT * sameCategories);
  }


  private static <A, B extends Comparable<B>> Map<A, B> sortByValue(Map<A, B> map, final boolean ascending) {
    List<Entry<A, B>> sortedEntries = new LinkedList<Entry<A, B>>(map.entrySet());

    Collections.sort(sortedEntries, new Comparator<Entry<A, B>>() {
      public int compare(Entry<A, B> o1, Entry<A, B> o2) {
        if (ascending)
          return o1.getValue().compareTo(o2.getValue());
        else
          return o2.getValue().compareTo(o1.getValue());
      }
    });

    Map<A, B> result = new LinkedHashMap<A, B>();
    for (Entry<A, B> entry : sortedEntries) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }
}
