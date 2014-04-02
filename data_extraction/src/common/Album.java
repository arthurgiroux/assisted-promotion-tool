package common;

import java.util.ArrayList;

public class Album {

  private String name;
  private String release_date;
  private ArrayList<String> genre;

  public Album(String name, String release_date, ArrayList<String> genre) {
    super();
    this.name = name;
    this.release_date = release_date;
    this.genre = new ArrayList<String>(genre);
  }

  public String getName() {
    return name;
  }

  public String getRelease_date() {
    return release_date;
  }

  public ArrayList<String> getGenre() {
    return genre;
  }
}
