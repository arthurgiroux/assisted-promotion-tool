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
import com.mongodb.ServerAddress;

import com.mongodb.MongoCredential;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;

/**
 *
 * @author mikaelcastellani
 */
public class DBHelper {

    //Collections
    private static final String DBNAME = "team15";
    private static final String ARTISTSCOLLECTION = "artistsCollection";
    private static final String SONGSCOLLECTION = "songsCollection";

    //Keywords attributes
    private static final String KEYWORDWORD = "word";

    //Artist Attributes
    private static final String ARTISTNAME = "artistName";
    private static final String ARTISTFAMILIARITY = "artistFamiliarity";
    private static final String ARTISTHOTTTNESSS = "artistHotttnesss";
    private static final String ARTISTCOUNTRY = "artistCountry";
    private static final String ARTISTSONGS = "artistSongs";
    private static final String ARTISTSONG = "artistSong";

    private static final String SONGNAME = "songName";
    private static final String SONGHOTNESS = "songHotness";
    private static final String SONGCOUNTRY = "songCountry";
    private static final String SONGGENRE = "songType";

    private DB db;
    private DBCollection songsCollection;
    private DBCollection artistsCollection;

    public DB getDb() {
        return db;
    }

    public DBHelper(String url, int port) throws UnknownHostException {
        MongoClient mongoClient = new MongoClient(url, port);
        DB db = mongoClient.getDB(DBNAME);
        boolean auth = db.authenticate("user", "pass".toCharArray());

        artistsCollection = db.getCollection(ARTISTSCOLLECTION);
    }

    void writeArtistEchoNest(CustomArtist custArt) {

        BasicDBObject doc = new BasicDBObject(ARTISTNAME, custArt.getName()).
                append(ARTISTFAMILIARITY, custArt.getFamiliarity()).
                append(ARTISTHOTTTNESSS, custArt.getHotness()).
                append(ARTISTCOUNTRY, custArt.getCountry());

        BasicDBObject songsList = new BasicDBObject();
        Set<CustomSong> custSongs = custArt.getCustomSongs();
        for (CustomSong song : custSongs) {
            BasicDBObject songDoc = new BasicDBObject(
                    SONGNAME, song.getSongName()).
                    append(SONGHOTNESS, song.getSongHotness()).
                    append(SONGCOUNTRY, song.getSongCountry()).
                    append(SONGGENRE, custArt.getGenre()).
                    append(ARTISTNAME, custArt.getName()).
                    append(ARTISTFAMILIARITY, custArt.getFamiliarity()).
                    append(ARTISTHOTTTNESSS, custArt.getHotness()).
                    append(ARTISTCOUNTRY, custArt.getCountry());

            songsList.append(ARTISTSONG, songDoc);
        }
        doc.append(ARTISTSONGS, songsList);

        artistsCollection.insert(doc);

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
