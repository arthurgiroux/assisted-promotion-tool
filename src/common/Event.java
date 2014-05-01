package common;

import java.util.Date;

public class Event {
  public static enum TYPE { SINGLE_RELEASE, CD_RELEASE_SHOW, PRESS_CAMPAIGN, PRESALE_CAMPAIGN, FIRST_TWEET, FIRST_FB,
    COUNTDOWN, ANNOUNCEMENT, ALBUM_COVER, INTERVIEW, VIDEO_CLIP, TEASER };
  public static String[] NAMES = { "single_release", "cd_release_show", "press_campaign", "presale_campaign", "first_tweet", "first_fb", 
    "countdown", "announcement", "album_cover", "interview", "video_clip", "teaser" };
  
  private TYPE type;
  private Date date;
  private float score = 0;
  
  public Event(TYPE type, Date date) {
    super();
    this.type = type;
    this.date = date;
  }
  
  public Event(TYPE type, Date date, float score) {
    super();
    this.type = type;
    this.date = date;
    this.score = score;
  }

  public TYPE getType() {
    return type;
  }

  public Date getDate() {
    return date;
  }
  
  public float getScore() {
    return score;
  }

}
