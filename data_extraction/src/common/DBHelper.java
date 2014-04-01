package common;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

/**
 *
 * @author mikaelcastellani, arthurgiroux
 */

public class DBHelper {
	
	private static volatile DBHelper instance = null;

    //Collections
    private static final String DBNAME = "team15";
    
    private static final String ARTISTSCOLLECTION = "artistsCollection";
    private static final String ALBUMSCOLLECTION = "albumsCollection";

    private DB db;
    private DBCollection albumsCollection;
    private DBCollection artistsCollection;

    
    public final static DBHelper getInstance() {
    	
    	// See wikipedia article for information on this shenanigan
        if (DBHelper.instance == null) {
           synchronized(DBHelper.class) {
             if (DBHelper.instance == null) {
            	 DBHelper.instance = new DBHelper(Settings.getInstance().getProperty("mongodb_host"), 27017);
             }
           }
        }
        return DBHelper.instance;
    }
    
    public DB getDb() {
        return db;
    }
    

    public DBHelper(String url, int port) {
        MongoClient mongoClient;
		try {
			mongoClient = new MongoClient(url, port);
	        DB db = mongoClient.getDB(DBNAME);
	        db.authenticate(Settings.getInstance().getProperty("mongodb_user"),
	        		Settings.getInstance().getProperty("mongodb_password").toCharArray());
	        
	        artistsCollection = db.getCollection(ARTISTSCOLLECTION);
	        albumsCollection = db.getCollection(ALBUMSCOLLECTION);
	        
		} catch (UnknownHostException e) {
			System.out.println("Something went wrong while connecting to the database");
			System.exit(1);
		}
    }
    
    public ObjectId insertArtist(String name, String country, double hotness, double familiary, String facebook_id,
    		String twitter_id) {
        BasicDBObject newArtist = new BasicDBObject("name", name).
        		append("country", country).
                append("hotness", hotness).
                append("familiarity", familiary).
                append("facebook_id", facebook_id).
                append("twitter_id", twitter_id);
        artistsCollection.insert(newArtist);
        
        // return the id of the insertion
        return (ObjectId) newArtist.get("_id");
    }
    
    public ObjectId insertAlbum(ObjectId artist_id, String name, Date release_date, List genre) {
        BasicDBObject newAlbum = new BasicDBObject("name", name).
        		append("artist_id", artist_id).
                append("release_date", release_date).
                append("genre", genre);
        albumsCollection.insert(newAlbum);
        
        // return the id of the insertion
        return (ObjectId) newAlbum.get("_id");	
    }
    
    public void emptyAll() {
    	artistsCollection.remove(new BasicDBObject());
    	albumsCollection.remove(new BasicDBObject());
    }
    
}