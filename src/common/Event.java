package common;

import java.util.Date;

public class Event {
  public static enum TYPE { SINGLE_RELEASE, CD_RELEASE_SHOW, PRESS_CAMPAIGN, PRESALE_CAMPAIGN, FIRST_TWEET, FIRST_FB,
    COUNTDOWN, ANNOUNCEMENT, ALBUM_COVER, INTERVIEW, VIDEO_CLIP, TEASER };
  
  private TYPE type;
  private Date date;
  
  public Event(TYPE type, Date date) {
    super();
    this.type = type;
    this.date = date;
  }

  public TYPE getType() {
    return type;
  }

  public Date getDate() {
    return date;
  }

}
