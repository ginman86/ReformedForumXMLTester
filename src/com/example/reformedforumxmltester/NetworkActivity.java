package com.example.reformedforumxmltester;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.webkit.WebView;

import com.example.reformedforumxmltester.MainActivity.rfProgram;
import com.example.reformedforumxmltester.MainActivity.rfXMLParser;

public class NetworkActivity extends Activity {
    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";
    private static final String URL = "http://stackoverflow.com/feeds/tag?tagnames=android&sort=newest";
   
    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false; 
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = true; 
    public static String sPref = null;
    
    public void loadPage() {
    	
       if((sPref.equals(ANY)) && (wifiConnected || mobileConnected)) {
            new DownloadXmlTask().execute(URL);
        }
        else if ((sPref.equals(WIFI)) && (wifiConnected)) {
            new DownloadXmlTask().execute(URL);
        } else {
            // show error
        }  
    }

//Implementation of AsyncTask used to download XML feed from stackoverflow.com.
private class DownloadXmlTask extends AsyncTask<String, Void, String> {
 @Override
 protected String doInBackground(String... urls) {
     try {
         return loadXmlFromNetwork(urls[0]);
     } catch (IOException e) {
         return "IOException";
    	 //return getResources().getString(R.string.connection_error);
     } catch (XmlPullParserException e) {
         return "XML Error";
    	 //return getResources().getString(R.string.xml_error);
     }
 }

 @Override
 protected void onPostExecute(String result) {  
     setContentView(R.layout.activity_main);
     // Displays the HTML string in the UI via a WebView
     WebView myWebView = (WebView) findViewById(R.id.pager);
     myWebView.loadData(result, "text/html", null);
 }
//Uploads XML from stackoverflow.com, parses it, and combines it with
//HTML markup. Returns HTML string.
private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
  InputStream stream = null;
  // Instantiate the parser
  rfXMLParser rfParserMain = new rfXMLParser();
  List<rfProgram> programs = null;
  Calendar rightNow = Calendar.getInstance(); 
  SimpleDateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");
      
  // Checks whether the user set the preference to include summary text
  //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
  //boolean pref = sharedPrefs.getBoolean("summaryPref", false);
      
  StringBuilder htmlString = new StringBuilder();
  htmlString.append("<h3>" + getResources().getString(R.string.app_name) + "</h3>");
  htmlString.append("<em>" + getResources().getString(R.string.hello_world) + " " + 
          formatter.format(rightNow.getTime()) + "</em>");
      
  try {
      stream = downloadUrl(urlString);        
      programs = rfParserMain.parse(stream);
  // Makes sure that the InputStream is closed after the app is
  // finished using it.
  } finally {
      if (stream != null) {
          stream.close();
      } 
   }
  
  // StackOverflowXmlParser returns a List (called "entries") of Entry objects.
  // Each Entry object represents a single post in the XML feed.
  // This section processes the entries list to combine each entry with HTML markup.
  // Each entry is displayed in the UI as a link that optionally includes
  // a text summary.
  for (rfProgram program : programs) {       
      htmlString.append("<p><a href='");
      htmlString.append(program.htmlUrl);
      htmlString.append("'>" + program.title + "</a></p>");
      // If the user set the preference to include summary text,
      // adds it to the display.
      //if (pref) {
          htmlString.append(program.text);
     // }
  }
  return htmlString.toString();
}

//Given a string representation of a URL, sets up a connection and gets
//an input stream.
private InputStream downloadUrl(String urlString) throws IOException {
  java.net.URL url = new java.net.URL(urlString);
  java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
  conn.setReadTimeout(10000 /* milliseconds */);
  conn.setConnectTimeout(15000 /* milliseconds */);
  conn.setRequestMethod("GET");
  conn.setDoInput(true);
  // Starts the query
  conn.connect();
  return conn.getInputStream();
}
 
}
}