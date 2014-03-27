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
import java.util.ArrayList;
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
    public static void main(String[] args) throws EchoNestException, UnknownHostException, InterruptedException {

        //MongoDb Connection
        DBHelper dbHelper = new DBHelper("localhost", 27017);
        EchoNestAPI en = new EchoNestAPI("PASSKEY");

        List<String> listGenres = en.listGenres();
        System.out.println(listGenres.size() + " genres loaded");

        Set<Artist> artistsList = new HashSet<Artist>();
        
        long counter = 1;
        for (String genre : listGenres) {
            System.out.println("Number of artists : " + artistsList.size() + " " + genre);
            for (int i = 0; i < 9; i++) {
                ArtistParams p = new ArtistParams();
                p.setResults(100);
                p.setStart(i * 100);
                p.includeHotttnesss();
                p.includeFamiliarity();
                p.includeArtistLocation();
                p.setMinHotttnesss(0.5f);
                p.addGenre(genre);
                List<Artist> artists = new ArrayList<>();

                if (counter % 115 == 0) {
                    System.out.println("start sleep");
                    Thread.sleep(60001);//1min
                    System.out.println("processing...");
                }

                try {
                    artists = en.searchArtists(p);
                    counter++;
                } catch (EchoNestException e) {
                    System.err.println("Genre : " + genre);
                    System.err.println("Error : " + e.getMessage());
                    e.printStackTrace();
                    Thread.sleep(60001);//1min
                }

                if (artists != null && !artists.isEmpty()) {
                    artistsList.addAll(artists);
                } else {
                    break;
                }
            }
        }

        System.out.println(artistsList.size());
        for (Artist artist : artistsList) {
            dbHelper.writeArtistEchoNest(artist.getName(), artist.getFamiliarity(), artist.getHotttnesss(), artist.getArtistLocation().getCountry() );
            System.out.println(artist.getName() + " || " + artist.getFamiliarity() + " || " + artist.getHotttnesss() + " || " + artist.getArtistLocation().getCountry());
        }

    }

}
