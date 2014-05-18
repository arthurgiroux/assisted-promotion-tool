/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package clustering;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

import common.DBHelper;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.bson.types.ObjectId;

/**
 *
 * @author Amine
 */
public class KMeans {
    public static class Artist implements Clusterable {
        private int fb_likes;
        private int twitter_followers;
        private int album_count;
        private DBObject object;

        private Artist(DBObject o, int fb, int tw, int ac){
            fb_likes = fb;
            twitter_followers = tw;
            album_count = ac;
            object = o;
        }
        
        public static Artist fromDBObject(DBObject o) {
          int twfint = ((Number) o.get("twitter_followers")).intValue();
          int fbf = ((Number) o.get("facebook_likes")).intValue();
          int ac = ((Number) o.get("album_count")).intValue();
          return new Artist(o, fbf,twfint,ac);
        }
        
        @Override
        public double[] getPoint() {
            double[] point = {fb_likes, twitter_followers, album_count};
            return point;
        }
        
        public DBObject getDBObject() {
          return object;
        }
    }
    
    public static void main(String[] args) throws UnknownHostException {
        if (args.length != 1) {
          System.out.println("Usage : KMeans <nrClusters>");
          System.exit(-1);
        }
        
        int kClusters = Integer.parseInt(args[0]);
        
        ArrayList<Artist> artists = new ArrayList<Artist>();
        DBHelper dbHelper = DBHelper.getInstance();
        DBCursor result = dbHelper.findArtistsWithFBandTW();
        
        while(result.hasNext()){
            DBObject currentArtist = result.next();
            artists.add(Artist.fromDBObject(currentArtist));
        }
        
        //System.out.println(artists.size());
        KMeansPlusPlusClusterer<Artist> clusterer = new KMeansPlusPlusClusterer<Artist>(kClusters);
        List<CentroidCluster<Artist>> clusters = clusterer.cluster(artists);
        //System.out.println(clusters.size());
        dbHelper.emptyClusterCenters();
        
        for(CentroidCluster<Artist> cluster : clusters){
            double[] center = cluster.getCenter().getPoint();
            ObjectId centerId = dbHelper.insertClusterCenter(center[0], center[1], center[2]);
            
            List<Artist> artC = cluster.getPoints();
            for(Artist artist : artC){
                dbHelper.updateMatrixRowCluster(artist.getDBObject(), centerId);
                //System.out.print("("+artist.fb_likes+","+artist.twitter_followers+","+artist.album_count+") ");
            }
        }
    }
}
