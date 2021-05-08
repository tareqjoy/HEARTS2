package com.example.hearts;

import android.content.Context;
import android.graphics.drawable.Drawable;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class CardDeckLayout extends RelativeLayout {


    private GameState gameState;
    private PlayerPosition playerPosition;
    private PlayerState playerState;
    private SystemState systemState;


    private PassCardSelectedCallback passCardSelectedCallback;
    private MovedCardCallback movedCardCallback;


    private final double imgW = pxFromDp(60.0), imgH = pxFromDp(75.0);
    private int cardAddedToMe = 0;
    private final double totalAng = 60.0, totalW = pxFromDp(700);
    private boolean myDeckLocked = false;
    private final long animationTime = 250;
    private boolean heartsBroken = false;
    private int[] sounds;
    //  private final double totalW=

    private Queue<GameInstanceVariable> gameInstance = new LinkedList<>();

    private LinearLayout cardContainer, passCardContainer, passLayout, messageLayout;
    private RelativeLayout rootLayout;
    private Button passButton;
    private TextView messageTextView;
    private CardDeck cardDeck;
    private ImageView cardOnTableImageView[] = new ImageView[4];

    //    private ImageView playerTypingImageView[];
    private TextView[] playerNameTextView;

    private SoundPool soundPool;
    // private FirebaseActivty writePassedCards;
    private Card cardToMove;

    OnClickListener deckImgClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!myDeckLocked) {


                final ImageView img1 = (ImageView) v;
                final Card selectedCard = (Card) img1.getTag();
                if (gameState == GameState.PASS && playerState == PlayerState.GIVE) {
                    soundPool.play(sounds[0], 1,1,1,0,1f);
                    final ImageView img2 = getAvailableSpaceInPass();

                    Animation.AnimationListener listener = new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            myDeckLocked = true;
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            img2.setImageDrawable(img1.getDrawable());
                            img2.setTag(img1.getTag());
                            cardDeck.removeCard((Card) img1.getTag());
                            updateLayout();
                            myDeckLocked = false;

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    };

                    if (img2 != null) {
                        animateTo(img1, img2, listener);
                    }
                } else if (gameState == GameState.RUNNING && playerPosition == PlayerPosition.ME && playerState == PlayerState.GIVE) {

                    if (cardOnTableImageView[0].getDrawable() == null) {
                        if (img1.getColorFilter() == null) { //null means this card can be playable
                            soundPool.play(sounds[0], 1,1,1,0,1f);
                            movedCardCallback.onComplete((Card) img1.getTag());
                            Animation.AnimationListener listener = new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    myDeckLocked = true;
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    cardOnTableImageView[0].setImageDrawable(img1.getDrawable());
                                    cardOnTableImageView[0].setTag(img1.getTag());

                                    cardDeck.removeCard(selectedCard);
                                    updateLayout();
                                    doneNewMove();
                                    myDeckLocked = false;
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            };
                            animateTo(img1, cardOnTableImageView[0], listener);
                        }
                    }
                }
            }
        }

    };

    OnClickListener passImgClick = new OnClickListener() {
        @Override
        public void onClick(View v) { //cards on passCardDeck, pass those to Me
            final ImageView img1 = (ImageView) v;
            if (gameState == GameState.PASS && playerState == PlayerState.GIVE && img1.getDrawable() != null) {
                soundPool.play(sounds[0], 1,1,1,0,1f);
                final ImageView img2 = getAvailableSpaceInDeck((Card) img1.getTag());
                Animation.AnimationListener listener = new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        cardDeck.addCard((Card) img1.getTag());
                        img1.setImageDrawable(null);
                        img1.setTag(null);
                        updateLayout();

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                };

                if (img2 != null) {
                    animateTo(img1, img2, listener);
                }
            } else if (gameState == GameState.PASS && playerState == PlayerState.TAKE && img1.getDrawable() != null) {
                final ImageView img2 = getAvailableSpaceInDeck((Card) img1.getTag());
                soundPool.play(sounds[1], 1,1,1,0,1f);
                Animation.AnimationListener listener = new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        cardDeck.addCard((Card) img1.getTag());
                        img1.setImageDrawable(null);
                        img1.setTag(null);
                        updateLayout();
                        cardAddedToMe++;
                        if (cardAddedToMe == 3) {
                            cardAddedToMe = 0;
                            final Handler handler = new Handler();
                            hidePassLayout();
                            setMessage("GAME STARTED");
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    donePassCardToMe();
                                    //Do something after 100ms
                                }
                            }, 2500);
                        }
                        donePassCardToMe();
                        /*
                        if (gameMode == PASS_CARD_TO_ME || gameMode == GAME_SESSION_ME || gameMode == GAME_SESSION_OTHER) {
                            passLayout.setVisibility(GONE);
                            if (gameMode == GAME_SESSION_ME) {

                            } else if (gameMode == GAME_SESSION_OTHER) {
                                setMessage("GAME STARTED");
                                //  setTemporaryMessage("Game Started", 2000);
                            }


                        }*/
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                };

                if (img2 != null) {
                    animateTo(img1, img2, listener);
                }
            }
        }
    };


    public void passCardFromMe(PassCardSelectedCallback callback, PlayerPosition direction) {
        systemState = SystemState.BUSY;
        gameState = GameState.PASS;
        playerState = PlayerState.GIVE;
        this.playerPosition = direction;
        setButtonText("PASS " + direction.name());
        removeMessage();
        showPassLayout();
        this.passCardSelectedCallback = callback;
    }


    private void donePassCardFromMe() {

        playerState = PlayerState.WAIT;
        // gameState = GameState.PASS;
        hidePassLayout();
        setMessage("Waiting for other player to pass");
        systemState = SystemState.OK;

    }

    public void passCardToMe(ArrayList<Card> cards, PlayerPosition direction) {
        systemState = SystemState.BUSY;
        playerState = PlayerState.TAKE;
        //gameState = GameState.PASS
        setButtonText("ACCEPT");
        removeMessage();
        showPassLayout();
        doPassToMe(cards, direction);

    }


    public void donePassCardToMe() {
        removeMessage();
        systemState = SystemState.OK;
        if (!gameInstance.isEmpty()) { //data already have read from server before the animation end
            GameInstanceVariable game = gameInstance.poll();
            newMoveNow(game.getGameState(), game.getPlayerState(), game.getPlayerPosition(), game.getCard());
        } else { //

        }

    }


    //for other's turn
    public void newMoveBuffer(GameState gameState, PlayerState playerState, PlayerPosition position, Card movedCard, MovedCardCallback cardCallback) {
        Log.i("qerat - newMoveBuffer", "called with (" + gameState.name() + ", " + playerState + ", " + position.name() + ", " + movedCard + ", ...)");
        this.movedCardCallback = cardCallback;
        this.cardToMove = movedCard;
        if (systemState == SystemState.BUSY) {
            Log.i("qerat - newMoveBuffer", "System BUSY");

            gameInstance.add(new GameInstanceVariable(gameState, playerState, movedCard, position));
            Log.i("qerat - newMoveBuffer", "pushed to queue");
        } else if (systemState == SystemState.OK) {
            Log.i("qerat - newMoveBuffer", "System OK");
            newMoveNow(gameState, playerState, position, movedCard);
            ///TODO:
        }
    }

    private void doneNewMove() {
        Log.i("qerat - doneNewMove", "newMove() is done");

        removeMessage();
        doClearTint();
        systemState = SystemState.OK;
        if (!gameInstance.isEmpty()) { //data already have read from server before the animation end
            GameInstanceVariable game = gameInstance.poll();
            Log.i("qerat - doneNewMove", "data in queue, calling newMoveNow(" + game.getGameState() + ", " + game.getPlayerState() + ", " + game.getPlayerPosition() + ", " + game.getCard() + ")");
            newMoveNow(game.getGameState(), game.getPlayerState(), game.getPlayerPosition(), game.getCard());
        } else { //

        }

    }

    private void newMoveNow(GameState gameState, PlayerState playerState, PlayerPosition playerPosition, Card card) {
        systemState = SystemState.BUSY;
        Log.i("qerat - newMoveNow", "called with (" + gameState + ", " + playerState + ", " + playerPosition + ", " + card + ")");

        this.gameState = gameState;
        this.playerState = playerState;
        this.playerPosition = playerPosition;
        if (gameState == GameState.RUNNING && playerPosition == PlayerPosition.ME && playerState == PlayerState.GIVE) {
            //give card to table
            setMessage("YOUR MOVE");
            doMakeTint(card);
        } else if (gameState == GameState.RUNNING && playerPosition == PlayerPosition.ME && playerState == PlayerState.TAKE) {
            //getting all card from the table
            soundPool.play(sounds[1], 1,1,1,0,1f);
        } else if (gameState == GameState.RUNNING && playerState == PlayerState.WAIT) {
            //other player gave card to table
            soundPool.play(sounds[0], 1,1,1,0,1f);
            doMoveCard(playerPosition, card);
        } else if (gameState == GameState.RUNNING && playerState == PlayerState.GIVE_ALL) {
            soundPool.play(sounds[1], 1,1,1,0,1f);
            doGetAllCard(playerPosition);
        }

    }

    public void setHeartsBroken(boolean heartsBroken) {
        this.heartsBroken = heartsBroken;
        if(heartsBroken){
            soundPool.play(sounds[2], 1,1,1,0,1f);
        }
    }

    public CardDeckLayout(Context context) {
        super(context);
        init();
    }

    private void setButtonText(String str) {
        passButton.setText(str);
    }

    public CardDeckLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public void setPlayerNameTextView(TextView[] playerNameTextView) {
        this.playerNameTextView = playerNameTextView;
    }


    private void init() {
        inflate(getContext(), R.layout.cards_layout, this);

        cardContainer = findViewById(R.id.cardContainer);
        passCardContainer = findViewById(R.id.passCardContainer);
        rootLayout = findViewById(R.id.rootLayout);
        passButton = findViewById(R.id.passButton);
        passLayout = findViewById(R.id.passLayout);
        messageLayout = findViewById(R.id.messageLayout);
        messageTextView = findViewById(R.id.messageTextView);
        cardOnTableImageView[0] = findViewById(R.id.onTableCard0);
        cardOnTableImageView[1] = findViewById(R.id.onTableCard1);
        cardOnTableImageView[2] = findViewById(R.id.onTableCard2);
        cardOnTableImageView[3] = findViewById(R.id.onTableCard3);


        passButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameState == GameState.PASS && playerState == PlayerState.GIVE) {
                    soundPool.play(sounds[1], 1,1,1,0,1f);
                    ArrayList<String> cards = getSelectedCardsForPass();
                    if (cards != null && cards.size() == 3) {
                        passCardSelectedCallback.onComplete(cards);
                        doPassFromMe(playerPosition);

                    }
                } else if (gameState == GameState.PASS && playerState == PlayerState.TAKE) {
                    soundPool.play(sounds[1], 1,1,1,0,1f);
                    for (int i = 0; i < passCardContainer.getChildCount(); i++) {
                        ImageView img = (ImageView) passCardContainer.getChildAt(i);
                        img.callOnClick();
                    }
                }
            }
        });

        updateLayout();

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool=new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();

        sounds=new int[3];
        sounds[0]=soundPool.load(getContext(),R.raw.card_given,1);
        sounds[1]=soundPool.load(getContext(),R.raw.get_card,1);
        sounds[2]=soundPool.load(getContext(),R.raw.heart_break,1);
    }

/*    public void setMyMove(Card c) {
        setGameMode(GAME_SESSION_ME);
        cardToMove = c;

    }*/


    private void showPassLayout() {
        passLayout.setVisibility(VISIBLE);

    }

    private void showPassLayout(ArrayList<Card> cards) {
        if (cards != null && cards.size() == 3) {
            passLayout.setVisibility(VISIBLE);
        }

    }


    private void hidePassLayout() {
        passLayout.setVisibility(GONE);
    }

    public void setCardDeck(CardDeck cardDeck) {
        this.cardDeck = cardDeck;
        // init();
    }


    private void setMessage(String str) {
        messageLayout.setVisibility(VISIBLE);
        messageTextView.setText(str);
    }

    private void setTemporaryMessage(final String str, int time) {
        messageTextView.setText(str);
        messageTextView.postDelayed(new Runnable() {
            public void run() {
                if (str.equals(messageTextView.getText().toString()))
                    messageTextView.setVisibility(View.GONE);
            }
        }, time);
    }

    private void removeMessage() {
        messageLayout.setVisibility(GONE);
        messageTextView.setText("");
    }


    public void updateLayout() {

        cardContainer.removeAllViews();
        if (cardDeck != null && cardDeck.size() != 0) {
            double midP = (cardDeck.size() + 1) / 2.0;

            double marginB = ((imgH * .5) / cardDeck.size());
            double rotate = ((totalAng / 2.0) / cardDeck.size());
            double radAng = Math.toRadians(totalAng);
            double sx = imgW / (imgW + (Math.sin(radAng) * ((imgH - imgW))));
            double marginL = -(imgW - (sx * imgW));

            LayoutParams lp = (LayoutParams) cardContainer.getLayoutParams();

            lp.bottomMargin = -(int) marginB;
            for (int i = 1; i <= cardDeck.size(); i++) {
                ImageView img = new ImageView(getContext());

                Card c = cardDeck.get(i - 1);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) imgW, (int) imgH);
                if (i == 1) {
                    layoutParams.setMargins(0, 0, 0, 0);
                } else {
                    layoutParams.setMargins((int) marginL, 0, 0, 0);
                }
                img.setLayoutParams(layoutParams);

                img.setScaleType(ImageView.ScaleType.FIT_XY);

                img.setScaleX((float) sx);
                img.setRotation((float) ((i - midP) * rotate));
                img.setTranslationY((int) (Math.abs(i - midP) * marginB));
                String filename = c.getType().toString().toLowerCase() + "_" + c.getNumber().toString().split("c")[1].toLowerCase();

                String pack = getContext().getPackageName();
                int id = getResources().getIdentifier(filename, "drawable", pack);
                Drawable drawable = getResources().getDrawable(id);
                img.setImageDrawable(drawable);

                img.setTag(c);
                //  img.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorBlackTransparent));

                img.setOnClickListener(deckImgClick);
                cardContainer.addView(img);


                for (int j = 0; j < passCardContainer.getChildCount(); j++) {
                    ImageView imgV = (ImageView) passCardContainer.getChildAt(j);
                    imgV.setOnClickListener(passImgClick);
                }
            }
            passLayout.invalidate();
        }
    }

    private void doMakeTint(Card card) {
        if (card != null && card.getType() != CardType.ANY && cardDeck.hasType(card.getType())) {
            for (int i = 0; i < cardContainer.getChildCount(); i++) {
                ImageView img = (ImageView) cardContainer.getChildAt(i);
                Card imgCard = (Card) img.getTag();
                if (cardDeck.contains(new Card(CardType.CLUB, CardNumber.c2))) {
                    if (!imgCard.equals(new Card(CardType.CLUB, CardNumber.c2))) {
                        img.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorBlackTransparent));
                    }
                } else {
                    if (card.getType() != imgCard.getType()) {
                        img.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorBlackTransparent));
                    }
                }
            }
        } else if (card != null && card.getType() == CardType.ANY) {
            for (int i = 0; i < cardContainer.getChildCount(); i++) {
                ImageView img = (ImageView) cardContainer.getChildAt(i);
                Card imgCard = (Card) img.getTag();

                if ((imgCard.getType() == CardType.HEART && !heartsBroken && !cardDeck.allSameTypeCard(CardType.HEART))) {
                    img.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorBlackTransparent));
                }

            }
        }
    }

    private void doClearTint() {
        for (int i = 0; i < cardContainer.getChildCount(); i++) {
            ImageView img = (ImageView) cardContainer.getChildAt(i);
            img.clearColorFilter();
        }
    }


    public ArrayList<String> getSelectedCardsForPass() {
        ArrayList<String> strs = new ArrayList<>();
        for (int i = 0; i < passCardContainer.getChildCount(); i++) {
            Card c = (Card) passCardContainer.getChildAt(i).getTag();
            if (c == null) {
                return null;
            }
            strs.add(c.toString());
        }
        return strs;
    }


    private ImageView getAvailableSpaceInDeck(Card c) {
        int idx = cardDeck.getIndexToAdd(c);
        if (idx == 0) {
            return (ImageView) cardContainer.getChildAt(0);
        } else {
            return (ImageView) cardContainer.getChildAt(idx - 1);
        }
    }

    private ImageView getAvailableSpaceInPass() {
        final int childCount = passCardContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView v = (ImageView) passCardContainer.getChildAt(i);
            if (v.getDrawable() == null) {
                return v;
            }
        }
        return null;
    }

    public void clearTable() {
        for (int i = 0; i < cardOnTableImageView.length; i++) {
            cardOnTableImageView[i].setImageDrawable(null);
        }
    }

    public double pxFromDp(final double dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }

    private void animateTo(final View fromV, final View toV, Animation.AnimationListener listener) {
        int[] loc1 = new int[2], loc2 = new int[2];
        fromV.getLocationInWindow(loc1);

        int fromX = loc1[0];
        int fromY = loc1[1];
        toV.getLocationOnScreen(loc2);
        int toX = loc2[0];
        int toY = loc2[1];


        TranslateAnimation animation = new TranslateAnimation(0, toX - fromX, 0, toY - fromY); //(float From X,To X, From Y, To Y)
        animation.setDuration(animationTime);
        animation.setFillAfter(false);
        animation.setAnimationListener(listener);

        fromV.startAnimation(animation);

    }

    public void doPassFromMe(PlayerPosition playerPosition) {
        if (gameState == GameState.PASS && playerState == PlayerState.GIVE) {
            int playerNo = -1;
            if (playerPosition == PlayerPosition.LEFT) {
                playerNo = 1;
            } else if (playerPosition == PlayerPosition.RIGHT) {
                playerNo = 3;
            } else if (playerPosition == PlayerPosition.STRAIGHT) {
                playerNo = 2;
            } else {
                //no pass at all
            }

            for (int i = 0; i < passCardContainer.getChildCount(); i++) {
                animateTo(passCardContainer.getChildAt(i), playerNameTextView[playerNo], new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        donePassCardFromMe();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

            }
        }
    }

    public void doPassToMe(final ArrayList<Card> cards, PlayerPosition playerPosition) {

        if (gameState == GameState.PASS && playerState == PlayerState.TAKE) {
            int playerNo = playerPosition.ordinal();
            /*
            if (playerPosition == PlayerPosition.LEFT) {
                playerNo = 3;
            } else if (playerPosition == PlayerPosition.RIGHT) {
                playerNo = 1;
            } else if (playerPosition == PlayerPosition.STRAIGHT) {
                playerNo = 2;
            } else {
                //no pass at all
            }
            */

            for (int i = 0; i < passCardContainer.getChildCount(); i++) {
                int[] loc1 = new int[2], loc2 = new int[2];
                playerNameTextView[playerNo].getLocationInWindow(loc1);

                int fromX = loc1[0];
                int fromY = loc1[1];
                passCardContainer.getChildAt(i).getLocationOnScreen(loc2);
                int toX = loc2[0];
                int toY = loc2[1];


                TranslateAnimation animation = new TranslateAnimation(toX - fromX, 0, toY - fromY, 0); //(float From X,To X, From Y, To Y)
                animation.setDuration(200);
                animation.setFillAfter(false);
                final int finalI = i;
                animation.setDuration(animationTime);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        String filename = cards.get(finalI).getType().toString().toLowerCase() + "_" + cards.get(finalI).getNumber().toString().split("c")[1].toLowerCase();

                        String pack = getContext().getPackageName();
                        int id = getResources().getIdentifier(filename, "drawable", pack);
                        Drawable drawable = getResources().getDrawable(id);
                        ((ImageView) passCardContainer.getChildAt(finalI)).setImageDrawable(drawable);


                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        passCardContainer.getChildAt(finalI).setTag(cards.get(finalI));
                        passLayout.postDelayed(new Runnable() {
                            public void run() {
                                if (passLayout.getVisibility() != GONE) {
                                    passButton.callOnClick();
                                }

                            }
                        }, 3000);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                passCardContainer.getChildAt(i).startAnimation(animation);

            }
        }
    }

    /**
     * Moves other's player card into table
     *
     * @param position other player position
     * @param card     his card
     */
    public void doMoveCard(final PlayerPosition position, final Card card) {
        int playerNameIndex = -1;
        if (position == PlayerPosition.LEFT) {
            playerNameIndex = 1;
        } else if (position == PlayerPosition.STRAIGHT) {
            playerNameIndex = 2;
        } else if (position == PlayerPosition.RIGHT) {
            playerNameIndex = 3;
        }

        int[] loc1 = new int[2], loc2 = new int[2];
        playerNameTextView[playerNameIndex].getLocationInWindow(loc1);
        int fromX = loc1[0];
        int fromY = loc1[1];

        cardOnTableImageView[playerNameIndex].getLocationOnScreen(loc2);
        int toX = loc2[0];
        int toY = loc2[1];

        TranslateAnimation animation = new TranslateAnimation(fromX - toX, 0, fromY - toY, 0); //(float From X,To X, From Y, To Y)
        animation.setDuration(200);
        animation.setFillAfter(false);

        final int finalPlayerNameIndex = playerNameIndex;
        animation.setDuration(animationTime);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                removeMessage();
                String filename = card.getType().toString().toLowerCase() + "_" + card.getNumber().toString().split("c")[1].toLowerCase();
                String pack = getContext().getPackageName();
                int id = getResources().getIdentifier(filename, "drawable", pack);
                Drawable drawable = getResources().getDrawable(id);
                cardOnTableImageView[finalPlayerNameIndex].setImageDrawable(drawable);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doneNewMove();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        cardOnTableImageView[playerNameIndex].startAnimation(animation);

    }

    private void doGetAllCard(PlayerPosition playerPosition) {
        int toX = 0, toY = 0;
        final int[] cardSent = {0};
        int cardAvail = 0;
        int[] loc1 = new int[2], loc2 = new int[2];
        TextView temp;
        if (playerPosition == PlayerPosition.LEFT) {
            temp = playerNameTextView[1];
            temp.getLocationOnScreen(loc2);
            toX = loc2[0];
            toY = loc2[1];
        } else if (playerPosition == PlayerPosition.STRAIGHT) {
            temp = playerNameTextView[2];
            temp.getLocationOnScreen(loc2);
            toX = loc2[0];
            toY = loc2[1];
        } else if (playerPosition == PlayerPosition.RIGHT) {
            temp = playerNameTextView[3];
            temp.getLocationOnScreen(loc2);
            toX = loc2[0];
            toY = loc2[1];
        } else {
            cardContainer.getLocationOnScreen(loc2);
            toY = loc2[1] + cardContainer.getHeight();
        }
        for (ImageView img : cardOnTableImageView) {
            if (img.getDrawable() != null) {
                cardAvail++;
            }
        }
        for (final ImageView img : cardOnTableImageView) {

            if (img.getDrawable() != null) {

                img.getLocationInWindow(loc1);

                int fromX = loc1[0];
                int fromY = loc1[1];


                TranslateAnimation animation = new TranslateAnimation(0, toX - fromX, 0, toY - fromY); //(float From X,To X, From Y, To Y)
                animation.setDuration(200);
                animation.setFillAfter(false);
                final int finalCardAvail = cardAvail;
                animation.setDuration(animationTime);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        img.setImageDrawable(null);
                        cardSent[0]++;
                        if (cardSent[0] == finalCardAvail) {
                            doneNewMove();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                img.startAnimation(animation);
            }
        }
    }


    private void addCardToDeck(ImageView img) {
        int idx = cardDeck.getIndexToAdd((Card) img.getTag());
        cardContainer.addView(img, idx);
        updateLayout();
    }

    public void clear() {
        if (cardDeck != null && cardContainer != null) {
            cardDeck.clear();
            cardContainer.removeAllViews();
            for (int i = 0; i < passCardContainer.getChildCount(); i++) {
                ImageView img = (ImageView) passCardContainer.getChildAt(i);
                img.setImageDrawable(null);
                img.setTag(null);
            }
            updateLayout();
        }

    }

    private void removeCardDeck(ImageView img) {
        cardDeck.removeCard((Card) img.getTag());
        for (int i = 0; i < cardContainer.getChildCount(); i++) {
            ImageView v = (ImageView) cardContainer.getChildAt(i);
            if (((Card) v.getTag()).equals((Card) v.getTag())) {
                // cardContainer.removeView(v);
                updateLayout();
                return;
            }
        }
    }


}
