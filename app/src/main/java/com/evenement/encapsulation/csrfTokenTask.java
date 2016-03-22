package com.evenement.encapsulation;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    private HttpsURLConnection connection = null;
    private URL url = null;
    private InputStream stream = null;

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

        if (connection != null) {
             connection.disconnect();
        }
        Log.d("token", token);

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

        assignTrustManager();

        try {
            url = new URL(uri);
            Log.d("aa",url.getHost()+ url.getPath()+": "+ url.getProtocol()+", " + url.getDefaultPort());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {

            connection = (HttpsURLConnection) url.openConnection();
Log.d("aa", connection.getHostnameVerifier()+"");
            connection.setDoInput(true);
            connection.setDoOutput(true);


            connection.connect();

            stream = connection.getInputStream();

        } catch (IOException e) {
            e.printStackTrace();

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
    Log.d("aa", html);

        Elements tokenTag = doc.getElementsByAttributeValue("id", "signin__csrf_token");

        Log.d("aa", tokenTag.toString());

        String token = tokenTag.attr("value");
        Log.d("aa", tokenTag.attr("id"));
        return token;
    }
}//taskClass
