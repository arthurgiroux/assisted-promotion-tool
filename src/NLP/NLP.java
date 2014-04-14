package NLP;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import common.DBHelper;
import common.Event;
import common.Event.TYPE;

public class NLP {
  
  private ObjectId artist_id;
  
  private ObjectId album_id;
  
  private Date release_date;
  
  private MaxentTagger tagger;
  
  private String artist_name;
  
  private String album_name;
  
  private DBObject matrix_row;
  
  private final static long MILLISECS_PER_DAY = 24 * 60 * 60 * 1000;
  
  private final static String[] STEMS = {"launch", "conference", "publish", "avail", "store", "releas", "album", "singl", "new", "interview" , "press", "campaign", "cover", "video", "clip", "hit", "countdown", "first", "show", "announc", "CD" };
  
  public NLP(DBObject matrix_row, MaxentTagger tagger) {
    this.matrix_row = matrix_row;
    this.artist_id = (ObjectId) matrix_row.get("artistId");
    this.album_id = (ObjectId) matrix_row.get("albumId");
    this.release_date = (Date) matrix_row.get("albumReleaseDate");
    this.tagger = tagger;
    this.artist_name = (String) matrix_row.get("artistName");
    this.album_name =  (String) matrix_row.get("albumName");
    if (album_name == null) {
      album_name = "";
    }
  }
  
  public void run() {
    /*
     * release of a single
        CD release show
        press campaign for album
        pre-sale campaign
        first twitter announcement
        first facebook announcement 
        count down
        album announcement
        album cover (picture)

     */
    DBHelper dbHelper = DBHelper.getInstance();
    
    Calendar c = Calendar.getInstance(); 
    c.setTime(release_date);
    c.add(Calendar.MONTH, -4);
    Date date = c.getTime();
    
    DBCursor previousAlbum = dbHelper.findPreviousAlbum(artist_id, release_date, date).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
    
    // If the previous album was released less than 4 month ago then we take the release date + 15 days as our start date
    if (previousAlbum.count() > 0) {
      DBObject previous = previousAlbum.next();
      date = (Date) previous.get("release_date");
      c.setTime(date);
      c.add(Calendar.DAY_OF_YEAR, 15);
      date = c.getTime();
    }
    
    HashMap<TYPE, Event> eventsFound = new HashMap<TYPE, Event>();
    
    System.out.println("treating : " + artist_name + " " + album_name);
    
    BasicDBObject query = new BasicDBObject("artist_id", artist_id).append("date", new BasicDBObject("$gt", date).append("$lte", release_date));
    DBCursor cursor = dbHelper.findTweetsByArtistId(query).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
        
    while (cursor.hasNext()) {
      DBObject item = cursor.next();
      String message = (String) item.get("message");
      int stemsFound = 0;
      for (String stem : STEMS) {
        if (message.contains(stem)) {
          stemsFound++;
        }
      }
      
      if (stemsFound > 0) {
        
        // We discard retweets and replies
        if (message.startsWith("RT") || message.startsWith("@")) {
          continue;
        }
        
        
        List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(message));
        
        for (List<HasWord> sentence : sentences) {
          ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);
          ArrayList<String> words = new ArrayList<String>();
          String words_str = "";
          
          for (TaggedWord word : tSentence) {
            words_str += word.value() + " ";
            words.add(word.value());
          }
         
          // We check for possessive rules
          if (keepMessage(tSentence)) {
            TYPE type = null;
            if (oneContains(words, new String[] { "singl" }) && oneContains(words, new String[]{ "releas", "new", "hit", "first", "album" })) {
              type = TYPE.SINGLE_RELEASE;
            }
            else if (containsAll(words,new String[] {"CD", "releas", "show" })) {
              type = TYPE.CD_RELEASE_SHOW;
            }
            else if (oneContains(words, new String[] { "press" }) && oneContains(words, new String[] { "conference", "interview", "campaign", "releas" })) {
              type = TYPE.PRESS_CAMPAIGN;
            }
            else if (oneContains(words, new String[] { "presale", "pre-sale", "preorder", "pre-order" }) && oneContains(words, new String[] { "album", "CD" })) {
              type = TYPE.PRESALE_CAMPAIGN;
            }
            else if (!oneContains(words, new String[] { "tour" }) && (oneContains(words, new String[] { "countdown" }) || containsAll(words, new String[] { "day", "releas" }) || words_str.toLowerCase().contains(album_name.toLowerCase()))) {
              type = TYPE.COUNTDOWN;
            }
            else if (containsAll(words, new String[] { "album", "cover", "releas" })) {
              type = TYPE.ALBUM_COVER;
            }
            else if (oneContains(words, new String[] { "interview"})) {
              type = TYPE.INTERVIEW;
            }
            else if (containsAll(words, new String[] { "announc", "album" })) {
              type = TYPE.ANNOUNCEMENT;
            }
            else if (containsAll(words, new String[] { "teaser", "album" })) {
              type = TYPE.TEASER;
            }
            else if (oneContains(words, new String[] { "video", "clip" })) {
              type = TYPE.VIDEO_CLIP;
            }

            if (type != null) {
              if (!eventsFound.containsKey(type)) {
                eventsFound.put(type, new Event(type, (Date) item.get("date")));
              }
              break;
            }
          }
        }
      }
    }
    
    System.out.println("events for :" + artist_name + " " + album_name);
    for (TYPE everyType : Event.TYPE.values()) {
      matrix_row.put("has_" + everyType.name().toLowerCase(), eventsFound.containsKey(everyType));
      
      if (eventsFound.containsKey(everyType)) {
        long days = (release_date.getTime() - eventsFound.get(everyType).getDate().getTime()) / MILLISECS_PER_DAY; 
        matrix_row.put("days_" + everyType.name().toLowerCase(), days);
      }
    }
    System.out.println("found : " + eventsFound.size()  + " events");
    dbHelper.updateMatrixRow(matrix_row);
  }
  
  // return if the list of words contains at least one of the given stems
  private boolean oneContains(ArrayList<String> words, String[] stems) {
    for (String word : words) {
      if (containsStem(word, stems)) {
        return true;
      }
    }
    return false;
  }
  
  // Return if the list of words contains all the given stems
  private boolean containsAll(ArrayList<String> words, String[] stems) {
    for (String word : words) {
      if (!containsStem(word, stems)) {
        return false;
      }
    }
    return true;
  }
  
  // Return if a word contains at least one of the given stems
  private boolean containsStem(String message, String[] stems) {
    for (String stem : stems) {
      if (message.toLowerCase().startsWith(stem)) {
        return true;
      }
    }
    return false;
  }
 
  
  /*
   1st case: 
  -possessive case (POS): ('s)
    Check if it's noun. If, noun= artist, or noun=album, noun is in taglist
      Check next noun2 (NN)
        If noun2 is in tag list or noun2=album_name, we keep message. (Example: Coldplay's album release)


2nd case:
  -possessive pronoun (PRP$):
    if PRP$=my or PRP$=our
      if next NN is in taglist or NN=album_name, keep message. (Example: my album release)
    else:
      if noun(before the pronoun)=artist 
        if next noun NN is in taglist or noun = album_name, we keep message. (Example: Coldplay released their album)
   */
  
  // Check the special case of possessive, if the message contains a possessive we need to check if it's
  // talking about the artist or not
  private boolean keepMessage(ArrayList<TaggedWord> tSentence) {
    
    for (int i = 0; i < tSentence.size(); ++i) {
      // We retrieve the first and last words of the artist and album name
      String[] artist_name_tokenized = artist_name.split(" ");
      String last_token_artist_name = artist_name_tokenized[artist_name_tokenized.length - 1];
      String[] album_name_tokenized = album_name.split(" ");
      String last_token_album_name = album_name_tokenized[album_name_tokenized.length - 1];
      
      TaggedWord word = tSentence.get(i);
      // If the possessive is a "'s"
      if (word.tag().equals("POS")) {
        // Then we check if the word contained a stem or the last word of artist / album name
        // I.e : Coldplay's, release's
        if (containsStem(word.value(), STEMS) || containsStem(word.value(), new String [] { last_token_artist_name,  last_token_album_name } )) {
          // We then retrieve the next noun in the sentence
          for (int j = i+1; i < tSentence.size(); ++j) {
            TaggedWord nextWord = tSentence.get(j);
            // If it's a noun
            if (nextWord.tag().equals("NN")) {
              // We check if the noun contains a stem or the first word of the album name
              // I.e : Check Coldplay's newest album
              // Check Coldplay's Ghost 
              return (containsStem(nextWord.value(), STEMS) || containsStem(nextWord.value(), new String[] { album_name_tokenized[0] }));
            }
          }
        }
      }
      
      // If the possessive is a pronoun (my, our, their etc.)
      else if (word.tag().equals("PRP$")) {
        // We only care if it's my or our
        if ((word.value().toLowerCase().equals("my") || word.value().toLowerCase().equals("our"))) {
          // We retrieve the next noun
          for (int j = i; i < tSentence.size(); ++j) {
            TaggedWord nextWord = tSentence.get(j);
            if (nextWord.tag().equals("NN")) {
              // Check if the noun contains a stem or first word of the album name
              // I.e : Our new album is online
              // my latest album is finally available
              return (containsStem(nextWord.value(), STEMS) || containsStem(nextWord.value(), new String[] { album_name_tokenized[0] }));
            }
          }
        }
        // If the possessive is their or something else
        else {
          // We Retrieve the previous noun
          for (int j = i-1; j >= 0; --j) {
            TaggedWord nextWord = tSentence.get(j);
            if (nextWord.tag().equals("NN")) {
              // Check if the noun contains a stem or last word of the album name
              // I.e : Coldplay released their album
              return (containsStem(nextWord.value(), STEMS) || containsStem(nextWord.value(), new String[] { last_token_album_name }));
            }
          }
        }
      }
      // If it doesn't contains a possessive we keep it
      else {
        return true;
      }
    }
    
    // In the other cases we discard
    return false;
  }
  
  public static void main(String[] args) {
        
    DBHelper db = DBHelper.getInstance();
    
    MaxentTagger tagger = new MaxentTagger("models/english-left3words-distsim.tagger");
    
    DBCursor cursor = db.findMatrixRows().addOption(Bytes.QUERYOPTION_NOTIMEOUT);

    while (cursor.hasNext()) {
      DBObject item = cursor.next();
      NLP worker = new NLP(item, tagger);
      worker.run();
    }
    
    System.out.println("NLP DONE");
  }
}
