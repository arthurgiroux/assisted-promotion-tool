package com.mycompany.dftwitterbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import twitter4j.PagableResponseList;
import twitter4j.Query;
import twitter4j.QueryResult;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class DFTwitterBot implements Serializable {

    private static final long serialVersionUID = 1L;

    private Twitter twitter;
    private AccessToken aT;
    private long aTID;
    private static boolean dbEnabled = false;

    public static void main(String args[]) throws Exception {
        DFTwitterBot dftb = new DFTwitterBot();

        File file = new File("twitterdetails.dftb");
        boolean exists = file.exists();
        if (exists) {
            dftb = readObject(dftb);
        } else if (!exists) {
            getTwitterAccess(dftb);
        }

        //MongoDb Connection
        //DBHelper dbHelper = new DBHelper("localhost", 27017);
        //dbEnabled = true;
        if (dbEnabled) {
            //List<String> keywordsList = dbHelper.readKeywords();
            //batchProcessing(dbHelper, dftb, keywordsList);
        } else {
            while (true) {
                menu(dftb);
            }
        }
    }

    private static void menu(DFTwitterBot dftb) throws TwitterException, IOException {
        System.out.println("Please enter your query:");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String option = br.readLine();

        viewQuery(dftb, option);

    }

    private static void viewTimeline(DFTwitterBot dftb) throws TwitterException {
        List<Status> statuses = dftb.twitter.getUserTimeline("DaftPunk");
        System.out.println("Showing friends timeline.");
        for (Status status : statuses) {
            System.out.println("User : " + status.getUser().getScreenName() + " Date : " + status.getCreatedAt() + " Text : " + status.getText() + " Retweet Count : " + status.getRetweetCount());//+ "".equals(status.getPlace()) ? "" : status.getPlace().getCountry());

        }
    }

    private static void viewQuery(DFTwitterBot dftb, String queryString) throws TwitterException {
        Query query = new Query(queryString);
        //query.setCount(10);
        //query.setQuery("source:twitter4j yusukey");
        //query.setSince("2013-12-10");
        QueryResult queryResult = dftb.twitter.search(query);
        List<Status> statuses = queryResult.getTweets();
        System.out.println("Showing query result");
        for (Status status : statuses) {
            System.out.println("User : " + status.getUser().getScreenName() + " Date : " + status.getCreatedAt() + " Text : " + status.getText() + " Retweet Count : " + status.getRetweetCount());//+ "".equals(status.getPlace()) ? "" : status.getPlace().getCountry());
        }
    }

    private static void storeData(DBHelper dbHelper, List<String> keywordsList, String source, Date createdAt, String text, int retweetCount, String country) {

        List<String> keywordsFound = new ArrayList<String>();

        for (String keyword : keywordsList) {
            if (text.contains(keyword)) {
                keywordsFound.add(keyword);
            }
        }

        dbHelper.write(source, createdAt, text, keywordsFound, retweetCount, country);
    }

    private static DFTwitterBot readObject(DFTwitterBot dftb) {
        try {
            FileInputStream fileIn
                    = new FileInputStream("twitterdetails.dftb");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            dftb = (DFTwitterBot) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return dftb;
        } catch (ClassNotFoundException c) {
            System.out.println(" DFTB class not found");
            c.printStackTrace();
            return dftb;
        }

        System.out.println("Deserialized Access...");
        System.out.println("Access Token ID: " + dftb.aTID);
        System.out.println("Access Token: " + dftb.aT.toString());
        System.out.println("Tweitter: " + dftb.twitter.toString());
        return dftb;

    }

//    private static void updateStatus(DFTwitterBot dftb) throws TwitterException, IOException {
//        String statusUpdate = null;
//
//        System.out.println("Please enter a status:");
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        statusUpdate = br.readLine();
//        if (statusUpdate.length() > 0) {
//            StatusUpdate su = new StatusUpdate(statusUpdate);
//            Status status = dftb.twitter.updateStatus(su);
//            System.out.println("Successfully updated the status to [" + status.getText() + "].");
//        } else {
//            menu(dftb);
//        }
//    }
    private static void getTwitterAccess(DFTwitterBot dftb) throws IOException, TwitterException {
// The factory instance is re-useable and thread safe.
        dftb.twitter = new TwitterFactory().getInstance();
        dftb.twitter.setOAuthConsumer("PCFBj9LcpRurejDkzylTxg", "piBLUgyW3dTp7471yIL8h3c1cwXkvTllCkNIBA5pb5g");
        RequestToken requestToken = dftb.twitter.getOAuthRequestToken();
        dftb.aT = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (null == dftb.aT) {
            System.out.println("Open the following URL and grant access to your account:");
            System.out.println(requestToken.getAuthorizationURL());
            System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
            String pin = br.readLine();
            try {
                if (pin.length() > 0) {
                    dftb.aT = dftb.twitter.getOAuthAccessToken(requestToken, pin);
                } else {
                    dftb.aT = dftb.twitter.getOAuthAccessToken();
                }
            } catch (TwitterException te) {
                if (401 == te.getStatusCode()) {
                    System.out.println("Unable to get the access token.");
                } else {
                    te.printStackTrace();
                }
            }

        }

        dftb.aTID = dftb.twitter.verifyCredentials().getId();
        writeObject(dftb);
//System.exit(0);
    }

    private static void writeObject(DFTwitterBot dftb) throws IOException {
        FileOutputStream fileOut
                = new FileOutputStream("twitterdetails.dftb");
        ObjectOutputStream out
                = new ObjectOutputStream(fileOut);
        System.out.println("Serialising:...");
        System.out.println("Access Token ID: " + dftb.aTID);
        System.out.println("Access Token: " + dftb.aT.toString());
        System.out.println("Tweitter: " + dftb.twitter.toString());
        out.writeObject(dftb);
        out.close();
        fileOut.close();

    }

    private static void batchProcessing(DBHelper dbHelper, DFTwitterBot dftb, List<String> keywordsList) throws TwitterException {
        List<String> albumList = dbHelper.readAlbumNamesToProcess();

        for (String albumName : albumList) {
            Query query = new Query("q=" + albumName);
            //query.setCount(10);
            //query.setQuery("source:twitter4j yusukey");
            //query.setSince("2013-12-10");
            QueryResult queryResult = dftb.twitter.search(query);
            List<Status> statuses = queryResult.getTweets();
            System.out.println("Showing query result");
            for (Status status : statuses) {
                System.out.println("User : " + status.getUser().getScreenName() + " Date : " + status.getCreatedAt() + " Text : " + status.getText() + " Retweet Count : " + status.getRetweetCount());//+ "".equals(status.getPlace()) ? "" : status.getPlace().getCountry());
                if (dbEnabled) {
                    storeData(dbHelper, keywordsList, status.getUser().getScreenName(), status.getCreatedAt(), status.getText(), status.getRetweetCount(), status.getPlace() == null ? null : status.getPlace().getCountry());
                }
            }
        }

    }
}
