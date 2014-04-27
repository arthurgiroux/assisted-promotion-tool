/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package echonestbot;

//import java.net.UnknownHostException;
//import java.util.logging.Level;
//import java.util.logging.Logger;

/**
 *
 * @author Amine Benabdeljalil
 */
public class DBInsertArtist implements Runnable{
    
    private Thread t;
    DBHelper dbHelper;
    CustomSong customSong;
    
    
    public DBInsertArtist(CustomSong cust, DBHelper db){
        this.customSong = cust;
        this.dbHelper = db;
    }
    
    @Override
    public void run() {
        dbHelper.writeSongEchoNest(this.customSong);
        System.out.println("Exiting Thread - Terminated");
    }
    
    public void start(){
      if (t == null)
      {
         t = new Thread (this);
         t.start ();
      }
    }
    
}
