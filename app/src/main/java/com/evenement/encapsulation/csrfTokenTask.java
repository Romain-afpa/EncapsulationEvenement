package com.evenement.encapsulation;

import android.os.AsyncTask;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

    private TextView tokenField;
    private HttpsURLConnection connection = null;
    private URL url = null;
    private InputStream stream = null;
    private WebView webView;
    private String csrfToken;
    private String formAction;

    public csrfTokenTask(WebView webView) {

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

        if (connection != null) {
             connection.disconnect();
        }

        String queryString = "signin[username]=librinfo&signin[password]=cR4MP0u=€&signin[_csrf_token]=" + csrfToken;

        webView.postUrl("https://dev3.libre-informatique.fr" + formAction, queryString.getBytes());
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

            connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setRequestMethod("GET");

            if (cookies != null)
                connection.setRequestProperty("Cookie", cookies);

            connection.connect();

            switch(connection.getResponseCode()) {

                case 200:
                    stream = connection.getInputStream();
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

    private void getSessionCookie() {

    }
}//taskClass
