package common;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

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
    private static final String FBPOSTSCOLLECTION = "fbpostsCollection";
    private static final String TWEETSCOLLECTION = "tweetsNLPBackup";

    private static final String MATRIXCOLLECTION = "matrixSandbox";

    public static String REGION = "artistRegion";
    public static String CATEGORY = "category";
    public static String NUMBEROFALBUMS = "album_count";
    public static String FACEBOOKLIKES = "facebook_likes";
    public static String TWITTERFOLLOWERS = "twitter_followers";

    private DB db;
    private DBCollection albumsCollection;
    private DBCollection artistsCollection;
    private DBCollection fbpostsCollection;
    private DBCollection tweetsCollection;

    private DBCollection matrixCollection;

    public final static DBHelper getInstance() {

        // See wikipedia article for information on this shenanigan
        if (DBHelper.instance == null) {
            synchronized (DBHelper.class) {
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

    private DBHelper(String url, int port) {
        MongoClient mongoClient;
        try {
            mongoClient = new MongoClient(url, port);
            DB db = mongoClient.getDB(DBNAME);
            db.authenticate(Settings.getInstance().getProperty("mongodb_user"),
                    Settings.getInstance().getProperty("mongodb_password").toCharArray());

            artistsCollection = db.getCollection(ARTISTSCOLLECTION);
            albumsCollection = db.getCollection(ALBUMSCOLLECTION);
            fbpostsCollection = db.getCollection(FBPOSTSCOLLECTION);
            tweetsCollection = db.getCollection(TWEETSCOLLECTION);

            matrixCollection = db.getCollection(MATRIXCOLLECTION);

        } catch (UnknownHostException e) {
            System.out.println("Something went wrong while connecting to the database");
            System.exit(1);
        }
    }

    public ObjectId insertArtist(String name, String country, double hotness, double familiary, List<String> facebook_id,
            List<String> twitter_id) {
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

    public void insertMatrixRow(
            ObjectId albumId,
            String albumName,
            Date albumReleaseDate,
            BasicDBList albumGenres,
            ObjectId artistId,
            String artistName,
            String artistCountry,
            String artistRegion,
            double artistHotness,
            int artistFBLikes,
            int twitterFollowers,
            int albumCount) {
        BasicDBObject newRow = new BasicDBObject("albumId", albumId).
                append("albumName", albumName).
                append("albumReleaseDate", albumReleaseDate).
                append("albumGenres", albumGenres).
                append("artistId", artistId).
                append("artistName", artistName).
                append("artistCountry", artistCountry).
                append("artistRegion", artistRegion).
                append("artistHotness", artistHotness).
                append("facebook_likes", artistFBLikes).
                append("twitter_followers", twitterFollowers).
                append("album_count", albumCount);
        matrixCollection.insert(newRow);
    }

    public void updateArtistLikes(DBObject artist, int likes, int talking_about) {
        artist.put("facebook_likes", likes);
        artist.put("facebook_talking_about", talking_about);
        artistsCollection.save(artist);
    }

    public void updateArtistFollowers(DBObject artist, int followers) {
        artist.put("twitter_followers", followers);
        artistsCollection.save(artist);
    }

    public void updateArtistAlbumCount(DBObject artist, int albumCount) {
        artist.put("album_count", albumCount);
        artistsCollection.save(artist);
    }

    public void insertAlbum(ObjectId artist_id, String name, Date release_date, List<String> genre) {
        BasicDBObject newAlbum = new BasicDBObject("name", name).
                append("artist_id", artist_id).
                append("release_date", release_date).
                append("genre", genre);
        albumsCollection.insert(newAlbum);

    }

    public void insertFbPosts(ObjectId artist_id, Date date, String message, int likes, int shares,
            boolean picture_attached, BasicDBList commentsList) {
        BasicDBObject new_post = new BasicDBObject("artist_id", artist_id).
                append("date", date).
                append("message", message).
                append("likes", likes).
                append("shares", shares).
                append("picture_attached", picture_attached).
                append("comments", commentsList);
        fbpostsCollection.insert(new_post);
    }

    public void insertTweet(ObjectId artist_id, Date date, String source, String message, int retweets,
            List<String> hashtags) {
        BasicDBObject new_post = new BasicDBObject("artist_id", artist_id).
                append("date", date).
                append("source", source).
                append("message", message).
                append("retweets", retweets).
                append("hashtags", hashtags);
        tweetsCollection.insert(new_post);
    }
    
    public void updateMatrixRow(DBObject row) {
      matrixCollection.save(row);
  }

    public void emptyAll() {
        artistsCollection.remove(new BasicDBObject());
        albumsCollection.remove(new BasicDBObject());
        fbpostsCollection.remove(new BasicDBObject());
        tweetsCollection.remove(new BasicDBObject());
    }

    public void emptyFacebookAndTwitterCollections() {
        fbpostsCollection.remove(new BasicDBObject());
        tweetsCollection.remove(new BasicDBObject());
    }

    public void dropMatrixCollection() {
        matrixCollection.drop();
    }

    public DBObject findArtist(ObjectId id) {
        BasicDBObject query = new BasicDBObject("_id", id);
        return artistsCollection.findOne(query);
    }

    public DBCursor findAllArtists() {
        return artistsCollection.find();
    }
    
    public DBCursor findAllArtistsWithFB(){
        //db.artistsCollection.find({facebook_likes:{$exists:true}})
        return artistsCollection.find(new BasicDBObject("facebook_likes", new BasicDBObject("$exists", true)));
    }
    
    public DBCursor findAllArtistsWithTW(){
        //db.artistsCollection.find({twitter_followers:{$exists:true}})
        return artistsCollection.find(new BasicDBObject("twitter_followers", new BasicDBObject("$exists", true)));
    }
	
	public DBCursor findArtistsWithFBandTW(){
        //db.artistsCollection.find({twitter_followers:{$exists:true}, facebook_likes:{$exists:true}})
        return artistsCollection.find(new BasicDBObject("facebook_likes", new BasicDBObject("$exists", true)).append("twitter_followers", new BasicDBObject("$exists", true)));
    }

    public DBCursor findAllAlbums() {
        return albumsCollection.find();
    }

    public DBCursor findMatrixRows() {
        return matrixCollection.find();
    }

    public long countArtists() {
        return artistsCollection.count();
    }

    public long countAlbums() {
        return albumsCollection.count();
    }

    public long countFacebookPosts() {
        return fbpostsCollection.count();
    }

    public long countTweets() {
        return tweetsCollection.count();
    }

    public boolean hasTweetsParsed(ObjectId artist_id) {
        BasicDBObject post = new BasicDBObject("artist_id", artist_id);
        DBObject obj = tweetsCollection.findOne(post);
        return obj != null;
    }

    public boolean artistExists(ObjectId artist_id) {
        BasicDBObject post = new BasicDBObject("_id", artist_id);
        DBObject obj = artistsCollection.findOne(post);
        return obj != null;
    }
    
    public DBCursor findFBPostsByArtistId(BasicDBObject query) {
      return fbpostsCollection.find(query);
    }
    
    public DBCursor findTweetsByArtistId(BasicDBObject query) {
      return tweetsCollection.find(query);
    }
    
    public DBCursor findPreviousAlbum(ObjectId artist_id, Date date, Date date_limit) {
      return albumsCollection.find(new BasicDBObject("artist_id", artist_id).append("release_date", new BasicDBObject("$lt", date)
      .append("$gte", date_limit))).sort(new BasicDBObject("release_date", -1)).limit(1);
    }
    
    /*
     public void insertCleanAlbum(DBObject album){
     cleanAlbumsCollection.insert(album);
     }

     public void insertCleanTweet(DBObject tweet){
     cleanTweetsCollection.insert(tweet);
     }

     public void insertCleanFbpost(DBObject fbpost){
     cleanFbpostsCollection.insert(fbpost);
     }
     */
    
    public DBObject findFBPostsById(ObjectId Id) {
        BasicDBObject query = new BasicDBObject("_id", Id);
        return fbpostsCollection.findOne(query);
    }
}