package com.evenement.encapsulation;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * Created by romain on 21/03/16.
 */
public class LoginTask extends AsyncTask<String, String, String> {

    private HttpsURLConnection connection = null;
    private URL url = null;
    private InputStream stream = null;
    private WebView webView;
    private String csrfToken;
    private String formAction;
    private final String username = "librinfo";
    private final String password = "cR4MP0u=€";

    public LoginTask() {
    }

    public LoginTask(WebView webView) {

        this.webView = webView;
    }


    @Override
    protected String doInBackground(String... params) {

        String html = readStream(getConnectionStream(params[0]));

        parseResponse(html);

        return "";
    }

    @Override
    protected void onPostExecute(String useless) {
        super.onPostExecute(useless);

        postLogin("https://dev3.libre-informatique.fr" + formAction);

        //webView.postUrl("https://dev3.libre-informatique.fr" + formAction, getQuery().getBytes());
        webView.loadUrl("https://dev3.libre-informatique.fr/tck.php/ticket/control");

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

        try {

            url = new URL(uri);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        assignTrustManager();

        try {
            String cookies = CookieManager.getInstance().getCookie(uri);
            //Log.d("aa", cookies);
//CookieManager.getInstance().removeAllCookie();
            connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; D5803 Build/23.4.A.1.264; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/48.0.2564.106 Mobile Safari/537.36");

            //if (cookies != null) {
               // connection.setRequestProperty("Cookie", cookies);
            //}

            connection.connect();

            switch (connection.getResponseCode()) {

                case 200:
                    stream = connection.getInputStream();
                    break;

                case 401:
                    stream = connection.getErrorStream();
                    break;

                default:
                    stream = connection.getErrorStream();
                    break;
            }
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

    private void parseResponse(String html) {

        Document doc = Jsoup.parse(html);

        Elements tokenTag = doc.select("#signin__csrf_token");
        csrfToken = tokenTag.attr("value");

        Elements formTag = doc.select(".login form");
        formAction = formTag.attr("action");
    }

    private void postLogin(String uri) {

        String cookies = CookieManager.getInstance().getCookie(uri);

        HttpsURLConnection conn = null;
        URL url = null;

        try {
            url = new URL(uri);

            assignTrustManager();

            conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; D5803 Build/23.4.A.1.264; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/48.0.2564.106 Mobile Safari/537.36");

            if(cookies != null) {
                conn.setRequestProperty("Cookie", cookies);
            }
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            writer.write(getQuery());
            writer.flush();

            conn.connect();
            Log.d("aa", conn.getResponseCode() + "");
            Log.d("aa", conn.getResponseMessage() + "");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getQuery() {

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("signin[username]", username)
                .appendQueryParameter("signin[password]", password)
                .appendQueryParameter("signin[_csrf_token]", csrfToken);

        return builder.build().getEncodedQuery();
    }
}//taskClass
