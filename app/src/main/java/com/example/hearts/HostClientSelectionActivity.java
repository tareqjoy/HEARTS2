package com.example.hearts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Button;

public class HostClientSelectionActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_client);

        Button hostBtn=findViewById(R.id.hostBtn), clientBtn=findViewById(R.id.clientBtn);


        hostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(HostClientSelectionActivity.this, Network.class);
                mainIntent.putExtra("mode","host");
                startActivity(mainIntent);
            }
        });

        clientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(HostClientSelectionActivity.this, Network.class);
                mainIntent.putExtra("mode","client");
                startActivity(mainIntent);
            }
        });
    }
}
