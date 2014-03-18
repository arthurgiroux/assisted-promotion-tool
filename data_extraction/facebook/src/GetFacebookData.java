import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Page;
import facebook4j.Post;
import facebook4j.Reading;
import facebook4j.ResponseList;

import java.net.UnknownHostException;
import java.util.Set;

public class GetFacebookData {

	public static void main(String[] args) {
		try {
			MongoClient mongoClient = new MongoClient();
			DB db = mongoClient.getDB("bigdata");
			Set<String> colls = db.getCollectionNames();
			

			for (String s : colls) {
			    System.out.println(s);
			}
			
			Facebook facebook = new FacebookFactory().getInstance();
			
			Page result = facebook.getPage("daftpunk");
			
			System.out.println("Likes : " + result.getLikes());
			System.out.println("Talking about : " + result.getTalkingAboutCount());
			

			String since = "2010";
			int limit = 50;
			int offset = 0;
			ResponseList<Post> feed = facebook.getFeed(result.getId(), new Reading().since(since).limit(limit));
			boolean hasData = true;
			
			while (hasData) {
				ResponseList<Post> tmpFeed = facebook.getFeed(result.getId(), new Reading().since(since).limit(limit).offset(offset));
				feed.addAll(tmpFeed);
				offset += limit;
				hasData = (tmpFeed.size() != 0);
			}
			
			System.out.println(feed.size());
			
		} catch (UnknownHostException | FacebookException e) {
			e.printStackTrace();
		}


	}

}
