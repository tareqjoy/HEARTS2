package com.example.hearts;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FirebaseNetworkActivity extends AppCompatActivity {
    private EditText gameCodeEditText;
    private TextView gameCodeTextView;
    private Button readyButton;
    private String mode, userId;
    private final String myUUID = UUID.randomUUID().toString();
    private String codeStr;


    private PlayerReadyAdapter adapter;
    private ListView playersListView;
    private List<PlayerReadyClass> playerStatusList;
    ;
    public static final String READY = "READY", CANCEL_READY = "CANCEL READY", CONNECT = "CONNECT", HOST = "host", CLIENT = "client";


    private Boolean gameStarted = false;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = database.getReference();
    final DatabaseReference gameRef = databaseReference.child("game");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_firebase_network);
        makeFullScreen();
        gameCodeEditText = findViewById(R.id.gameCodeEditText);
        gameCodeTextView = findViewById(R.id.gameCodeTextView);
        readyButton = findViewById(R.id.readyButton);
        playersListView = findViewById(R.id.clientsListView);
        playerStatusList = new ArrayList<>();

        adapter = new PlayerReadyAdapter(FirebaseNetworkActivity.this, playerStatusList);
        playersListView.setAdapter(adapter);


        mode = getIntent().getStringExtra("mode");
        userId = getIntent().getStringExtra("user_name");


        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currStateStr = readyButton.getText().toString();

                if (currStateStr.equals(READY)) {
                    readyButton.setEnabled(false);
                    gameRef.child(codeStr).child("player").child(myUUID).setValue(new PlayerReadyClass(myUUID, userId, true)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            readyButton.setEnabled(true);
                            readyButton.setText(CANCEL_READY);

                        }
                    });
                } else if (currStateStr.equals(CANCEL_READY)) {
                    readyButton.setEnabled(false);
                    gameRef.child(codeStr).child("player").child(myUUID).setValue(new PlayerReadyClass(myUUID, userId, false)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            readyButton.setEnabled(true);
                            readyButton.setText(READY);

                        }
                    });
                } else if (currStateStr.equals(CONNECT)) {

                    String str = gameCodeEditText.getText().toString();
                    str = str.trim().replaceAll("\\s+", " ");
                    if (str.trim().isEmpty()) {
                        Toast.makeText(FirebaseNetworkActivity.this, "Invalid code, check again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    codeStr = str.toUpperCase();

                    readyButton.setEnabled(false);
                    gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(codeStr)) {
                                gameRef.child(codeStr).child("player").child(myUUID).setValue(new PlayerReadyClass(myUUID, userId, false)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        readyButton.setEnabled(true);
                                        readyButton.setText(READY);
                                        gameCodeEditText.setEnabled(false);
                                        listenForPlayerUpdates();
                                    }
                                });
                            } else {
                                Toast.makeText(FirebaseNetworkActivity.this, "No game found for this code, Try again with new again.", Toast.LENGTH_SHORT).show();
                                readyButton.setEnabled(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            Toast.makeText(FirebaseNetworkActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();


                        }
                    });

                }
            }
        });

        if (mode.equals(HOST)) {
            gameCodeEditText.setVisibility(View.GONE);
            gameCodeTextView.setVisibility(View.VISIBLE);
            readyButton.setVisibility(View.GONE);


            gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    codeStr = getAlphaNumericString(6);
                    if (dataSnapshot.hasChild(codeStr)) {
                        Toast.makeText(FirebaseNetworkActivity.this, "Same code exist, start game again", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        gameRef.child(codeStr).child("player").child(myUUID).setValue(new PlayerReadyClass(myUUID, userId, false)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                gameCodeTextView.setText(codeStr);
                                gameCodeTextView.setVisibility(View.VISIBLE);
                                readyButton.setVisibility(View.VISIBLE);
                                readyButton.setText(READY);

                                listenForPlayerUpdates();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(FirebaseNetworkActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else { //client is selected
            gameCodeTextView.setVisibility(View.GONE);
            gameCodeEditText.setVisibility(View.VISIBLE);
            readyButton.setVisibility(View.VISIBLE);
            readyButton.setText(CONNECT);
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!gameStarted && codeStr != null && !codeStr.isEmpty()) {
            gameRef.child(codeStr).child("player").child(myUUID).removeValue();
        }
    }

    private void listenForPlayerUpdates() {
        gameRef.child(codeStr).child("player").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int readyCount = 0;

                playerStatusList.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    PlayerReadyClass temp = dataSnapshot1.getValue(PlayerReadyClass.class);
                    if (temp.getReady()) {
                        readyCount++;
                    }
                    playerStatusList.add(temp);
                }
                adapter.notifyDataSetChanged();
                if (readyCount == 4) {


                    gameRef.child(codeStr).child("playerOrder").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildrenCount()==4) {
                                ArrayList<String> playerOrder=new ArrayList<>();
                                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                                    playerOrder.add(dataSnapshot1.getValue(String.class));
                                }


                                gameRef.child(codeStr).child("playerOrder").removeEventListener(this);
                                gameStarted = true;
                                Intent mainIntent = new Intent(FirebaseNetworkActivity.this, main.class);
                                mainIntent.putExtra("mode", mode);
                                mainIntent.putExtra("uuid", myUUID);
                                mainIntent.putExtra("gameCode", codeStr);
                                mainIntent.putExtra("playerOrder", playerOrder);
                                //mainIntent.putExtra("playerClass", playerStatusList);
                                startActivity(mainIntent);
                                finish();


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    ///TODO : NEXT ACTIVITY
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
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
