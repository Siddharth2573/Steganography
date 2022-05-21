package com.stegano.strenggeheim.activity;

import static com.stegano.strenggeheim.Constants.ASSET_ABOUT_US;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.stegano.strenggeheim.R;

public class AboutUsActivity extends AppCompatActivity {
    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        webView = findViewById(R.id.aboutUsContent);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(ASSET_ABOUT_US);

    }

}
