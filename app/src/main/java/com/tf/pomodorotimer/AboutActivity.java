package com.tf.pomodorotimer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
    private TextView aboutMainTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle(R.string.options_menu_about);
        aboutMainTextView = findViewById(R.id.about_main_text_view);
        aboutMainTextView.setText(getString(R.string.about_app_text));
        Linkify.addLinks(aboutMainTextView, Linkify.WEB_URLS);
    }
}
