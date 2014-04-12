package recommender.matrix;

import java.util.Date;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import common.DBHelper;

public class Matrix {

  public static void main(String[] args) {
    DBHelper db = DBHelper.getInstance();
    
    db.dropMatrixCollection();

    DBCursor cursor = db.findAllAlbums().addOption(Bytes.QUERYOPTION_NOTIMEOUT);

    try {

      while (cursor.hasNext()) {

        DBObject item = cursor.next();
        
        ObjectId albumId = (ObjectId) item.get("_id");
        String albumName = (String) item.get("name");
        //System.out.println(albumName);
        Date albumReleaseDate = (Date) item.get("release_date");
        BasicDBList albumGenres = (BasicDBList) item.get("genre");
        
        ObjectId artistId = (ObjectId) item.get("artist_id");
        
        DBObject artist = db.findArtist(artistId);
        
        String artistName = (String) artist.get("name");
        String artistCountry = (String) artist.get("country");
        String artistRegion = ( String) artist.get("region");
        double artistHotness = (double) artist.get("hotness");
        
        int artistFBLikes = (int) ((artist.get("facebook_likes") == null) ? 0 : artist.get("facebook_likes"));
        
        
        
        //artist.get("album_count"); ???
        Integer twitterFollowers = (int) ((artist.get("twitter_followers") == null) ? 0 : artist.get("twitter_followers"));
        
        db.insertMatrixRow(
            albumId,
            albumName,
            albumReleaseDate,
            albumGenres,
            artistId,
            artistName,
            artistCountry,
            artistRegion,
            artistHotness,
            artistFBLikes,
            twitterFollowers,
            0);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
