/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package echonestbot;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mikaelcastellani
 */
public class CustomSong {
    private String songName;
    private List<String> songTypeList = new ArrayList<String>();
    private Double songHotness;
    private String songCountry;
    private Double songDuration;

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public List<String> getSongTypeList() {
        return songTypeList;
    }

    public void setSongTypeList(List<String> songTypeList) {
        this.songTypeList = songTypeList;
    }

    public Double getSongHotness() {
        return songHotness;
    }

    public void setSongHotness(Double songHotness) {
        this.songHotness = songHotness;
    }

    public String getSongCountry() {
        return songCountry;
    }

    public void setSongCountry(String songCountry) {
        this.songCountry = songCountry;
    }

    public Double getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(Double songDuration) {
        this.songDuration = songDuration;
    }
    
    
    
}
