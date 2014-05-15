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
import java.util.Collections;
import java.util.Iterator;
import java.util.StringTokenizer;
import org.bson.types.ObjectId;

/**
 *
 * @author Amine
 */
public class ClusteringArtistsTW {
    
    static ArrayList<ArrayList<ArtistTW>> clusters_tw = new ArrayList<ArrayList<ArtistTW>>();    
    
    public static class ArtistTW implements Comparable<ArtistTW>{
        int twitter_followers;        
        ObjectId id;
        
        
        public ArtistTW(ObjectId oid, int twf){
            id = oid;
            twitter_followers = twf;

        }
        
        @Override
        public int compareTo(ArtistTW o) {
            ArtistTW other = (ArtistTW) o;
            if(this.twitter_followers < other.twitter_followers){
                return -1;
            } else if(this.twitter_followers == other.twitter_followers){
                return 0;
            } else{
                return 1;
            }                        
        }
    }
    
    public static void main(String[] args) throws UnknownHostException {
        
        new Integer (5).doubleValue();
        
        ArrayList<ArtistTW> artArr = new ArrayList<ArtistTW>();
        
        DBHelper dbHelper = DBHelper.getInstance();
        DBCursor artists = dbHelper.findAllArtistsWithTW();
        while(artists.hasNext()){
            DBObject currentArtist = artists.next();
            //System.out.println(currentArtist);            
            String twf = currentArtist.get("twitter_followers").toString();
            StringTokenizer st = new StringTokenizer(twf, ".");
            int twfint = Integer.parseInt(st.nextToken());
            System.out.println(twfint);
            ArtistTW artist = new ArtistTW((ObjectId)currentArtist.get("_id"), twfint);
            artArr.add(artist);                        
        }                
        
        Collections.sort(artArr);
               
        parse(artArr, 1);        
        merge_clusters(6);
        print_clusters();
        
    }
    
    static double mean(ArrayList<ArtistTW> cluster){
        double result;
        int sum = 0;
        int length = cluster.size();
        for(int i = 0; i<length;i++){
            sum += cluster.get(i).twitter_followers;
        }
        result = (double)sum / (double)length;
        return result;
    }
    static double stdev(ArrayList<ArtistTW> cluster, double mean){
        double result;
        int length = cluster.size();
        double var = 0;
        for(int i  = 0; i < length; i++){
            var += Math.pow((double)cluster.get(i).twitter_followers - mean, 2);
        }
        result = Math.sqrt(var/(double)length);
        return result;
    }
    static void parse(ArrayList<ArtistTW> arr, double n){
        //current cluster
        ArrayList<ArtistTW> current = new ArrayList<ArtistTW>();
        for(ArtistTW i : arr){
            if(current.size()<=1){
                current.add(i);
                continue;
            }
            //change mean by center of cluster
            int center = current.size()/2;
            double m = 0, mean_center, mean_cluster; 
            mean_cluster = mean(current);
            mean_center = current.get(center).twitter_followers;            
            double sd_cluster = stdev(current, mean_cluster);
            double sd_center = stdev(current, mean_center);
            double sd = Math.max(sd_cluster, sd_center);
            if(sd == sd_cluster){
                m = mean_cluster;
            } else if(sd == sd_center){
                m = mean_center;
            }
            
            if(Math.abs(m - i.twitter_followers) > n * sd){
                //System.out.println(Math.abs(m - i.twitter_followers) - n * sd);
                clusters_tw.add(current);
                current = new ArrayList<ArtistTW>();
            }
            current.add(i);
        }
        clusters_tw.add(current);
    }
    
    static void print_clusters(){
        int count = 0;
        for(ArrayList<ArtistTW> cluster : clusters_tw) {
            System.out.print("[" + cluster.get(0).twitter_followers+ "," + cluster.get(cluster.size()-1).twitter_followers+ "]: Cluster #" + (count++) + " (" + cluster.size() + ") - ");
            for(ArtistTW innerCluster : cluster) {
                System.out.print(innerCluster.twitter_followers + " ");
            }
            System.out.println();
        }
        System.out.println("Clusters count: " + clusters_tw.size());
    }
    
    static void merge_clusters(int min_merge){
        int index = 0, max = clusters_tw.size();
        Iterator it = clusters_tw.iterator();
        while(index < max - 1){
            ArrayList<ArtistTW> current_cluster = clusters_tw.get(index);
            ArrayList<ArtistTW> next_cluster = clusters_tw.get(index+1);
            if(next_cluster.size() <= min_merge){
                //merge
                current_cluster.addAll(next_cluster);
                //remove next from clusters
                clusters_tw.remove(index+1);
                max = clusters_tw.size();
            } else{
                index ++;
            }
        }
    }
    
}
