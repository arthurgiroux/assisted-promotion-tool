package common;

public class FreeBaseKeyManager {
  private static volatile FreeBaseKeyManager instance = null;
  private String[] keys;
  private int index;
  
  public final static FreeBaseKeyManager getInstance() {
    // See wikipedia article for information on this shenanigan
    if (FreeBaseKeyManager.instance == null) {
      synchronized(FreeBaseKeyManager.class) {
        if (FreeBaseKeyManager.instance == null) {
          FreeBaseKeyManager.instance = new FreeBaseKeyManager(Settings.getInstance().getProperty("freebase_api_key"));
        }
      }
    }
    return FreeBaseKeyManager.instance;
  }
  
  private FreeBaseKeyManager(String keys) {
    this.keys = keys.split(";");
    index = 0;
  }
  
  public String getKey() {
    System.out.println("API key " + keys[index]);
    return keys[index];
  }
  
  public void useNext() {
    System.out.println("Switching freebase API key");
    index++;
    if (index >= keys.length) {
      try {
        System.out.println("No more freebase keys, sleeping for 24h");
        Thread.sleep(24*3600000);
      } catch (InterruptedException e) {
        System.err.println("FreeBase didn't wait :(");
      }
    }
    index %= keys.length;
  }
}
