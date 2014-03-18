/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.dftwitterbot;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author mikaelcastellani
 */
public class DBHelper {

    //Collections
    private static final String DBNAME = "promotionToolDB";
    private static final String TWEETSCOLLECTION = "tweetsCollection";
    private static final String ALBUMSCOLLECTION = "albumsCollection";
    private static final String KEYWORDSCOLLECTION = "keywordsCollection";

    //Keywords attributes
    private static final String KEYWORDWORD = "word";

    //Album attributes
    private static final String ALBUMNAME = "name";

    //Tweet attributes
    public static final String TWEETSOURCE = "source";
    public static final String TWEETDATE = "date";
    public static final String TWEETTEXT = "text";
    public static final String TWEETRETWEETCOUNT = "retweetCount";
    public static final String TWEETCOUNTRY = "country";
    public static final String TWEETKEYWORDS = "keywords";
    public static final String TWEETKEYWORD = "keyword";

    private DB db;
    private DBCollection tweetsCollection;

    public DB getDb() {
        return db;
    }

    public DBHelper(String url, int port) throws UnknownHostException {
// To directly connect to a single MongoDB server (note that this will not auto-discover the primary even
// if it's a member of a replica set:
// or
        MongoClient mongoClient = new MongoClient(url, port);

        db = mongoClient.getDB(DBNAME);
        tweetsCollection = db.getCollection(TWEETSCOLLECTION);
    }

    void write(String source, Date createdAt, String text, List<String> keywordsFound, int retweetCount, String country) {

        BasicDBObject doc = new BasicDBObject(TWEETSOURCE, source).
                append(TWEETDATE, createdAt).
                append(TWEETTEXT, text).
                append(TWEETRETWEETCOUNT, retweetCount).
                append(TWEETCOUNTRY, country);

        if (!keywordsFound.isEmpty()) {
            BasicDBObject keywordsList = new BasicDBObject();
            for (String keyword : keywordsFound) {
                keywordsList.append(TWEETKEYWORD, keyword);
            }
            doc.append(TWEETKEYWORDS, keywordsList);
        }

        tweetsCollection.insert(doc);

    }

    public List<String> readAlbumNamesToProcess() {
        List<String> albumNames = new ArrayList<String>();

        DBCollection coll = db.getCollection(ALBUMSCOLLECTION);
        DBCursor cursor = coll.find();
        try {
            while (cursor.hasNext()) {
                albumNames.add(cursor.next().get(ALBUMNAME).toString());
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

    }

}
