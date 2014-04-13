package data_extraction.freebase;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import common.DBHelper;
import common.FreeBaseKeyManager;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AlbumCounter {

    private static String getQuery(String artistName) {
        String query = "[{"
                + "\"type\": \"/music/album\","
                + "\"artist\": \"" + JSONObject.escape(artistName) + "\","
                //+ "\"album_content_type\": \"Studio album\"," // Seems too restrictive
                + "\"release_type\": \"Album\"," 
                + "\"return\": \"count\""
                + "}]";
        return query;
    }

    private static int getAlbumCountForArtist(String artistName) throws Error {

        HttpTransport httpTransport = new NetHttpTransport();
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();

        GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread");
        url.put("key", FreeBaseKeyManager.getInstance().getKey());
        url.put("query", getQuery(artistName));

        HttpRequest request;

        try {
            request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = request.execute();

            JSONObject response = (JSONObject) new JSONParser().parse(httpResponse.parseAsString());

            JSONArray r = (JSONArray) (response.get("result"));

            return ((Long) (r.get(0))).intValue();

        } catch (IOException e) {
            if (e.getMessage().contains("dailyLimitExceeded")) {
                FreeBaseKeyManager.getInstance().useNext();
                return getAlbumCountForArtist(artistName);
            }
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        throw new Error("Could not retrieve album count for " + artistName);
    }

    public static void main(String[] args) {
        DBHelper db = DBHelper.getInstance();

        DBCursor cursor = db.findAllArtists();

        while (cursor.hasNext()) {
            try {

                DBObject item = cursor.next();

                String name = (String) (item.get("name"));
                
                int albumCount = getAlbumCountForArtist(name);

                System.out.println(name + " : " + albumCount);
                
                //db.updateArtistAlbumCount(item, albumCount); REFINE THE QUERY FIRST
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        
        cursor.close();
    }
}
