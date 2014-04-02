package echonest;

import com.echonest.api.v4.Artist;
import com.echonest.api.v4.ArtistParams;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;

import common.Album;
import common.DBHelper;
import common.Settings;
import freebase.FreeBase;

import java.text.SimpleDateFormat;
import java.util.List;

import org.bson.types.ObjectId;

public class EchoNest {

  private final static float HOTNESS_THRESHOLD = 0.4f;
  private final static int ARTISTS_LIMIT = 1;

  public void run() {

    DBHelper dbHelper = DBHelper.getInstance();

    EchoNestAPI echonest = new EchoNestAPI(Settings.getInstance().getProperty("echonest_api_key"));
    
    SimpleDateFormat parseFormatComplete = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat parseFormatNoDay = new SimpleDateFormat("yyyy-MM");
    
    try {
      List<String> listGenres = echonest.listGenres();
  
      for (String genre : listGenres) {
    
        ArtistParams p = new ArtistParams();
        p.setResults(ARTISTS_LIMIT);
        p.setStart(0);
        p.includeHotttnesss();
        p.includeFamiliarity();
        p.includeArtistLocation();
        p.sortBy("hotttnesss", false);
        p.setEndYearAfter(2010);
        p.setMinHotttnesss(HOTNESS_THRESHOLD);
        p.addGenre(genre);
        
        int current_pos = 0;
        boolean hasData = true;
    
    
    
        while (hasData && current_pos < 2 - ARTISTS_LIMIT) {
          try {
            List<Artist> artists = echonest.searchArtists(p);
    
            /* if there are less than ARTISTS_LIMIT artists, in the next 
             * iteration the artists list would be empty
             */
            if (artists.size() < ARTISTS_LIMIT) {
              hasData = false;
            }
    
            current_pos += ARTISTS_LIMIT;
            p.setStart(current_pos);
    
            for (Artist artist : artists) {
    
              System.out.println("Found artist : " + artist.getName());
    
              System.out.println("Querying freebase");
    
              FreeBase fb = new FreeBase(artist.getName());
    
              if (fb.run()) {
                System.out.println("Found on freebase");
                if (fb.getFacebook_id().size() > 0 || fb.getTwitter_id().size() > 0) {
                  String location = "";
                  try {
                    location = artist.getArtistLocation().getCountry();
                  }
                  catch (NullPointerException e) {
                    location = "";
                  }
    
                  ObjectId artist_id = dbHelper.insertArtist(artist.getName(), location, artist.getHotttnesss(), artist.getFamiliarity(),
                      fb.getFacebook_id(), fb.getTwitter_id());
                  System.out.println("Found " + fb.getAlbums().size() + " albums");
                  for (Album album : fb.getAlbums()) {
                    try {
                      String[] releaseDate = album.getRelease_date().split("-");
                      if (releaseDate.length > 1) {
    
                        SimpleDateFormat parseFormat = parseFormatComplete;
                        if (releaseDate.length == 2) {
                          parseFormat = parseFormatNoDay;
                        }
    
                        dbHelper.insertAlbum(artist_id, album.getName(), parseFormat.parse(album.getRelease_date()), album.getGenre());
                      }
                    } catch (java.text.ParseException e) {
                      // Shouldn't happen but if this happen just discard the row
                      System.err.println(e.getStackTrace());
                    }
                  }
                }
              }
              else {
                System.out.println("Not found on freebase");
              }
            }
          } catch (EchoNestException e) {
            System.err.println("Something went wrong with EchoNest API " + e.getMessage());
            // Waiting 10s to let the API cool off a little
            e.printStackTrace();
            try {
              Thread.sleep(10000);
            } catch (InterruptedException e1) {
    
            }
          }
        }
      }
    }
    catch (EchoNestException e) {
      System.err.println("EchoNest : Couldn't retrieve the genres, aborting");
    }
  }
}