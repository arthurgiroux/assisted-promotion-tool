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
    private static final String DBNAME = "promotionToolDB";
    private static final String TWEETSCOLLECTION = "tweetsCollection";
    private static final String ARTISTSCOLLECTION = "artistsCollection";
    private static final String KEYWORDSCOLLECTION = "keywordsCollection";

    //Keywords attributes
    private static final String KEYWORDWORD = "word";
    
    //Artist Attributes
    private static final String ARTISTNAME = "name";
    private static final String ARTISTFAMILIARITY = "familiarity";
    private static final String ARTISTHOTTTNESSS = "hotttnesss";
    private static final String ARTISTCOUNTRY = "country";

    private DB db;
    private DBCollection artistsCollection;

    public DB getDb() {
        return db;
    }

    public DBHelper(String url, int port) throws UnknownHostException {
// To directly connect to a single MongoDB server (note that this will not auto-discover the primary even
// if it's a member of a replica set:
// or
        MongoClient mongoClient = new MongoClient(url, port);

        db = mongoClient.getDB(DBNAME);
        artistsCollection = db.getCollection(ARTISTSCOLLECTION);
    }

    void writeArtistEchoNest(String name, Double familiarity, Double hotttnesss, String country) {

        BasicDBObject doc = new BasicDBObject(ARTISTNAME, name).
                append(ARTISTFAMILIARITY, familiarity).
                append(ARTISTHOTTTNESSS, hotttnesss).
                append(ARTISTCOUNTRY, country);
        
        artistsCollection.insert(doc);

    }

    /*public List<String> readTwitterAccountsToProcess() {
        List<String> albumNames = new ArrayList<String>();

        DBCollection coll = db.getCollection(ARTISTSCOLLECTION);
        DBCursor cursor = coll.find();
        try {
            while (cursor.hasNext()) {
                albumNames.add(cursor.next().get(ARTISTNAME).toString());
            }
        } finally {
            cursor.close();
        }

        return albumNames;

    }

    public List<String> readKeywords() {
        List<String> albumNames = new ArrayList<String>();

        DBCollection coll = db.getCollection(KEYWORDSCOLLECTION);
        DBCursor cursor = coll.find();
        try {
            while (cursor.hasNext()) {
                albumNames.add(cursor.next().get(KEYWORDWORD).toString());
            }
        } finally {
            cursor.close();
        }

        return albumNames;

    }*/

}
