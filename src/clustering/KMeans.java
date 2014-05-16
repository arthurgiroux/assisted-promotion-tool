/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Clustering;

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
    public static class Artist implements Clusterable{
        int fb_likes;
        int twitter_followers;
        int album_count;
        ObjectId id;

        public Artist(ObjectId oid, int fb, int tw, int ac){
            fb_likes = fb;
            twitter_followers = tw;
            album_count = ac;
            id = oid;
        }
        
        @Override
        public double[] getPoint() {
            double[] point = {fb_likes, twitter_followers, album_count};
            return point;
        }
    }
    
    public static void main(String[] args) throws UnknownHostException {
        
        int kClusters = 20;
        
        ArrayList<Artist> artists = new ArrayList<>();
        DBHelper dbHelper = DBHelper.getInstance();
        DBCursor result = dbHelper.findArtistsWithFBandTW();
        while(result.hasNext()){
            DBObject currentArtist = result.next();
            String twf = currentArtist.get("twitter_followers").toString();
            StringTokenizer st = new StringTokenizer(twf, ".");
            int twfint = Integer.parseInt(st.nextToken());
            int fbf = (int)currentArtist.get("facebook_likes");
            int ac = (int)currentArtist.get("album_count");
            ObjectId oid = (ObjectId)currentArtist.get("_id");
            artists.add(new Artist(oid, fbf,twfint,ac));
        }
        //System.out.println(artists.size());
        KMeansPlusPlusClusterer<Artist> clusterer = new KMeansPlusPlusClusterer<>(kClusters);
        List<CentroidCluster<Artist>> clusters = clusterer.cluster(artists);
        //System.out.println(clusters.size());
        for(CentroidCluster<Artist> cluster : clusters){
            List<Artist> artC = cluster.getPoints();
            for(Artist artist : artC){
                System.out.print("("+artist.fb_likes+","+artist.twitter_followers+","+artist.album_count+") ");
            }
            System.out.println();
        }
    }
}
