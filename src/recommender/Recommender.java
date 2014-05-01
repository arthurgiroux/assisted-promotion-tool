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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

  public static float thresholdCosim = 0.75f;

  public static float FBLikesWeight = 0.3f;
  public static float TwitterFollowersWeight = 0.3f;
  public static float NumberOfAlbumsWeight = 0.1f;
  public static float RegionWeight = 0.2f;
  public static float CategoryWeight = 0.1f;

  private int[] counters = new int[TYPE.values().length];
  private int[] sums = new int[TYPE.values().length];

  private String region;
  private String[] categories;
  private int facebookLikes;
  private int twitterFollowers;
  private int albumsCount;

  public Recommender(String region, String[] categories, 
      int facebookLikes, int twitterFollowers, int albumsCount) {
    this.region = region;
    this.categories = categories;
    this.facebookLikes = facebookLikes;
    this.twitterFollowers = twitterFollowers;
    this.albumsCount = albumsCount;
  }

  public Map<String, Double> recommend() {
    if (FBLikesWeight + TwitterFollowersWeight + NumberOfAlbumsWeight + RegionWeight + CategoryWeight > 1) {
      System.out.println("Check the weights");
      System.exit(0);
    }

    DBHelper db = DBHelper.getInstance();

    DBCursor cursor = db.findMatrixRows().addOption(Bytes.QUERYOPTION_NOTIMEOUT);

    Set<DBObject> similarResults = new HashSet<DBObject>();
    
    double max = 0;
    
    while (cursor.hasNext()) {
      DBObject obj = cursor.next();

      double cosim = computeSimilarity(obj);
      
      if (cosim > thresholdCosim) {
        similarResults.add(obj);
      }
    }
    

    for(DBObject matrixRow: similarResults){
      for (int i = 0; i < Event.NAMES.length; i++) {
        boolean hasAttribute = (boolean) matrixRow.get("has_" + Event.NAMES[i]);
        if (hasAttribute) {
          long attributeValue = (long) matrixRow.get("days_" + Event.NAMES[i]);

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
    
    return sortByValue(finalResult);
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
    if (n1 == n2) return 1;
    return 1 - ((double) Math.abs(n1 - n2)) / Math.max(n1, n2);
  }

  public double computeSimilarity(DBObject obj) {
    
    int sameRegion = region.equals((String) obj.get(REGION)) ? 1 : 0;

    int sameCategories = (isOfsameCategory(obj) ? 1 : 0);
    
    double cossimFb = computeSim(facebookLikes, (Integer) (obj.get(FACEBOOKLIKES)));
    double cossimTw = computeSim(twitterFollowers, (Integer) (obj.get(TWITTERFOLLOWERS)));
    double cossimAlbums = computeSim(albumsCount, (Integer) (obj.get(NUMBEROFALBUMS)));

    return (FBLikesWeight * cossimFb
        + TwitterFollowersWeight * cossimTw
        + NumberOfAlbumsWeight * cossimAlbums
        + RegionWeight * sameRegion
        + CategoryWeight * sameCategories);
  }


  private static Map<String, Double> sortByValue(Map<String, Double> map) {
    List<Entry<String, Double>> sortedEntries = new LinkedList<Entry<String, Double>>(map.entrySet());

    Collections.sort(sortedEntries, new Comparator<Entry<String, Double>>() {
      public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
        return o1.getValue().compareTo(o2.getValue());
      }
    });

    Map<String, Double> result = new LinkedHashMap<String, Double>();
    for (Entry<String, Double> entry : sortedEntries) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }
}
