package com.example.hearts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class main extends AppCompatActivity {


    private String myUUID;
    private String mode;
    private String codeStr;
    private boolean lockUpdateCards = true;


    private TextView playerNameTextView[] = new TextView[4];
    private TextView playerPointsTextView[] = new TextView[4];
    private ImageView typingImageView[] = new ImageView[4];
    private CardDeckLayout cardDeck;


    private HashMap<String, PlayerReadyClass> players;
    private ArrayList<String> playerOrderList;
    private HashMap<String, Integer> playerIndex = new HashMap<>();


    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = database.getReference();
    final DatabaseReference gameRef = databaseReference.child("game");


    private PlayerPosition currPassDirection;

    private PassCardSelectedCallback writePassedCards = new PassCardSelectedCallback() {
        @Override
        public void onComplete(ArrayList<String> cards) {
            gameRef.child(codeStr).child("selectedCards").child(myUUID).setValue(cards);
        }
    };

    private MovedCardCallback movedCardCallback = new MovedCardCallback() {
        @Override
        public void onComplete(Card card) {
            gameRef.child(codeStr).child("move").child("movedPlayer").setValue(new PlayerCard(myUUID, card));
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);



        mode = getIntent().getStringExtra("mode");
        myUUID = getIntent().getStringExtra("uuid");
        codeStr = getIntent().getStringExtra("gameCode");
        playerOrderList = getIntent().getStringArrayListExtra("playerOrder");

        playerNameTextView[0] = new TextView(this);
        playerNameTextView[1] = findViewById(R.id.player1Name);
        playerNameTextView[2] = findViewById(R.id.player2Name);
        playerNameTextView[3] = findViewById(R.id.player3Name);


        playerPointsTextView[0] = findViewById(R.id.player0score);
        playerPointsTextView[1] = findViewById(R.id.player1score);
        playerPointsTextView[2] = findViewById(R.id.player2score);
        playerPointsTextView[3] = findViewById(R.id.player3score);


        typingImageView[0] = new ImageView(this);
        typingImageView[1] = findViewById(R.id.player1Typing);
        typingImageView[2] = findViewById(R.id.player2Typing);
        typingImageView[3] = findViewById(R.id.player3Typing);

        cardDeck = findViewById(R.id.playerCrads);

        players = new HashMap<>();


        final CardDeck c = new CardDeck();
        cardDeck.setCardDeck(c);
        cardDeck.setPlayerNameTextView(playerNameTextView);


        gameRef.child(codeStr).child("player").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ///TODO: Handle PlayerCard Left Event
                players.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    PlayerReadyClass temp = dataSnapshot1.getValue(PlayerReadyClass.class);
                    players.put(temp.getUUID(), temp);
                }
                for (int i = 0; i < playerOrderList.size(); i++) {
                    if (playerOrderList.get(i).equals(myUUID) && i == 0) {
                        break;
                    } else if (playerOrderList.get(i).equals(myUUID)) {
                        Collections.rotate(playerOrderList, playerOrderList.size() - i);
                        break;
                    }
                }

                for (int i = 0; i < playerOrderList.size(); i++) {
                    playerIndex.put(playerOrderList.get(i), i);
                }
                for (int i = 1; i < playerOrderList.size(); i++) {
                    playerNameTextView[i].setText(players.get(playerOrderList.get(i)).getName());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //c.addCard(new Card(CardType.HEART, CardNumber.cQ));


        gameRef.child(codeStr).child("playerCards").child(myUUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!lockUpdateCards) {
                    lockUpdateCards = true;
                    cardDeck.clear();
                    //  ArrayList<Card> allMyCards=new ArrayList<>();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        String cardStr = dataSnapshot1.getValue(String.class);
                        Card tempCard = new Card(cardStr);
                        c.addCard(tempCard);
                        //  Toast.makeText(main.this,cardStr,Toast.LENGTH_SHORT).show();
                    }
                    cardDeck.updateLayout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        gameRef.child(codeStr).child("gameState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String val = dataSnapshot.getValue(String.class);
                if (val != null) {


                    gameRef.child(codeStr).child("playerCards").child(myUUID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            cardDeck.clear();
                            //  ArrayList<Card> allMyCards=new ArrayList<>();
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                String cardStr = dataSnapshot1.getValue(String.class);
                                Card tempCard = new Card(cardStr);
                                c.addCard(tempCard);
                                //  Toast.makeText(main.this,cardStr,Toast.LENGTH_SHORT).show();
                            }
                            cardDeck.updateLayout();
                            if (val.equals("pl")) {
                                cardDeck.passCardFromMe(writePassedCards, PlayerPosition.LEFT);
                                currPassDirection = PlayerPosition.LEFT;
                            } else if (val.equals("pr")) {
                                cardDeck.passCardFromMe(writePassedCards, PlayerPosition.RIGHT);
                                currPassDirection = PlayerPosition.RIGHT;
                            } else if (val.equals("ps")) {
                                cardDeck.passCardFromMe(writePassedCards, PlayerPosition.STRAIGHT);
                                currPassDirection = PlayerPosition.RIGHT;
                            } else if (val.equals("np")) {
                                cardDeck.donePassCardToMe();
                                //cardDeck.setGameMode(CardDeckLayout.PASS_CARD_FROM_ME);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        gameRef.child(codeStr).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    Toast.makeText(main.this, "Game is cancelled by server", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //getting my cards after passing from another player
        gameRef.child(codeStr).child("passedCards").child(myUUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 3) {

                    ArrayList<Card> cards = new ArrayList<>();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        String str = dataSnapshot1.getValue(String.class);
                        Card c = new Card(str);
                        cards.add(c);
                    }
                    cardDeck.passCardToMe(cards, currPassDirection);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        gameRef.child(codeStr).child("move").child("playerToMove").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    PlayerCard temp = dataSnapshot.getValue(PlayerCard.class);
                    if (temp != null && temp.getPlayer().equals(myUUID)) {
                        cardDeck.newMoveBuffer(GameState.RUNNING, PlayerState.GIVE, PlayerPosition.ME, temp.getCard(), movedCardCallback);
                    } else if (temp != null) {
                        typingImageView[playerIndex.get(temp.getPlayer())].setVisibility(View.VISIBLE);
                        // cardDeck.newMove(GameState.RUNNING, PlayerState.WAIT);
                    }
                } catch (Exception e){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        gameRef.child(codeStr).child("move").child("heartsBroke").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    cardDeck.setHeartsBroken(dataSnapshot.getValue(Boolean.class));
                } catch (Exception e){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        gameRef.child(codeStr).child("move").child("playerPoints").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Long> results = (HashMap<String, Long>) dataSnapshot.getValue();
                if (results != null) {
                    for (HashMap.Entry<String, Long> entry : results.entrySet()) {
                        String key = entry.getKey();
                        Long value = entry.getValue();
                        playerPointsTextView[playerIndex.get(key)].setText(value.toString());
                        // ...
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        gameRef.child(codeStr).child("move").child("playerGot").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    String temp = dataSnapshot.getValue(String.class);
                    if (temp != null) {
                        int op = playerIndex.get(temp);
                        PlayerPosition pp = PlayerPosition.ME;
                        if (op == 0) {
                            pp = PlayerPosition.ME;
                        } else if (op == 1) {
                            pp = PlayerPosition.LEFT;
                        } else if (op == 2) {
                            pp = PlayerPosition.STRAIGHT;
                        } else if (op == 3) {
                            pp = PlayerPosition.RIGHT;
                        }
                        final Handler handler = new Handler();

                        final PlayerPosition finalPp = pp;

                        cardDeck.newMoveBuffer(GameState.RUNNING, PlayerState.GIVE_ALL, finalPp, null, null);
                        //Do something after 100ms


                    } else {
                        // cardDeck.newMove(GameState.RUNNING, PlayerState.WAIT);
                    }
                }catch (Exception e){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        gameRef.child(codeStr).child("move").child("movedPlayer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    PlayerCard p = dataSnapshot.getValue(PlayerCard.class);
                    if (p != null) {
                        if (!p.getPlayer().equals(myUUID)) {
                            for (ImageView img : typingImageView) {
                                img.setVisibility(View.GONE);
                            }
                            int i = playerIndex.get(p.getPlayer());
                            PlayerPosition tempP = PlayerPosition.ME;
                            if (i == 1) {
                                tempP = PlayerPosition.LEFT;
                            } else if (i == 2) {
                                tempP = PlayerPosition.STRAIGHT;
                            } else if (i == 3) {
                                tempP = PlayerPosition.RIGHT;
                            }
                            cardDeck.newMoveBuffer(GameState.RUNNING, PlayerState.WAIT, tempP, p.getCard(), null);
                            //cardDeck.doCardAnimationFromPlayer(i, p.getCard());
                        }
                    }
                }catch (Exception e){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //Toast.makeText(this,String.valueOf(defaultScoreLayoutTextView.getTextSize()),Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        makeFullScreen();
    }

    /*
        private PlayerPosition reversePosition(PlayerPosition playerPosition, HashMap<String, Integer> playerIndex) {
            if (playerPosition == PlayerPosition.LEFT) {
                return PlayerPosition.values()[playerIndex.size() - 1];
            } else if (playerPosition == PlayerPosition.RIGHT) {
                return PlayerPosition.values()[1];
            } else if (playerPosition == PlayerPosition.STRAIGHT && playerIndex.size() % 2 == 0) {
                return PlayerPosition.values()[playerIndex.size() / 2];
            } else {
                return PlayerPosition.ME;
            }
        }
    */
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
