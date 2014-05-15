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
  private final static int MIN_RESULTS_NB = 10;

  private final static float FACEBOOK_LIKES_WEIGHT = 0.2f;
  private final static float TWITTER_FOLLOWERS_WEIGHT = 0.2f;
  private final static float ALBUMS_COUNT_WEIGHT = 0.2f;
  private final static float REGION_WEIGHT = 0.2f;
  private final static float CATEGORY_WEIGHT = 0.2f;

  private int[] counters = new int[TYPE.values().length];
  private int[] sums = new int[TYPE.values().length];

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
  }

  public Map<String, Double> recommend() {
    if (FACEBOOK_LIKES_WEIGHT + TWITTER_FOLLOWERS_WEIGHT + ALBUMS_COUNT_WEIGHT + REGION_WEIGHT + CATEGORY_WEIGHT > 1) {
      System.out.println("Check the weights");
      System.exit(0);
    }

    DBHelper db = DBHelper.getInstance();

    DBCursor cursor = db.findMatrixRows().addOption(Bytes.QUERYOPTION_NOTIMEOUT);

    results = new HashMap<DBObject, Double>();
    
    while (cursor.hasNext()) {
      DBObject obj = cursor.next();

      results.put(obj, computeSimilarity(obj));
    }
    
    similarResults = getSimilarResults(results);
    

    for(DBObject matrixRow: similarResults){
      for (int i = 0; i < Event.NAMES.length; i++) {
        boolean hasAttribute = (Boolean) matrixRow.get("has_" + Event.NAMES[i]);
        if (hasAttribute) {
          long attributeValue = (Long) matrixRow.get("days_" + Event.NAMES[i]);

          counters[i]++;
          sums[i] += attributeValue;
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

  private double computeSim(int n1, int n2) {
    return (n1 == n2) ? 1 : 1 - ((double) Math.abs(n1 - n2)) / Math.max(n1, n2);
  }

  public double computeSimilarity(DBObject obj) {
    
    int sameRegion = region.equals((String) obj.get(REGION)) ? 1 : 0;

    int sameCategories = (isOfsameCategory(obj) ? 1 : 0);
    
    double cossimFb = computeSim(facebookLikes, (Integer) (obj.get(FACEBOOKLIKES)));
    double cossimTw = computeSim(twitterFollowers, (Integer) (obj.get(TWITTERFOLLOWERS)));
    double cossimAlbums = computeSim(albumsCount, (Integer) (obj.get(NUMBEROFALBUMS)));

    return (FACEBOOK_LIKES_WEIGHT * cossimFb
        + TWITTER_FOLLOWERS_WEIGHT * cossimTw
        + ALBUMS_COUNT_WEIGHT * cossimAlbums
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
