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
import org.bson.types.ObjectId;

/**
 *
 * @author Amine
 */
public class ClusteringArtists {
    
    static ArrayList<ArrayList<Artist>> clusters = new ArrayList<ArrayList<Artist>>();
    static ArrayList<ArrayList<Integer>> clusters_int = new ArrayList<ArrayList<Integer>>();
    
    public static class Artist implements Comparable<Artist>{
        int facebook_followers;
        ObjectId id;
        
        
        public Artist(ObjectId oid, int fbf){
            id = oid;
            facebook_followers = fbf;

        }
        
        @Override
        public int compareTo(Artist o) {
            Artist other = (Artist) o;
            if(this.facebook_followers < other.facebook_followers){
                return -1;
            } else if(this.facebook_followers == other.facebook_followers){
                return 0;
            } else{
                return 1;
            }                        
        }
    }
    
    public static void main(String[] args) throws UnknownHostException {
        
        ArrayList<Artist> artArr = new ArrayList<Artist>();
        
        DBHelper dbHelper = DBHelper.getInstance();
        DBCursor artists = dbHelper.findAllArtistsWithFB();
        while(artists.hasNext()){
            DBObject currentArtist = artists.next();
            artArr.add(new Artist((ObjectId)currentArtist.get("_id"), (Integer) currentArtist.get("facebook_likes")));                        
        }                
        
        Collections.sort(artArr);
               
        parse(artArr, 1);        
        merge_clusters(5);
        print_clusters();
        
    }
    
    static double mean(ArrayList<Artist> cluster){
        double result;
        int sum = 0;
        int length = cluster.size();
        for(int i = 0; i<length;i++){
            sum += cluster.get(i).facebook_followers;
        }
        result = (double)sum / (double)length;
        return result;
    }
    static double stdev(ArrayList<Artist> cluster, double mean){
        double result;
        int length = cluster.size();
        double var = 0;
        for(int i  = 0; i < length; i++){
            var += Math.pow((double)cluster.get(i).facebook_followers - mean, 2);
        }
        result = Math.sqrt(var/(double)length);
        return result;
    }
    static void parse(ArrayList<Artist> arr, double n){
        //current cluster
        ArrayList<Artist> current = new ArrayList<Artist>();
        for(Artist i : arr){
            if(current.size()<=1){
                current.add(i);
                continue;
            }
            //change mean by center of cluster
            int center = current.size()/2;
            double m = 0, mean_center, mean_cluster; 
            mean_cluster = mean(current);
            mean_center = current.get(center).facebook_followers;
            
            double sd_cluster = stdev(current, mean_cluster);
            double sd_center = stdev(current, mean_center);
            double sd = Math.max(sd_cluster, sd_center);
            if(sd == sd_cluster){
                m = mean_cluster;
            } else if(sd == sd_center){
                m = mean_center;
            }
            
            if(Math.abs(m - i.facebook_followers) > n * sd){
                //System.out.println(Math.abs(m - i.facebook_followers) - n * sd);
                clusters.add(current);
                current = new ArrayList<Artist>();
            }
            current.add(i);
        }
        clusters.add(current);
    }
    
    static void print_clusters(){
        int count = 0;
        for(ArrayList<Artist> cluster : clusters) {
            System.out.print("[" + cluster.get(0).facebook_followers+ "," + cluster.get(cluster.size()-1).facebook_followers+ "]: Cluster #" + (count++) + " (" + cluster.size() + ") - ");
            for(Artist innerCluster : cluster) {
                System.out.print(innerCluster.facebook_followers + " ");
            }
            System.out.println();
        }
        System.out.println("Clusters count: " + clusters.size());
    }
    
    static void merge_clusters(int min_merge){
        int index = 0, max = clusters.size();
        Iterator it = clusters.iterator();
        while(index < max - 1){
            ArrayList<Artist> current_cluster = clusters.get(index);
            ArrayList<Artist> next_cluster = clusters.get(index+1);
            if(next_cluster.size() <= min_merge){
                //merge
                current_cluster.addAll(next_cluster);
                //remove next from clusters
                clusters.remove(index+1);
                max = clusters.size();
            } else{
                index ++;
            }
        }
    }
    
}
