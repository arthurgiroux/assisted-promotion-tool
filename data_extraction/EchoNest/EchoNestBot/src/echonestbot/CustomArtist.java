/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package echonestbot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author mikaelcastellani
 */
public class CustomArtist {
    private String name;
    private Set<CustomSong> customSongs = new HashSet<CustomSong>();
    private Double hotness;

    public Double getHotness() {
        return hotness;
    }

    public void setHotness(Double hotness) {
        this.hotness = hotness;
    }
    private String country;
    private String city;
    private Double familiarity;
    private String genre;

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<CustomSong> getCustomSongs() {
        return customSongs;
    }

    public void setCustomSongs(Set<CustomSong> customSongs) {
        this.customSongs = customSongs;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getFamiliarity() {
        return familiarity;
    }

    public void setFamiliarity(Double familiarity) {
        this.familiarity = familiarity;
    }
    
    
            
            
}
