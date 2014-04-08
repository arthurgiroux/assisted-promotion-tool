package common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/***
 * Singleton class for the settings
 * Template from wikipedia
 * @author arthur
 *
 */
public class Settings {

  private static volatile Settings instance = null;

  private Properties properties;

  private Settings() {
    super();

    properties = new Properties();
    try {
      properties.load(new FileInputStream("credentials.properties"));
    } catch (IOException e) {

    }
  }

  public final static Settings getInstance() {

    // See wikipedia article for information on this shenanigan
    if (Settings.instance == null) {
      synchronized(Settings.class) {
        if (Settings.instance == null) {
          Settings.instance = new Settings();
        }
      }
    }
    return Settings.instance;
  }

  public String getProperty(String key) {
    return (String) properties.get(key);
  }

}
