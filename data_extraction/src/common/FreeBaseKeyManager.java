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
    return keys[index];
  }
  
  public void useNext() {
    index++;
    if (index >= keys.length) {
      try {
        Thread.sleep(24*3600);
      } catch (InterruptedException e) {
        System.err.println("FreeBase didn't wait :(");
      }
    }
    index %= keys.length;
  }
}
