package recommender;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {
  
  public static void main(String[] args) throws Exception {
    if (args.length != 5) {
      System.out.println("Usage : WebServer <cluster weight> <region weight> <category weight> <threshold> <min_result_nb>");
      System.exit(-1);
    }
    
    Recommender.CLUSTER_WEIGHT = Float.parseFloat(args[0]);
    Recommender.REGION_WEIGHT = Float.parseFloat(args[1]);
    Recommender.CATEGORY_WEIGHT = Float.parseFloat(args[2]);
    
    Recommender.THRESHOLD = Float.parseFloat(args[3]);
    Recommender.MIN_RESULTS_NB = Integer.parseInt(args[4]);
    
    if (Recommender.CLUSTER_WEIGHT 
        + Recommender.REGION_WEIGHT 
        + Recommender.CATEGORY_WEIGHT > 1) {
      System.out.println("Check weights !");
      System.exit(-1);
    }
    
    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
    server.createContext("/recommend", new MyHandler());
    server.setExecutor(null); // creates a default executor
    server.start();
  }

  static class MyHandler implements HttpHandler {
    private static Map<String, String> parseGetParameters(HttpExchange t) throws UnsupportedEncodingException {
      URI requestedUri = t.getRequestURI();
      String query = requestedUri.getRawQuery();

      String pairs[] = query.split("[&]");

      Map<String, String> get = new HashMap<String, String>();
      for (String pair : pairs) {
        String param[] = pair.split("[=]");

        if (param.length == 2) {
          String key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
          String value = get.get(key);
          value = (value == null ? "" : value+",") + URLDecoder.decode(param[1], System.getProperty("file.encoding"));

          get.put(key, value);
        }
      }
      return get;
    }
    
    public void handle(HttpExchange t) throws IOException {
      Map<String, String> get = parseGetParameters(t);
      
      System.out.println("Handling request : " + get);
      
      String response = "";
      
      try {
        String callback = get.get("callback");
        
        String region = get.get("region");
        String[] categories = get.get("categories").split(",");
        int facebookLikes = Integer.parseInt(get.get("fbLikes"));
        int twitterFollowers = Integer.parseInt(get.get("twFollowers"));
        int albumsCount = Integer.parseInt(get.get("albumCount"));
        
        Recommender r = new Recommender(region, categories, facebookLikes, twitterFollowers, albumsCount);
        
        Map<String, Double> result = r.recommend();
        
        Map<String, String> stats = r.getStats();
        
        response += callback + "(\n";
        response += "{\n \"results\" : [\n";
        boolean first = true;
        for (Entry<String, Double> e : result.entrySet()) {
          if (!first) {
            response += ",\n";
          }
          response += "  {\n";
          response += "    \"event\" : \"" + JSONObject.escape(e.getKey()) + "\",\n";
          response += "    \"days\" : " + Math.round(e.getValue()) + "\n";
          response += "  }";
          first = false;
        }
        response += "],\n";
        response += " \"stats\" : {\n";
        first = true;
        for (Entry<String, String> e : stats.entrySet()) {
          if (!first) {
            response += ",\n";
          }
          response += "  \"" + e.getKey() + "\" : \"" + JSONObject.escape(e.getValue()) + "\"";
          first = false;
        }
        response += "\n }\n})\n";
      } catch (NumberFormatException e) {
        response = "{ \"error\" : \"Malformed request\"}";
      } catch (NullPointerException e) {
        response = "{ \"error\" : \"Malformed request\"}";
      }
      
      Headers header = t.getResponseHeaders();
      header.add("Content-Type", "application/javascript");
      
      byte[] toSend = response.getBytes();
      
      t.sendResponseHeaders(200, toSend.length);
      OutputStream os = t.getResponseBody();
      os.write(toSend);
      os.close();
    }
  }

}
