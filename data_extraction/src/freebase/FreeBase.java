package freebase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import common.Album;
import common.Settings;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FreeBase {

  private String facebook_id;
  private String twitter_id;
  private String artist_name;
  private List<Album> albums;

  public FreeBase(String name) {
    artist_name = name;
    albums = new ArrayList<Album>();
  }


  private String getQuery() {
    String query = "[{" +
        "\"type\": \"/music/artist\"," +
        "\"limit\": 1," +
        "\"name\": \"" + artist_name + "\"," +
        "\"/internet/social_network_user/facebook_id\": null," +
        "\"/internet/social_network_user/twitter_id\": null," +
        "\"genre\": []," +
        "\"album\": [{" +
        "\"name\": null," +
        "\"release_type\": \"Album\"," +
        "\"release_date\": null," +
        "\"genre\": []," +
        "\"primary_release\": null," +
        "\"release_date>=\": \"2010-01-01\"" +
        "}]" +
        "}]";
    return query;
  }

  public boolean run() {
    try {
      HttpTransport httpTransport = new NetHttpTransport();
      HttpRequestFactory requestFactory = httpTransport.createRequestFactory();

      JSONParser parser = new JSONParser();
      GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread");
      url.put("key", Settings.getInstance().getProperty("freebase_api_key"));
      url.put("query", getQuery());

      HttpRequest request = requestFactory.buildGetRequest(url);
      HttpResponse httpResponse = request.execute();

      JSONObject response = (JSONObject) parser.parse(httpResponse.parseAsString());
      JSONArray results = (JSONArray) response.get("result");

      if (results.size() > 0) {
        JSONObject result = (JSONObject) results.get(0);

        twitter_id = (String) result.get("/internet/social_network_user/twitter_id");
        facebook_id = (String) result.get("/internet/social_network_user/facebook_id");

        JSONArray albumsArray = (JSONArray) result.get("album");
        for (Object item : albumsArray) {

          JSONObject objectitem = (JSONObject) item;
          albums.add(new Album((String) objectitem.get("name"), (String) objectitem.get("release_date"), ((ArrayList<String>) objectitem.get("genre"))));
        }
        return true;
      }
      else {
        return false;
      }
    } catch (IOException | ParseException e) {
      System.err.println(e.getMessage());
      return false;
    }
  }


  public String getFacebook_id() {
    return facebook_id;
  }


  public String getTwitter_id() {
    return twitter_id;
  }


  public List<Album> getAlbums() {
    return albums;
  }


}
