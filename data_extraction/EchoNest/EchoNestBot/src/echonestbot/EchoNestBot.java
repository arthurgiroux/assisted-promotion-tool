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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
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
        //DBHelper dbHelper = new DBHelper("localhost", 27017);
        EchoNestAPI en = new EchoNestAPI("ABQ1ZFO6BNOIHISN9");

        List<String> listGenres = en.listGenres();
        System.out.println(listGenres.size() + " genres loaded");

        Set<CustomArtist> artistsList = new HashSet<CustomArtist>();

        Date timeLastSleep = new Date();
        long counter = 1;
        for (int j = 0; j < 1; j++) {
            String genre = listGenres.get(j);
            //for (String genre : listGenres) {
            System.out.println("Number of artists : " + artistsList.size() + " " + genre);
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

                if (counter % 115 == 0) {
                    System.out.println("start sleep");
                    Thread.sleep(65001 - (new Date().getTime() - timeLastSleep.getTime()));//1min - 
                    timeLastSleep = new Date();
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

                for (Artist artist : artists) {
                    CustomArtist custArtist = new CustomArtist();
                    custArtist.setName(artist.getName());
                    custArtist.setFamiliarity(artist.getFamiliarity());
                    custArtist.setHotness(artist.getHotttnesss());
                    custArtist.setCountry(artist.getArtistLocation().getCountry());
                    custArtist.setCity(artist.getArtistLocation().getCity());
                    custArtist.setGenre(genre);
                    List<Song> listSongs = artist.getSongs();
                    for (Song song : listSongs) {
                        counter += 5;
                        if (counter > 80) {
                            System.out.println("start sleep");
                            Thread.sleep(65001 - (new Date().getTime() - timeLastSleep.getTime()));//1min - 
                            timeLastSleep = new Date();
                            System.out.println("processing...");
                        }

                        CustomSong custSong = new CustomSong();
                        custSong.setSongName(song.getTitle());
                        custSong.setSongDuration(song.getDuration());
                        custSong.setSongCountry(custArtist.getCountry());

                        String buckets[] = {"song_hotttnesss", "song_type"};
                        song.fetchBuckets(buckets, false);
                        Double hotness = song.getDouble("song_hotttnesss");
                        custSong.setSongHotness(hotness);
                        List<String> typeList = (List<String>) song.getObject("song_type");
                        custSong.setSongTypeList(typeList);
                        if (custSong.getSongHotness() > 0.4f) {
                            custArtist.getCustomSongs().add(custSong);
                        }
                    }
                    System.out.println(custArtist.toString() + custArtist.getName());
                    artistsList.add(custArtist);
                }

                if (artists == null || !artists.isEmpty()) {
                    break;
                }
            }
        }

        System.out.println(artistsList.size());
        for (CustomArtist artist : artistsList) {
            System.out.println(artist.toString());
            //dbHelper.writeArtistEchoNest(artist.getName(), artist.getFamiliarity(), artist.getHotttnesss(), artist.getArtistLocation().getCountry() );
        }

    }

}
