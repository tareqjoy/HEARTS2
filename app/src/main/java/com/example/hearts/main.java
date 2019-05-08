package com.example.hearts;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import static android.view.View.GONE;
import static android.view.View.LAYER_TYPE_HARDWARE;

public class main extends Activity {
    private CardDeck playerCards;
    private ImageView scoreShowImageView, closeScoreView, backgroundImg, onTableCard2, rightPlayerCard, leftPlayerCard, topPlayerCard, onTableCard1, onTableCard3, downPlayerCard, onTableCard4, player1image, player2image, player3image, passFirstCard, passSecondCard, passThirdCard;
    private LinearLayout gameOverMsg, table, heartsBrokenMsg, passCardLinearLayout, scoreLayout;
    private TextView warningTextView, usersTurnTextView, player1score, player2score, player3score, player4score, noPassingMsg, player1TotalScore, player2TotalScore, player3TotalScore, player4TotalScore;
    private Vector<String> userCards = new Vector<>();
    private Vector<String> player1 = new Vector<>();
    private Vector<String> player2 = new Vector<>();
    private Vector<String> player3 = new Vector<>();
    private Vector<String> allCards = new Vector<>();
    private ImageView[] userCardsImage;
    private Button passButton, restartButton;
    private final int BUTTON_ACCEPT = 1, BUTTON_PASS = 2;
    private SoundPool soundPool;
    private ArrayList<Pair<Integer, String>> cardsOnTable = new ArrayList<>();
    private int animationDuration = 0;
    private int round = 0;
    private boolean isUsersTurn = false, isUsersFirstTurn = false, isHeartBroken = false, isPassingCards = true;
    private int userplayed = 0, cardGivenSound = 0, cardTakenSound = 0, heartBreakSound =0;
    private TextView defaultScoreLayoutTextView;

    private TableRow player1History, player2History, player3History, player4History, roundNo;

    private String current_card = "clubs";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //  makeFullScreen();

        animationDuration = getResources().getInteger(R.integer.animationDuration);

        initializeViews();
        makeAnimationUsingHardware();

        generateAllCards();

        randomizeAndLoadCardToPlayers();
        initSoundPool();//N.B. before initUserCardOnClickActions()
        initCloseScoreLayoutOnClickActions();
        initShowScoreLayoutOnClickActions();
        initUserCardsOnClickActions();
        initPassButtonOnClickActions();
        initPassCardsOnClickActions();
        initRestartButtonOnClickActions();

        startGame();

        defaultScoreLayoutTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultScoreLayoutTextView.getTextSize());
        //Toast.makeText(this,String.valueOf(defaultScoreLayoutTextView.getTextSize()),Toast.LENGTH_SHORT).show();
    }

    private void initSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 1);
        }
        cardGivenSound = soundPool.load(this, R.raw.card_given, 1);
        cardTakenSound=soundPool.load(this, R.raw.get_card, 2);
        heartBreakSound =soundPool.load(this,R.raw.heart_break,3);
    }

    private void initRestartButtonOnClickActions() {
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameOverMsg.setVisibility(GONE);
                restartRound();
            }
        });
    }

    private void startGame() {

        startRound();
    }

    private void playSound(int soundID){
        soundPool.play(soundID,1,1,0,0,1);
        //soundPool.release();
    }

    private void initCloseScoreLayoutOnClickActions() {
        closeScoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ImageView img=(ImageView) v;
                scoreLayout.setVisibility(GONE);
            }
        });
    }

    private void initPassCardsOnClickActions() {
        passFirstCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = (ImageView) v;
                if (img.getTag() != null) {
                    playerCards.addCard((String) img.getTag());
                    userCards.add((String) img.getTag());
                    img.setImageResource(0);
                    img.setTag(null);
                }
            }
        });
        passSecondCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = (ImageView) v;
                if (img.getTag() != null) {
                    playerCards.addCard((String) img.getTag());
                    userCards.add((String) img.getTag());
                    img.setImageResource(0);
                    img.setTag(null);
                }
            }
        });
        passThirdCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = (ImageView) v;
                if (img.getTag() != null) {
                    playerCards.addCard((String) img.getTag());
                    userCards.add((String) img.getTag());
                    img.setImageResource(0);
                    img.setTag(null);
                }
            }
        });
    }

    private void initShowScoreLayoutOnClickActions() {
        scoreShowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scoreLayout.setVisibility(View.VISIBLE);

            }
        });
    }


    private void initPassButtonOnClickActions() {
        passButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int state = (int) v.getTag();
                if (state == BUTTON_PASS) {
                    if (!isPassable()) {

                        isPassingCards = false;
                        passCardNow();

                    } else {
                        showWarnig("Select 3 Cards!");
                    }
                } else if (state == BUTTON_ACCEPT) {
                    if (isNoPassingRound()) {
                        passCardLinearLayout.setVisibility(GONE);
                    } else {
                        passCardLinearLayout.setVisibility(GONE);
                        playerCards.addCard(passFirstCard.getTag().toString());
                        playerCards.addCard(passSecondCard.getTag().toString());
                        playerCards.addCard(passThirdCard.getTag().toString());
                    }
                    startRoundNow();
                }
            }
        });
    }

    private boolean isNoPassingRound() {
        if (round % 4 == 0)
            return true;
        else
            return false;
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

    @Override
    protected void onResume() {
        super.onResume();
        makeFullScreen();
    }

    private void initializeViews() {
        playerCards = findViewById(R.id.playerCrads);
        topPlayerCard = findViewById(R.id.topPlayerCard);
        rightPlayerCard = findViewById(R.id.rightPlayerCard);
        leftPlayerCard = findViewById(R.id.leftPlayerCard);
        onTableCard1 = findViewById(R.id.onTableCard1);
        onTableCard2 = findViewById(R.id.onTableCard2);
        onTableCard3 = findViewById(R.id.onTableCard3);
        onTableCard4 = findViewById(R.id.onTableCard4);
        downPlayerCard = findViewById(R.id.downPlayerCard);
        table = findViewById(R.id.table);
        warningTextView = findViewById(R.id.warning_textView);
        usersTurnTextView = findViewById(R.id.users_turn_textView);
        player1score = findViewById(R.id.player1score);
        player2score = findViewById(R.id.player2score);
        player3score = findViewById(R.id.player3score);
        player4score = findViewById(R.id.player4score);
        player1image = findViewById(R.id.player1image);
        player2image = findViewById(R.id.player2image);
        player3image = findViewById(R.id.player3image);
        heartsBrokenMsg = findViewById(R.id.hearts_broken_msg);
        passCardLinearLayout = findViewById(R.id.pass_card_layout);
        passFirstCard = findViewById(R.id.pass_first);
        passSecondCard = findViewById(R.id.pass_second);
        passThirdCard = findViewById(R.id.pass_third);
        passButton = findViewById(R.id.pass_button);
        noPassingMsg = findViewById(R.id.noPassingMsg);
        backgroundImg = findViewById(R.id.backgroundImg);
        scoreLayout = findViewById(R.id.scoreLayout);
        closeScoreView = findViewById(R.id.closeImageView);
        scoreShowImageView = findViewById(R.id.scoreShowImageView);
        player1TotalScore = findViewById(R.id.player1TotalScore);
        player2TotalScore = findViewById(R.id.player2TotalScore);
        player3TotalScore = findViewById(R.id.player3TotalScore);
        player4TotalScore = findViewById(R.id.player4TotalScore);
        roundNo = findViewById(R.id.roundNo);
        player1History = findViewById(R.id.player1History);
        player2History = findViewById(R.id.player2History);
        player3History = findViewById(R.id.player3History);
        player4History = findViewById(R.id.player4History);
        defaultScoreLayoutTextView = findViewById(R.id.defaultTextViewScoreLayout);
        gameOverMsg = findViewById(R.id.gameOverMsg);
        restartButton = findViewById(R.id.restartBtn);
    }


    private void randomizeAndLoadCardToPlayers() {
        Collections.shuffle(allCards);

        select13CardsToVector(userCards);
        select13CardsToVector(player1);
        select13CardsToVector(player2);
        select13CardsToVector(player3);

        playerCards.loadDeck(userCards);
    }

    private void select13CardsToVector(Vector<String> player) {
        Random random = new Random();

        for (int i = 0; i < 13; i++) {

            int randomInt = 0;
            if (allCards.size() > 1)
                randomInt = random.nextInt(allCards.size() - 1);
            player.add(allCards.get(randomInt));
            allCards.remove(randomInt);
        }
    }

    private void passCardNow() {
        String[] fromPlayer1 = new String[3], fromPlayer2 = new String[3], fromPlayer3 = new String[3], fromPlayer4 = new String[3];
        if (round % 4 != 0) {
            fromPlayer1 = chooseCardsToPass(player1);
            fromPlayer2 = chooseCardsToPass(player2);
            fromPlayer3 = chooseCardsToPass(player3); //NB: ordering important
            fromPlayer4 = cardsToPassFromUser();
            if (round % 4 == 1) { //pass clockwise
                addPassedCardsToDeck(player1, fromPlayer4);
                addPassedCardsToDeck(player2, fromPlayer1);
                addPassedCardsToDeck(player3, fromPlayer2);
                addAndShowPassedCardsToUser(fromPlayer3);
            } else if (round % 4 == 2) {
                addPassedCardsToDeck(player1, fromPlayer2);
                addPassedCardsToDeck(player2, fromPlayer3);
                addPassedCardsToDeck(player3, fromPlayer4);
                addAndShowPassedCardsToUser(fromPlayer1);
            } else if (round % 4 == 3) {
                addPassedCardsToDeck(player1, fromPlayer3);
                addPassedCardsToDeck(player2, fromPlayer4);
                addPassedCardsToDeck(player3, fromPlayer1);
                addAndShowPassedCardsToUser(fromPlayer2);
            }

        } else {
            noPassingRound();
        }
    }

    private void noPassingRound() {
        isPassingCards = false;
        noPassingMsg.setVisibility(View.VISIBLE);
        passFirstCard.setVisibility(GONE);
        passSecondCard.setVisibility(GONE);
        passThirdCard.setVisibility(GONE);
    }

    private void addAndShowPassedCardsToUser(String[] str) {
        passFirstCard.setClickable(false);
        passFirstCard.setTag(str[0]);
        passSecondCard.setClickable(false);
        passSecondCard.setTag(str[1]);
        passThirdCard.setClickable(false);
        passThirdCard.setTag(str[2]);
        passButton.setText("Accept");
        passButton.setTag(BUTTON_ACCEPT);
        loadCardToImageView(passFirstCard, str[0]);
        loadCardToImageView(passSecondCard, str[1]);
        loadCardToImageView(passThirdCard, str[2]);

    }

    private void addPassedCardsToDeck(List<String> lst, String[] str) {
        lst.add(str[0]);
        lst.add(str[1]);
        lst.add(str[2]);
    }

    private String[] chooseCardsToPass(List<String> lst) {
        String[] str = new String[3];
        str[0] = chooseHighestCard(lst);
        lst.remove(str[0]);
        str[1] = chooseHighestCard(lst);
        lst.remove(str[1]);
        str[2] = chooseHighestCard(lst);
        lst.remove(str[2]);
        return str;
    }

    private String[] cardsToPassFromUser() {
        String[] str = new String[3];
        str[0] = passFirstCard.getTag().toString();
        str[1] = passSecondCard.getTag().toString();
        str[2] = passThirdCard.getTag().toString();
        return str;
    }

    private void generateAllCards() {
        for (int i = 0; i < 52; i++) {
            StringBuilder str = new StringBuilder();
            int cardId = i / 13;
            if (cardId == 0) {
                str.append("card_clubs_");
            } else if (cardId == 1) {
                str.append("card_diamonds_");
            } else if (cardId == 2) {
                str.append("card_hearts_");
            } else {
                str.append("card_spades_");
            }
            str.append((i % 13) + 2);
            allCards.add(str.toString());
        }
    }

    private void makeAnimationUsingHardware() {
        topPlayerCard.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        rightPlayerCard.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        leftPlayerCard.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        table.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        warningTextView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        backgroundImg.setLayerType(LAYER_TYPE_HARDWARE, null);

    }

    private void initUserCardsOnClickActions() {
        playSound(cardGivenSound);
        userCardsImage = playerCards.getCardsImage().toArray(new ImageView[13]);
        for (int i = 0; i < 13; i++) {
            userCardsImage[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView img = (ImageView) v;
                    if (isPassingCards) {
                        if (isPassable()) {
                            removeCardFromUsersDeck(img);
                            addToPassingCard(img);

                        }
                        return;
                    }
                    if (isUsersTurn) {
                        if (isUsersFirstTurn) {
                            if (!((String) img.getTag()).contains("clubs_2")) {
                                showWarnig("PLAY 2 of CLUBS!");
                                return;
                            }
                        }
                        if (isFirstMove()) { //if first move of USER
                            if (!isHeartBroken && ((String) img.getTag()).contains("hearts") && (playerHasThatCardType(userCards, "clubs") || playerHasThatCardType(userCards, "diamonds") || playerHasThatCardType(userCards, "spades"))) {
                                showWarnig("HEARTS NOT BROKEN!");
                                return;
                            }
                        }
                        if (!isFirstMove()) { //not first move of USER

                            if (!((String) img.getTag()).contains(current_card) && playerHasThatCardType(userCards, current_card)) {
                                showWarnig("PLAY " + current_card.toUpperCase() + "!");
                                return;
                            }
                        }

                        checkIfHeartBreaks((String) img.getTag());
                        playSound(cardGivenSound);
                        isUsersFirstTurn = false;
                        isUsersTurn = false;
                        usersTurnTextView.setVisibility(GONE);
                        removeCardFromUsersDeck(img);
                        if (isFirstMove())
                            current_card = getCardType((String) img.getTag());
                        passCardToTableWithAnimation(onTableCard4, downPlayerCard, (String) img.getTag(), 4);
                    }
                }
            });

        }
    }



    private boolean isPassable() {
        if (!isPassingCards)
            return false;
        else {
            if (passFirstCard.getTag() == null || passSecondCard.getTag() == null || passThirdCard.getTag() == null) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void addToPassingCard(ImageView img) {

        if (passFirstCard.getTag() == null) {
            passFirstCard.setTag(img.getTag());
            passFirstCard.setImageDrawable(img.getDrawable());
        } else if (passSecondCard.getTag() == null) {
            passSecondCard.setTag(img.getTag());
            passSecondCard.setImageDrawable(img.getDrawable());
        } else if (passThirdCard.getTag() == null) {
            passThirdCard.setTag(img.getTag());
            passThirdCard.setImageDrawable(img.getDrawable());
        }
    }

    private void removeCardFromUsersDeck(ImageView img) {
        if (extractImageViewFromCardDeck(0).equals(img)) {
            if (playerCards.getCardContainer().getChildCount() > 1) {
                playerCards.getCardContainer().removeView(playerCards.getCardContainer().getChildAt(0));
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) playerCards.getCardContainer().getChildAt(0).getLayoutParams();
                layoutParams.setMargins(0, 0, 0, 0);
                playerCards.getCardContainer().getChildAt(0).setLayoutParams(layoutParams);
            } else {
                playerCards.getCardContainer().removeView(playerCards.getCardContainer().getChildAt(0));
            }
        } else {
            for (int i = 0; i < playerCards.getCardContainer().getChildCount(); i++) {
                if (extractImageViewFromCardDeck(i).equals(img)) {
                    playerCards.getCardContainer().removeView(playerCards.getCardContainer().getChildAt(i));
                    break;
                }
            }
        }
        userCards.remove(img.getTag());
    }

    private void checkIfHeartBreaks(String givenCard) {
        if (!isHeartBroken && getCardType(givenCard).equals("hearts")) {
            breakHearts();
        }
    }

    private void breakHearts() {
        isHeartBroken = true;
        playSound(heartBreakSound);
        heartsBrokenMsg.setVisibility(View.VISIBLE);
        heartsBrokenMsg.postDelayed(new Runnable() {
            public void run() {
                heartsBrokenMsg.setVisibility(View.GONE);
            }
        }, 1500);
    }

    private String getCardType(String cardFullName) {
        return cardFullName.split("_")[1];
    }

    private ImageView extractImageViewFromCardDeck(int childIndex) {
        return (ImageView) ((LinearLayout) playerCards.getCardContainer().getChildAt(childIndex)).getChildAt(0);
    }

    private boolean playerHasThatCardType(List<String> deck, String type) {
        for (String str : deck) {
            if (str.contains(type)) {
                return true;
            }
        }
        return false;
    }

    private void showWarnig(String str) {

        warningTextView.setText(str);
        warningTextView.clearAnimation();


        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_down_to_up);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

                warningTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                warningTextView.postDelayed(new Runnable() {
                    public void run() {
                        warningTextView.setVisibility(View.GONE);
                    }
                }, 1500);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        if (warningTextView.getVisibility() != View.VISIBLE)
            warningTextView.startAnimation(animation);

        //warningTextView.setVisibility(GONE);
    }

    private void startRound() {
        round++;
        current_card = "clubs";
        isUsersFirstTurn = false;
        isUsersTurn = false;
        isHeartBroken = false;
        isPassingCards = true;
        userplayed = 0;
        startPassingCardSession();
    }

    private void startPassingCardSession() {
        passFirstCard.setImageResource(0);
        passFirstCard.setVisibility(View.VISIBLE);
        passSecondCard.setImageResource(0);
        passSecondCard.setVisibility(View.VISIBLE);
        passThirdCard.setImageResource(0);
        passThirdCard.setVisibility(View.VISIBLE);
        noPassingMsg.setVisibility(GONE);
        passThirdCard.setClickable(true);
        passThirdCard.setTag(null);
        passFirstCard.setClickable(true);
        passFirstCard.setTag(null);
        passSecondCard.setClickable(true);
        passSecondCard.setTag(null);
        passButton.setTag(BUTTON_PASS);
        passCardLinearLayout.setVisibility(View.VISIBLE);
        if (round % 4 == 1) {
            passButton.setText("Pass Left");
        } else if (round % 4 == 2) {
            passButton.setText("Pass Right");
        } else if (round % 4 == 3) {
            passButton.setText("Pass Straight");
        } else {
            noPassingRound();
            passButton.setTag(BUTTON_ACCEPT);
            passButton.setText("OK");
        }
    }

    private void startRoundNow() {
        if (player1.contains("card_clubs_2")) {
            passCardPlayer1("card_clubs_2");
        } else if (player2.contains("card_clubs_2")) {
            passCardPlayer2("card_clubs_2");
        } else if (player3.contains("card_clubs_2")) {
            passCardPlayer3("card_clubs_2");
        } else {
            isUsersFirstTurn = true;
            usersTurn();
        }
    }

    private void makeNextAction(final int currentPlayer) {
        userplayed++;

        if (userplayed == 4) { //4 cards on table

            Pair<Integer, Pair<Integer, List<Integer>>> val = chooseWhoGetsTheCards();
            int playerGets = val.first;
            int point = val.second.first;
            List<Integer> whoGavePoints = val.second.second;

            animateAndTransformTableCardsToPlayer(getPlayerImageViewByID(playerGets), playerGets, point, whoGavePoints);
            userplayed = 0;

        } else {
            if (round % 2 == 1) { //clockwise
                if (currentPlayer == 1)
                    choosePlayer(2);
                else if (currentPlayer == 2)
                    choosePlayer(3);
                else if (currentPlayer == 3)
                    usersTurn();

                else
                    choosePlayer(1);
            } else {   //reverse clock wise
                if (currentPlayer == 1)
                    usersTurn();
                else if (currentPlayer == 2)
                    choosePlayer(1);
                else if (currentPlayer == 3)
                    choosePlayer(2);

                else
                    choosePlayer(3);
            }
        }
    }

    private void usersTurn() {
        isUsersTurn = true;
        usersTurnTextView.setVisibility(View.VISIBLE);
    }

    private ImageView getPlayerImageViewByID(int id) {
        if (id == 1)
            return leftPlayerCard;
        else if (id == 2)
            return topPlayerCard;
        else if (id == 3)
            return rightPlayerCard;
        else
            return downPlayerCard;
    }

    private TextView getPlayerScoreTextViewByID(int id) {
        if (id == 1)
            return player1score;
        else if (id == 2)
            return player2score;
        else if (id == 3)
            return player3score;
        else
            return player4score;
    }

    private ImageView getPlayerPictureImageViewByID(int id) {
        if (id == 1)
            return player1image;
        else if (id == 2)
            return player2image;
        else if (id == 3)
            return player3image;
        else
            return null;
    }

    private Pair<Integer, Pair<Integer, List<Integer>>> chooseWhoGetsTheCards() {
        List<Pair<Integer, String>> temp = new ArrayList<>();
        List<Integer> whoGavePoints = new ArrayList<>();
        int point = 0;
        for (Pair<Integer, String> p : cardsOnTable) {
            if (p.second.contains("hearts")) {
                whoGavePoints.add(p.first);
                point++;
            } else if (p.second.contains("spades_12")) {
                whoGavePoints.add(p.first);
                point += 13;
            }
            if (p.second.contains(current_card)) {
                temp.add(new Pair(p.first, p.second));
            }
        }
        cardsOnTable.clear();
        Collections.sort(temp, new Comparator<Pair<Integer, String>>() {
            @Override
            public int compare(Pair<Integer, String> o1, Pair<Integer, String> o2) {
                String[] s1 = o1.second.split("_");
                String[] s2 = o2.second.split("_");

                return Integer.valueOf(s1[2]).compareTo(Integer.valueOf(s2[2]));
            }

        });
        current_card = "";
        return new Pair(temp.get(temp.size() - 1).first, new Pair(point, whoGavePoints));
    }

    private void animateAndTransformTableCardsToPlayer(View player, final int current, final int points, final List<Integer> whoGavePoints) {
        int[] locDesti = new int[2];
        int[] locSource = new int[2];
        player.getLocationOnScreen(locDesti);
        table.getLocationOnScreen(locSource);
        playSound(cardTakenSound);
        Animation animate = new TranslateAnimation(0, locDesti[0] - locSource[0], 0, locDesti[1] - locSource[1]);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                table.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                for (Integer i : whoGavePoints) {
                    if (i != 4) {
                        getPlayerPictureImageViewByID(i).setImageResource(getPlayerHappyReactionResID(i));
                        final int por = i;
                        getPlayerPictureImageViewByID(i).postDelayed(new Runnable() {
                            public void run() {
                                getPlayerPictureImageViewByID(por).setImageResource(getPlayerNormalReactionResID(por));
                            }
                        }, 2000);
                    }
                }
                if (current != 4 && points != 0) {
                    getPlayerPictureImageViewByID(current).setImageResource(getPlayerAngryReactionResID(current));
                    getPlayerPictureImageViewByID(current).postDelayed(new Runnable() {
                        public void run() {
                            getPlayerPictureImageViewByID(current).setImageResource(getPlayerNormalReactionResID(current));
                        }
                    }, 2000);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                table.clearAnimation();
                table.setLayerType(View.LAYER_TYPE_NONE, null);
                clearTable();
                int currPoint = Integer.parseInt(getPlayerScoreTextViewByID(current).getText().toString());
                currPoint += points;
                getPlayerScoreTextViewByID(current).setText(String.valueOf(currPoint));
                if (isRoundEnds()) {
                    roundEndActions();

                } else {
                    choosePlayer(current);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animate.setInterpolator(new DecelerateInterpolator());
        animate.setDuration(animationDuration);
        table.startAnimation(animate);
    }

    private void roundEndActions() {

        int tot1 = Integer.parseInt(player1TotalScore.getText().toString());
        int tot2 = Integer.parseInt(player2TotalScore.getText().toString());
        int tot3 = Integer.parseInt(player3TotalScore.getText().toString());
        int tot4 = Integer.parseInt(player4TotalScore.getText().toString());

        int curr1 = Integer.parseInt(getPlayerScoreTextViewByID(1).getText().toString());
        int curr2 = Integer.parseInt(getPlayerScoreTextViewByID(2).getText().toString());
        int curr3 = Integer.parseInt(getPlayerScoreTextViewByID(3).getText().toString());
        int curr4 = Integer.parseInt(getPlayerScoreTextViewByID(4).getText().toString());

        updateScoreLayout(tot1, tot2, tot3, tot4, curr1, curr2, curr3, curr4);
        ;

        //N.B.ordering important
        getPlayerScoreTextViewByID(1).setText(String.valueOf(0)); //reset score to 0
        getPlayerScoreTextViewByID(2).setText(String.valueOf(0));
        getPlayerScoreTextViewByID(3).setText(String.valueOf(0));
        getPlayerScoreTextViewByID(4).setText(String.valueOf(0));

        if (isGameFinished(tot1 + curr1, tot2 + curr2, tot3 + curr3, tot4 + curr4)) {
            gameOverMsg.setVisibility(View.VISIBLE);
        } else {
            restartRound();
        }
    }

    private boolean isGameFinished(int t1, int t2, int t3, int t4) {
        int maxScore = getResources().getInteger(R.integer.max_score);
        if (t1 >= maxScore) {
            return true;
        } else if (t2 >= maxScore) {
            return true;
        } else if (t3 >= maxScore) {
            return true;
        } else if (t4 >= maxScore) {
            return true;
        }
        return false;
    }

    private void updateScoreLayout(int tot1, int tot2, int tot3, int tot4, int curr1, int curr2, int curr3, int curr4) {


        player1TotalScore.setText(String.valueOf(tot1 + curr1));
        player2TotalScore.setText(String.valueOf(tot2 + curr2));
        player3TotalScore.setText(String.valueOf(tot3 + curr3));
        player4TotalScore.setText(String.valueOf(tot4 + curr4));


        addColumnToScoreLayout(curr1, curr2, curr3, curr4);
    }

    private void addColumnToScoreLayout(int p1, int p2, int p3, int p4) {
        TextView caption = copyTextView(defaultScoreLayoutTextView);
        TextView p1s = copyTextView(defaultScoreLayoutTextView);
        TextView p2s = copyTextView(defaultScoreLayoutTextView);
        TextView p3s = copyTextView(defaultScoreLayoutTextView);
        TextView p4s = copyTextView(defaultScoreLayoutTextView);

        caption.setText("Round " + (round - 1));
        p1s.setText(String.valueOf(p1));
        p2s.setText(String.valueOf(p2));
        p3s.setText(String.valueOf(p3));
        p4s.setText(String.valueOf(p4));

        roundNo.addView(caption);
        player1History.addView(p1s);
        player2History.addView(p2s);
        player3History.addView(p3s);
        player4History.addView(p4s);

    }

    private TextView copyTextView(TextView tv) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(tv.getLayoutParams());
        textView.setTextColor(tv.getTextColors());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.getTextSize());
        textView.setPadding(tv.getPaddingLeft(), tv.getPaddingTop(), tv.getPaddingRight(), tv.getPaddingBottom());
        return textView;
    }

    private void restartRound() {
        playerCards.reloadCards();
        distributeCards();
        startRound();
    }

    private void distributeCards() {
        generateAllCards();
        randomizeAndLoadCardToPlayers();

    }

    private boolean isRoundEnds() {
        return player1.size() == 0 && player2.size() == 0 && player3.size() == 0 && userCards.size() == 0;
    }

    private int getPlayerHappyReactionResID(int id) {
        if (id == 1)
            return R.drawable.player1happy;
        else if (id == 2)
            return R.drawable.player2happy;
        else if (id == 3)
            return R.drawable.player3happy;
        else
            return -1;
    }

    private int getPlayerNormalReactionResID(int id) {
        if (id == 1)
            return R.drawable.player1normal;
        else if (id == 2)
            return R.drawable.player2normal;
        else if (id == 3)
            return R.drawable.player3normal;
        else
            return -1;
    }

    private int getPlayerAngryReactionResID(int id) {
        if (id == 1)
            return R.drawable.player1angry;
        else if (id == 2)
            return R.drawable.player2angry;
        else if (id == 3)
            return R.drawable.player3angry;
        else
            return -1;
    }

    private void choosePlayer(int player) {
        if (player == 1)
            play1();
        else if (player == 2)
            play2();
        else if (player == 3)
            play3();
        else
            usersTurn();

    }

    private void clearTable() {
        onTableCard1.setImageResource(0);
        onTableCard2.setImageResource(0);
        onTableCard3.setImageResource(0);
        onTableCard4.setImageResource(0);
    }

    private void passCardPlayer1(final String card) {
        passCardToTableWithAnimation(onTableCard1, leftPlayerCard, card, 1);
        player1.remove(card);
    }

    private void passCardToTableWithAnimation(final ImageView onTableCardImageView, final ImageView playerCardOnHandImageView, final String card, final int current) {
        int[] locDesti = new int[2];
        int[] locSource = new int[2];

        playerCardOnHandImageView.clearAnimation();

        onTableCardImageView.getLocationOnScreen(locDesti);
        playerCardOnHandImageView.getLocationOnScreen(locSource);

        Animation animate = new TranslateAnimation(0, locDesti[0] - locSource[0], 0, locDesti[1] - locSource[1]);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                playSound(cardGivenSound);
                playerCardOnHandImageView.setLayerType(LAYER_TYPE_HARDWARE, null);
                onTableCardImageView.setVisibility(View.INVISIBLE);
                loadCardToImageView(onTableCardImageView, card);
                loadCardToImageView(playerCardOnHandImageView, card);
                playerCardOnHandImageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                playerCardOnHandImageView.setLayerType(LAYER_TYPE_HARDWARE, null);
                onTableCardImageView.setVisibility(View.VISIBLE);
                playerCardOnHandImageView.setVisibility(View.INVISIBLE);
                playerCardOnHandImageView.clearAnimation();
                cardsOnTable.add(new Pair(current, card));
                makeNextAction(current);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {


            }
        });
        animate.setInterpolator(new DecelerateInterpolator());
        animate.setDuration(animationDuration);

        playerCardOnHandImageView.startAnimation(animate);
    }

    private void loadCardToImageView(ImageView img, String card) {
        img.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("@drawable/" + card, null, getPackageName())));
    }

    private void passCardPlayer2(final String card) { //top player
        passCardToTableWithAnimation(onTableCard2, topPlayerCard, card, 2);

        player2.remove(card);
    }

    private void passCardPlayer3(final String card) {
        passCardToTableWithAnimation(onTableCard3, rightPlayerCard, card, 3);


        player3.remove(card);
    }

    private void play1() {
        passCardPlayer1(selectCardFromDeck(player1));

    }

    private String selectCardFromDeck(List<String> deck) {

        if (deck.size() == 0) //a round ends
            return "";
        if (isFirstMove()) { //first move of AI
            String str = chooseLowestCard(deck);
            current_card = getCardType(str);
            checkIfHeartBreaks(str);
            return str;
        }
        List<String> avail = new ArrayList<>();
        for (int i = 0; i < deck.size(); i++) {
            if (deck.get(i).contains(current_card)) {
                avail.add(deck.get(i));
            }
        }
        if (avail.size() != 0) {
            String str = chooseHighestCard(avail);
            checkIfHeartBreaks(str);
            return str;
        } else {
            String str = chooseHighestCard(deck);
            checkIfHeartBreaks(str);
            return str;
        }
    }

    private boolean isFirstMove() {
        return current_card.equals("");
    }

    private String chooseHighestCard(List<String> lst) {
        List<String> temp = chooseAvailableValidCardsToMove(lst);
        temp = sortCards(temp);
        Collections.reverse(temp);
        return lst.get(0);
    }


    private String chooseLowestCard(List<String> lst) {
        List<String> temp = chooseAvailableValidCardsToMove(lst);
        temp = sortCards(temp);

        return temp.get(0);
    }

    private List<String> chooseAvailableValidCardsToMove(List<String> lst) {
        List<String> temp = new ArrayList<>();
        if (isFirstMove()) {
            if (!isHeartBroken) {
                for (String str : lst) {
                    if (!str.contains("hearts")) { //hearts card can't be given
                        temp.add(str);
                    }
                }
            }
            if (temp.size() == 0 || isHeartBroken) { //all cards are hearts or hearts is broken
                temp.addAll(lst);
            }
        } else {
            for (String str : lst) {
                if (str.contains(current_card)) { //filter to only current type cards
                    temp.add(str);
                }
            }
            if (temp.size() == 0) { //no current type cards
                temp.addAll(lst);
            }
        }
        return temp;
    }

    private List<String> sortCards(List<String> lst) {
        Collections.sort(lst, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {

                String[] s1 = o1.split("_");
                String[] s2 = o2.split("_");

                return Integer.valueOf(s1[2]).compareTo(Integer.valueOf(s2[2]));

            }
        });
        return lst;
    }

    private void play2() {
        passCardPlayer2(selectCardFromDeck(player2));
    }

    private void play3() {
        passCardPlayer3(selectCardFromDeck(player3));
    }
}
