package main;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import common.DBHelper;

public class WebServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/status", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            DBHelper db = DBHelper.getInstance();
            
            String response = "# Artists: " + db.countArtists() + "\n" +
                "# Albums: " + db.countAlbums() + "\n" +
                "# Facebook Posts: " + db.countFacebookPosts() + "\n" +
                "# Tweets: " + db.countTweets() + "\n" + 
                "Data size: " + db.getDataSize() + "\n";
            
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
