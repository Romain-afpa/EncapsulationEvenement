package com.evenement.encapsulation;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * Created by romain on 21/03/16.
 */
public class csrfTokenTask extends AsyncTask<String, String, String> {

    private Context context;

    public csrfTokenTask(Context context) {
        this.context = context;
    }


    @Override
    protected String doInBackground(String... params) {

        String html = readStream(getConnectionStream(params[0]));

        return getToken(html);

    }

    @Override
    protected void onPostExecute(String token) {
        super.onPostExecute(token);

        Log.d("aa", token + "");


    }

    private String readStream(InputStream stream) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        StringBuffer buffer = new StringBuffer();

        String line = "";

        try {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return buffer.toString();
    }

    private InputStream getConnectionStream(String uri) {

        HttpsURLConnection connection = null;
        URL url = null;
        InputStream stream = null;

        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {

            assignTrustManager();
            connection = (HttpsURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);


            connection.connect();
            stream = connection.getInputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (connection != null) {
            connection.disconnect();
        }

        return stream;

    }

    private void assignTrustManager() {

        TrustManager manager = new TrustManager();

        TrustManager[] trustAllCerts = new TrustManager[]{manager};


        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getToken(String html) {

        Document doc = Jsoup.parse(html);

        Element tokenTag = doc.getElementById("signin__csrf_token");

        return tokenTag.attr("value");
    }
}//taskClass
