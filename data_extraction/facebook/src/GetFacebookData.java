import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Page;
import facebook4j.Post;
import facebook4j.Reading;
import facebook4j.ResponseList;

import java.net.UnknownHostException;

public class GetFacebookData {
	
	private final static String SINCE_YEAR = "2010";
	private final static int LIMIT = 50;

	public static void main(String[] args) {
		try {
			MongoClient mongoClient = new MongoClient();
			DB db = mongoClient.getDB("promotionToolDB");
			
			Facebook facebook = new FacebookFactory().getInstance();
			
			DBCollection coll = db.getCollection("artistsCollection");
			
			DBCollection facebookColl = db.getCollection("fbpostsCollection");
			
			DBCursor cursor = coll.find();
			try {
				while(cursor.hasNext()) {
					DBObject item = cursor.next();
					try {
						Page result = facebook.getPage((String) item.get("facebook_url"));
												
						item.put("facebook_likes", result.getLikes());
						item.put("facebook_talking_about", result.getTalkingAboutCount());
						coll.save(item);
	
						int offset = 0;
						boolean hasData = true;
							
						while (hasData) {
							ResponseList<Post> feed = facebook.getFeed(result.getId(), new Reading().since(SINCE_YEAR).limit(LIMIT).offset(offset));
							for (Post post : feed) {
								// TODO:
								// A facebook cover change is included in a post, we should maybe discard them.
								BasicDBObject new_post = new BasicDBObject("artist_id", item.get("_id")).
			                              append("date", post.getCreatedTime()).
			                              append("message", post.getMessage()).
			                              append("likes", post.getLikes().size()).
			                              append("shared", post.getSharesCount()).
			                              append("picture_attached", post.getPicture() == null || post.getPicture().equals(""));
								facebookColl.insert(new_post);
							}
							offset += LIMIT;
							hasData = (feed.size() != 0);
						}
						
					} catch (FacebookException e) {
						// something went wrong, just discard this entry
					}
				}
			} finally {
				cursor.close();
			}
		}
		 catch (UnknownHostException e) {
			 System.out.println("Error connection to MongoDB");
		}
	}
}
