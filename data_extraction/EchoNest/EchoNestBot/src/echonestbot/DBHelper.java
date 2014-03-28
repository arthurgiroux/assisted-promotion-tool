/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package echonestbot;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;

/**
 *
 * @author mikaelcastellani
 */
public class DBHelper {

    //Collections
    private static final String DBNAME = "test";
    private static final String ARTISTSCOLLECTION = "artistsCollection";
    private static final String SONGSCOLLECTION = "songsCollection";


    //Keywords attributes
    private static final String KEYWORDWORD = "word";
    
    //Artist Attributes
    private static final String ARTISTNAME = "artistName";
    private static final String ARTISTFAMILIARITY = "artistFamiliarity";
    private static final String ARTISTHOTTTNESSS = "artistHotttnesss";
    private static final String ARTISTCOUNTRY = "artistCountry";
    
    private static final String SONGNAME = "songName";
    private static final String SONGHOTNESS = "songHotness";
    private static final String SONGCOUNTRY = "songCountry";
    private static final String SONGGENRE = "songType";

    private DB db;
    private DBCollection songsCollection;

    public DB getDb() {
        return db;
    }

    public DBHelper(String url, int port) throws UnknownHostException {
// To directly connect to a single MongoDB server (note that this will not auto-discover the primary even
// if it's a member of a replica set:
// or
        MongoClient mongoClient = new MongoClient(url, port);

        db = mongoClient.getDB(DBNAME);
        songsCollection = db.getCollection(SONGSCOLLECTION);
    }

    void writeArtistEchoNest(CustomArtist custArt) {
        
        BasicDBObject doc = new BasicDBObject(ARTISTNAME, custArt.getName()).
                append(ARTISTFAMILIARITY, custArt.getFamiliarity()).
                append(ARTISTHOTTTNESSS, custArt.getHotness()).
                append(ARTISTCOUNTRY, custArt.getCountry());
        
        songsCollection.insert(doc);

    }
    
    void writeSongEchoNest(CustomSong custSong) {
        

        
        BasicDBObject doc = new BasicDBObject(
                SONGNAME, custSong.getSongName()).
                append(SONGHOTNESS, custSong.getSongHotness()).
                append(SONGCOUNTRY, custSong.getSongCountry()).
                append(SONGGENRE, custSong.getCustomArtist().getGenre()).
                append(ARTISTNAME, custSong.getCustomArtist().getName()).
                append(ARTISTFAMILIARITY, custSong.getCustomArtist().getFamiliarity()).
                append(ARTISTHOTTTNESSS, custSong.getCustomArtist().getHotness()).
                append(ARTISTCOUNTRY, custSong.getCustomArtist().getCountry());
        
        songsCollection.insert(doc);

    }

}
