package com.example.hearts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FirebaseHostClientSelectionActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_host_client);
        makeFullScreen();
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        Button hostBtn = findViewById(R.id.hostBtn), clientBtn = findViewById(R.id.clientBtn);
        final EditText editText = findViewById(R.id.nameEditText);


        final String userId = sharedPref.getString("user_name", "");
        editText.setText(userId);

        hostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = editText.getText().toString();
                str = str.trim().replaceAll("\\s+", " ");
                if (str.trim().isEmpty()) {
                    Toast.makeText(FirebaseHostClientSelectionActivity.this, "Invalid name was given, check again.", Toast.LENGTH_SHORT).show();
                    editText.setText(userId);
                    return;
                }

                // save your string in SharedPreferences
                sharedPref.edit().putString("user_name", str).apply();

                Intent mainIntent = new Intent(FirebaseHostClientSelectionActivity.this, FirebaseNetworkActivity.class);
                mainIntent.putExtra("mode", "host");
                mainIntent.putExtra("user_name", str);
                startActivity(mainIntent);
            }
        });

        clientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = editText.getText().toString();
                str = str.trim().replaceAll("\\s+", " ");
                if (str.trim().isEmpty()) {
                    Toast.makeText(FirebaseHostClientSelectionActivity.this, "Invalid name was given, check again.", Toast.LENGTH_SHORT).show();
                    editText.setText(userId);
                    return;
                }
                sharedPref.edit().putString("user_name", str).apply();
                Intent mainIntent = new Intent(FirebaseHostClientSelectionActivity.this, FirebaseNetworkActivity.class);
                mainIntent.putExtra("mode", "client");
                mainIntent.putExtra("user_name", str);
                startActivity(mainIntent);
            }
        });
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
