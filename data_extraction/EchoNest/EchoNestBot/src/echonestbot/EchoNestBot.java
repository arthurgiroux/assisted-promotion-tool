/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package echonestbot;

import com.echonest.api.v4.Artist;
import com.echonest.api.v4.ArtistParams;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author mikaelcastellani
 */
public class EchoNestBot {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws EchoNestException, UnknownHostException {
        
        
        //MongoDb Connection
        DBHelper dbHelper = new DBHelper("localhost", 27017);
        
        EchoNestAPI en = new EchoNestAPI("PASSKEY");

        List<String> listGenres = en.listGenres();
        

        Set<Artist> artistsList = new HashSet<Artist>();
        ArtistParams p = new ArtistParams();
        /*for(int j = 0; j < 11; j++){
            String genre = listGenres.get(j);*/ //For testing : limit the number of genres (there are 1000)
        for (String genre : listGenres) {
            for (int i = 0; i < 9; i++) {
                p.setResults(100);
                p.setStart(i * 100);
                p.includeHotttnesss();
                p.includeFamiliarity();
                p.includeArtistLocation();
                p.addGenre(genre);
                List<Artist> artists = en.searchArtists(p);
                if (artists != null && !artists.isEmpty()) {
                    artistsList.addAll(artists);
                } else {
                    break;
                }
            }
        }

       //System.out.println(artistsList.size());
        for (Artist artist : artistsList) {
            dbHelper.writeArtistEchoNest(artist.getName(), artist.getFamiliarity(), artist.getHotttnesss(), artist.getArtistLocation().getCountry() );
            System.out.println(artist.getName() + " || " + artist.getFamiliarity() + " || " + artist.getHotttnesss() + " || " + artist.getArtistLocation().getCountry());
        }

    }

}
