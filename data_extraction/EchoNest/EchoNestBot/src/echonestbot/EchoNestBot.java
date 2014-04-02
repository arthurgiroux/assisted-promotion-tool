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
import com.echonest.api.v4.Song;
import com.echonest.api.v4.SongParams;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
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
        EchoNestAPI en = new EchoNestAPI("apikey");

        List<String> listGenres = en.listGenres();

        Date timeLastSleep = new Date();
        long counter = 1;
        //for (int j = 0; j < 50; j++) {
        //    String genre = listGenres.get(j);
        for (String genre : listGenres) {
            for (int i = 0; i < 9; i++) {
                ArtistParams p = new ArtistParams();
                p.setResults(100);
                p.setStart(i * 100);
                p.includeHotttnesss();
                p.includeFamiliarity();
                p.includeArtistLocation();
                p.setEndYearAfter(2010);
                p.setMinHotttnesss(0.5f);
                p.addGenre(genre);
                List<Artist> artists = new ArrayList<>();

                if (counter > 118) {
                    long sleepTime = (65001 - (new Date().getTime() - timeLastSleep.getTime())) < 0 ? 60000 : (63001 - (new Date().getTime() - timeLastSleep.getTime()));
                    System.out.println("start sleep : " + counter + " for " + sleepTime / 1000 + "s");
                    Thread.sleep(sleepTime);
                    timeLastSleep = new Date();
                    counter = 0;
                    System.out.println("processing...");
                }

                try {
                    artists = en.searchArtists(p);
                    counter++;
                } catch (EchoNestException e) {
                    System.err.println("Genre : " + genre);
                    System.err.println("Error : " + e.getMessage());
                    e.printStackTrace();
                    Thread.sleep(30001);//30 sec then retry
                    System.out.println("Retry for artist");
                    artists = en.searchArtists(p);
                }

                for (Artist artist : artists) {
                    CustomArtist custArtist = new CustomArtist();
                    custArtist.setName(artist.getName());
                    custArtist.setFamiliarity(artist.getFamiliarity());
                    custArtist.setHotness(artist.getHotttnesss());
                    //Bug in the API
                    try {
                        custArtist.setCountry(artist.getArtistLocation().getCountry());
                        custArtist.setCity(artist.getArtistLocation().getCity());
                    } catch (NullPointerException e) {
                        custArtist.setCountry(null);
                        custArtist.setCity(null);
                    }

                    custArtist.setGenre(genre);
                    custArtist.getCustomSongs().addAll(searchSongsByArtist(en, custArtist));
                    counter++;
                    if (counter > 118) {
                        long sleepTime = (65001 - (new Date().getTime() - timeLastSleep.getTime())) < 0 ? 60000 : (63001 - (new Date().getTime() - timeLastSleep.getTime()));
                        System.out.println("start sleep : " + counter + " for " + sleepTime / 1000 + "s");
                        Thread.sleep(sleepTime);
                        timeLastSleep = new Date();
                        counter = 0;
                        System.out.println("processing...");
                    }
                    System.out.println(custArtist.getName() + " with " + custArtist.getCustomSongs().size() + " songs. ");
                    dbHelper.writeArtistEchoNest(custArtist);
                }

                if (artists == null || !artists.isEmpty()) {
                    //If no artist is returned, go to next genre
                    break;
                }
            }
        }
    }

    public static Set<CustomSong> searchSongsByArtist(EchoNestAPI en, CustomArtist custArtist)
            throws EchoNestException, InterruptedException {
        SongParams p = new SongParams();
        p.setArtist(custArtist.getName());
        p.includeAudioSummary();
        p.includeSongHotttnesss();
        p.setMinSongHotttnesss(0.4f);
        p.setResults(100);
        p.sortBy("song_hotttnesss", false);

        List<Song> songs = null;
        try {
            songs = en.searchSongs(p);
        } catch (EchoNestException e) {
            System.err.println("Error : " + e.getMessage());
            e.printStackTrace();
            Thread.sleep(30001);//30 sec then retry
            System.out.println("Retry for song");
            songs = en.searchSongs(p);
        }

        Set<CustomSong> customSongs = new HashSet<CustomSong>();
        for (Song song : songs) {
            CustomSong custSong = new CustomSong();
            custSong.setSongName(song.getTitle());
            custSong.setSongHotness(song.getSongHotttnesss());
            custSong.setSongDuration(song.getDuration());
            custSong.setSongCountry(custArtist.getCountry());
            custSong.setSongType(custArtist.getGenre());
            customSongs.add(custSong);
        }
        return customSongs;
    }

}
