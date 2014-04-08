package common;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterKeyManager {
  private static volatile TwitterKeyManager instance = null;
  
  private static Twitter twitter = null;
  
  private String[] consumerKeys;
  private String[] consumerSecrets;
  private String[] accessTokens;
  private String[] accessTokenSecrets;
  
  private int length;
  private int index;
  
  public final static TwitterKeyManager getInstance() {
    // See wikipedia article for information on this shenanigan
    if (TwitterKeyManager.instance == null) {
      synchronized(TwitterKeyManager.class) {
        if (TwitterKeyManager.instance == null) {
          TwitterKeyManager.instance = new TwitterKeyManager(
              Settings.getInstance().getProperty("twitter_consumer_keys"),
              Settings.getInstance().getProperty("twitter_consumer_secrets"),
              Settings.getInstance().getProperty("twitter_access_tokens"),
              Settings.getInstance().getProperty("twitter_access_token_secrets"));
        }
      }
    }
    return TwitterKeyManager.instance;
  }
  
  private TwitterKeyManager(String consumerKeys, String consumerSecrets, String accessTokens, String accessTokenSecrets) {
    this.consumerKeys = consumerKeys.split(";");
    this.consumerSecrets = consumerSecrets.split(";");
    this.accessTokens = accessTokens.split(";");
    this.accessTokenSecrets = accessTokenSecrets.split(";");
    
    index = 0;
    length = this.consumerKeys.length;
    
    useNext();
  }
  
  public Twitter getTwitterInstance() {
    return twitter;
  }
  
  public void useNext() {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setOAuthConsumerKey(consumerKeys[index % length])
      .setOAuthConsumerSecret(consumerSecrets[index % length])
      .setOAuthAccessToken(accessTokens[index % length])
      .setOAuthAccessTokenSecret(accessTokenSecrets[index % length]);
    
    TwitterFactory tf = new TwitterFactory(cb.build());
    twitter = tf.getInstance();
    
    index++;
  }
}
