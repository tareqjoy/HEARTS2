package com.example.hearts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainMenu extends AppCompatActivity {
    private Button playBtn, rulesBtn, aboutBtn, playBotBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        initViews();
        initPlayBtnOnClickActions();
        rulesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://bicyclecards.com/how-to-play/hearts/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        aboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainMenu.this, "Developed by Tareq Rahman Joy,CSE, KUET! Thanks for playing :)", Toast.LENGTH_LONG).show();
            }
        });



    }

    private void initPlayBtnOnClickActions() {
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(MainMenu.this, FirebaseHostClientSelectionActivity.class);
                MainMenu.this.startActivity(mainIntent);

            }
        });
    }

    private void initViews() {
        playBtn = findViewById(R.id.playButton);
        rulesBtn = findViewById(R.id.rulesButton);
        aboutBtn = findViewById(R.id.aboutButton);
        playBotBtn = findViewById(R.id.playBotButton);
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeFullScreen();
    }

    private void makeFullScreen() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

    }
}
