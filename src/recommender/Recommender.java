/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender;

import com.mongodb.BasicDBList;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import common.DBHelper;

import java.awt.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import static common.DBHelper.*;
import java.util.Date;

/**
 *
 * @author mikaelcastellani
 */
public class Recommender {

    public static float thresholdCosim = 0.9f;

    public static float FBLikesWeight = 0.4f;
    public static float TwitterFollowersWeight = 0.4f;
    public static float NumberOfAlbumsWeight = 0.0f;
    public static float RegionWeight = 0.2f;
    public static float CategoryWeight = 0.0f;
    
    public static double singleReleaseSum = 0;
    public static int singleReleaseCounter = 0;

    public static double cdReleaseShowSum = 0;
    public static int cdReleaseShowCounter = 0;

    public static double pressCampaignSum = 0;
    public static int pressCampaignCounter = 0;

    public static double presaleCampaignSum = 0;
    public static int presaleCampaignCounter = 0;

    public static double firstTweetSum = 0;
    public static int firstTweetCounter = 0;

    public static double firstFbSum = 0;
    public static int firstFbCounter = 0;

    public static double countDownSum = 0;
    public static int countDownCounter = 0;

    public static double announcementSum = 0;
    public static int announcementCounter = 0;

    public static double albumCoverSum = 0;
    public static int albumCoverCounter = 0;

    public static double interviewSum = 0;
    public static int interviewCounter = 0;

    public static double videoClipSum = 0;
    public static int videoClipCounter = 0;

    public static double teaserSum = 0;
    public static int teaserCounter = 0;
    
    

    public static void main(String[] args) {
        try {
            Date start = new Date();

            if (FBLikesWeight + TwitterFollowersWeight + NumberOfAlbumsWeight + RegionWeight + CategoryWeight > 1) {
                System.out.println("Check the weights");
                System.exit(0);
            }

            DBHelper db = DBHelper.getInstance();

            DBCursor cursor = db.findMatrixRows().addOption(Bytes.QUERYOPTION_NOTIMEOUT);

            Set<DBObject> similarResults = new HashSet<DBObject>();
            HashMap<String, Double> finalResult = new HashMap<String, Double>();

            int counter = 0;
            DBObject obj1 = cursor.next();
            while (cursor.hasNext()) {
                DBObject obj2 = cursor.next();

                Double cosim = computeSimilarity(obj1, obj2);
                //System.out.println("Cosim calculated" + cosim);
                if (cosim > thresholdCosim) {
                    counter++;
                    similarResults.add(obj2);
                    //System.out.println(obj2.toString());
                }

            }

            //System.out.println(counter + " matches found.");

            //System.out.println("Time elapsed : " + (new Date().getTime() - start.getTime()) / 1000.0 + " seconds");
            
            for(DBObject matrixRow: similarResults){
            	boolean hasSingleRelease = (boolean)matrixRow.get("has_single_release");
            	if(hasSingleRelease == true){
            		long singleReleaseDays = (long)matrixRow.get("days_single_release");
            		singleReleaseCounter++;
            		singleReleaseSum += singleReleaseDays;
            	}
            	
            	boolean hasCDRealeaseShow = (boolean)matrixRow.get("has_cd_release_show");
            	if(hasCDRealeaseShow == true){
            		long CDReleaseShowDays = (long)matrixRow.get("days_cd_release_show");
            		cdReleaseShowCounter++;
            		cdReleaseShowSum+=CDReleaseShowDays;
            		
            	}
            	
            	boolean hasPressCampaign = (boolean)matrixRow.get("has_press_campaign");
            	if(hasPressCampaign == true){
            		long pressCampaignDays = (long)matrixRow.get("days_press_campaign");
            		pressCampaignCounter++;
            		pressCampaignSum+=pressCampaignDays;
            	}
            	
            	boolean hasPresaleCampaign = (boolean)matrixRow.get("has_presale_campaign");
            	if(hasPresaleCampaign == true){
            		long presaleCampaignDays = (long)matrixRow.get("days_presale_campaign");
            		presaleCampaignCounter++;
            		presaleCampaignSum+=presaleCampaignDays;
            	}
            	
            	boolean hasFirstTweet = (boolean)matrixRow.get("has_first_tweet");
            	if(hasFirstTweet == true){
            		long firstTweetDays = (long)matrixRow.get("days_first_tweet");
            		firstTweetCounter++;
            		firstTweetSum+=firstTweetDays;
            	}
            	
            	boolean hasFirstFB = (boolean)matrixRow.get("has_first_fb");
            	if(hasFirstFB == true){
            		long firstFBDays = (long)matrixRow.get("days_first_fb");
            		firstFbCounter++;
            		firstFbSum +=firstFBDays;
            	}
            	
            	boolean hasCountdown = (boolean)matrixRow.get("has_countdown");
            	if(hasCountdown == true){
            		long countdownDays = (long)matrixRow.get("days_countdown");
            		countDownCounter++;
            		countDownSum += countdownDays;
        		}
            	
            	boolean hasAnnouncement = (boolean)matrixRow.get("has_announcement");
            	if(hasAnnouncement == true){
            		long announcementDays = (long)matrixRow.get("days_announcement");
            		announcementCounter++;
            		announcementSum+=announcementDays;
            	}
            	
            	boolean hasAlbumCover = (boolean)matrixRow.get("has_album_cover");
            	if(hasAlbumCover == true){
            		long albumCoverDays = (long)matrixRow.get("days_album_cover");
            		albumCoverCounter ++;
            		albumCoverSum+=albumCoverDays;
            	}
            	
            	boolean hasInterview = (boolean)matrixRow.get("has_interview");
            	if(hasInterview == true){
            		long interviewDays = (long)matrixRow.get("days_interview");
            		interviewCounter++;
            		interviewSum += interviewDays;
            	}
            	
            	boolean hasVideoClip = (boolean)matrixRow.get("has_video_clip");
            	if(hasVideoClip == true){
            		long videoClipDays = (long)matrixRow.get("days_video_clip");
            		videoClipCounter++;
            		videoClipSum+=videoClipDays;
            	}
            	
            	boolean hasTeaser = (boolean)matrixRow.get("has_teaser");
            	if(hasTeaser == true){
            		long teaserDays = (long)matrixRow.get("days_teaser");
            		teaserCounter++;
            		teaserSum+=teaserDays;
            	}
            }
            
            if(singleReleaseCounter > 0)
            	finalResult.put("days_single_release", singleReleaseSum/singleReleaseCounter);
            
            if(cdReleaseShowCounter > 0)
            	finalResult.put("days_cd_release_show", cdReleaseShowSum/cdReleaseShowCounter);
            
            if(pressCampaignCounter > 0)
            	finalResult.put("days_press_campaign", pressCampaignSum/pressCampaignCounter);
            
            if(presaleCampaignCounter > 0)
            	finalResult.put("days_presale_campaign", presaleCampaignSum/presaleCampaignCounter);
            
            if(firstTweetCounter > 0)
            	finalResult.put("days_first_tweet", firstTweetSum/firstTweetCounter);
            
            if(firstFbCounter > 0)
            	finalResult.put("days_first_fb", firstFbSum/firstFbCounter);
            
            if(countDownCounter > 0)
            	finalResult.put("days_countdown", countDownSum/countDownCounter);
            
            if(announcementCounter > 0)
            	finalResult.put("days_announcement", announcementSum/announcementCounter);
            
            if(albumCoverCounter > 0)
            	finalResult.put("days_album_cover", albumCoverSum/albumCoverCounter);
				
            if(interviewCounter > 0)
            	finalResult.put("days_interview", interviewSum/interviewCounter);
				
            if(videoClipCounter > 0)
            	finalResult.put("days_video_clip", videoClipSum/videoClipCounter);
				
            if(teaserCounter > 0)
            	finalResult.put("days_teaser", teaserSum/teaserCounter);
            
            /*Iterator it = finalResult.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                System.out.println(pairs.getKey() + " = " + pairs.getValue());
                it.remove(); // avoids a ConcurrentModificationException
            }*/
            
            HashMap<String, Double> sortedFinalResult = (HashMap<String, Double>)sortByValue(finalResult);
            
            boolean first = true;
            System.out.println("[");
            Iterator it = sortedFinalResult.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                if (!first) System.out.println(",");
                first = false;
                System.out.print("{\"" + pairs.getKey() + "\" : " + pairs.getValue() + "}");
                it.remove(); // avoids a ConcurrentModificationException
            }
            System.out.println("]");
            
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

    }

    public static double computeSimilarity(DBObject obj1, DBObject obj2) {

        int sameRegion = obj1.get(REGION) == obj2.get(REGION) ? 1 : 0;

        int sameCategories = 0;
        BasicDBList categoryList1 = (BasicDBList) obj1.get(CATEGORY);
        BasicDBList categoryList2 = (BasicDBList) obj2.get(CATEGORY);
        if (categoryList1 != null && categoryList2 != null) {
            for (Object category : categoryList1) {
                for (Object category2 : categoryList2) {
                    if (((String) category).equals(((String) category2))) {
                        sameCategories = 1;
                        break;
                    }
                }
            }
        }
        double cossimFb;
        double cossimTw;
        double cossimAlbums;

        if ((Integer) (obj1.get(FACEBOOKLIKES)) == (Integer) (obj2.get(FACEBOOKLIKES))) {
            cossimFb = 1;
        } else {
            cossimFb = (double) Math.min((Integer) (obj1.get(FACEBOOKLIKES)), (Integer) (obj2.get(FACEBOOKLIKES)))
                    / Math.max((Integer) (obj1.get(FACEBOOKLIKES)), (Integer) (obj2.get(FACEBOOKLIKES)));
        }

        if ((Integer) (obj1.get(TWITTERFOLLOWERS)) == (Integer) (obj2.get(TWITTERFOLLOWERS))) {
            cossimTw = 1;
        } else {
            cossimTw = (double) Math.min((Integer) (obj1.get(TWITTERFOLLOWERS)), (Integer) (obj2.get(TWITTERFOLLOWERS)))
                    / Math.max((Integer) (obj1.get(TWITTERFOLLOWERS)), (Integer) (obj2.get(TWITTERFOLLOWERS)));
        }

        if ((Integer) (obj1.get(NUMBEROFALBUMS)) == (Integer) (obj2.get(NUMBEROFALBUMS))) {
            cossimAlbums = 1;
        } else {
            cossimAlbums = (double) Math.min((Integer) (obj1.get(NUMBEROFALBUMS)), (Integer) (obj2.get(NUMBEROFALBUMS)))
                    / Math.max((Integer) (obj1.get(NUMBEROFALBUMS)), (Integer) (obj2.get(NUMBEROFALBUMS)));
        }

        return (FBLikesWeight * cossimFb
                + TwitterFollowersWeight * cossimTw
                + NumberOfAlbumsWeight * cossimAlbums
                + RegionWeight * sameRegion
                + CategoryWeight * sameCategories);
    }
    
    
    static Map sortByValue(Map map) {
    	LinkedList<String> list = new LinkedList<String>(map.entrySet());
        Collections.sort(list, new Comparator() {
             public int compare(Object o1, Object o2) {
                  return ((Comparable) ((Map.Entry) (o1)).getValue())
                 .compareTo(((Map.Entry) (o2)).getValue());
             }
        });

       Map result = new LinkedHashMap();
       for (Iterator it = list.iterator(); it.hasNext();) {
           Map.Entry entry = (Map.Entry)it.next();
           result.put(entry.getKey(), entry.getValue());
       }
       return result;
   } 
}
