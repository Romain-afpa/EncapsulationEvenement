package com.evenement.encapsulation;

import android.content.Context;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

/**
 * Created by romain on 21/03/16.
 */
public class WebViewClient extends android.webkit.WebViewClient {


    @Override
    public void onReceivedSslError(final WebView view, final SslErrorHandler handler, SslError error) {
        Log.d("CHECK", "onReceivedSslError");

        String message = "Certificate error.";
        switch (error.getPrimaryError()) {
            case SslError.SSL_UNTRUSTED:
                message = "The certificate authority is not trusted.";
                break;
            case SslError.SSL_EXPIRED:
                message = "The certificate has expired.";
                break;
            case SslError.SSL_IDMISMATCH:
                message = "The certificate Hostname mismatch.";
                break;
            case SslError.SSL_NOTYETVALID:
                message = "The certificate is not yet valid.";
                break;
        }
        message += " Do you want to continue anyway?";
       handler.proceed();
    }


}
