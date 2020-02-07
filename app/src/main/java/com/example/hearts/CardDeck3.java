package com.example.hearts;

import android.content.Context;

import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

public class CardDeck3 extends LinearLayout  {
 //   private String[] cards = new String[13];
    private Context context;

    private LinearLayout cardContainer;
  //  private ImageView[] cardsImage = new ImageView[13];
    private Vector<String> userCards;
    private List<LinearLayout> llList = new ArrayList<>();

    public CardDeck3(Context context) {
        super(context);
        this.context = context;

        init();
    }

    public CardDeck3(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CardDeck3(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public CardDeck3(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    private void init() {

        //Inflate xml resource, pass "this" as the parent, we use <merge> tag in xml to avoid
        //redundant parent, otherwise a LinearLayout will be added to this LinearLayout ending up
        //with two view groups
        inflate(getContext(), R.layout.carddeck, this);

        //Get references to text views
        cardContainer = findViewById(R.id.cardContainer);


        for (int i = 0; i < 13; i++) {
            LinearLayout ll = new LinearLayout(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i != 0)
                layoutParams.setMargins((int) context.getResources().getDimension(R.dimen.card_margin_left), 0, 0, 0);
            ll.setLayoutParams(layoutParams);
            llList.add(ll);
            ImageView img = new ImageView(context);
            img.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            img.setElevation((int) context.getResources().getDimension(R.dimen.card_elevation));
            img.setLayoutParams(new ViewGroup.LayoutParams((int) context.getResources().getDimension(R.dimen.card_width), (int) context.getResources().getDimension(R.dimen.card_height)));
            // img.setPadding((int) context.getResources().getDimension(R.dimen.card_padding_left), (int) context.getResources().getDimension(R.dimen.card_padding_top), (int) context.getResources().getDimension(R.dimen.card_padding_right), (int) context.getResources().getDimension(R.dimen.card_padding_bottom));

            img.setScaleType(ImageView.ScaleType.FIT_XY);
            img.setId(i);

            ll.addView(img);


            cardContainer.addView(ll);

        }


        //Animate views with a nice fadeIn effect before drawing

    }

    public void loadDeck(Vector<String> ucards) {
        userCards = ucards;

        String[] str = ucards.toArray(new String[ucards.size()]);
        Arrays.sort(str, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {

                String[] s1 = o1.split("_");
                String[] s2 = o2.split("_");
                if (s1[1].equals(s2[1]))
                    return Integer.valueOf(s1[2]).compareTo(Integer.valueOf(s2[2]));
                else
                    return s1[1].compareTo(s2[1]);
            }
        });

        for (int i = 0; i < str.length; i++) {
            ImageView img = findViewById(i);

            try {
                img.setImageDrawable(context.getResources().getDrawable(context.getResources().getIdentifier("@drawable/" + str[i], null, context.getPackageName())));
                img.setTag(str[i]);


            } catch (Exception e) {

            }

        }
    }

    public Vector<ImageView> getCardsImage() {
        Vector<ImageView> imgV = new Vector<>();
        for (int i = 0; i < 13; i++) {
            imgV.add((ImageView) findViewById(i));
        }
        return imgV;
    }

    public void addCard(String str) {
        for (LinearLayout ll : llList) {
            if (!cardContainerContains(ll)) {
                addCardAtValidPosition(decideWhereToAdd(str),ll,str);
                return;
            }

        }
    }

    public int decideWhereToAdd(String str) {
        for (int i = 0; i < cardContainer.getChildCount(); i++) {
            String curr = (String) ((ImageView) ((LinearLayout) cardContainer.getChildAt(i)).getChildAt(0)).getTag();
            String currType = curr.split("_")[1];
            int currID = Integer.parseInt(curr.split("_")[2]);
            String strType = str.split("_")[1];
            int strID = Integer.parseInt(str.split("_")[2]);
            if (currType.compareTo(strType) > 0) {
                return i;
            } else if (currType.equals(strType)) {
                if (currID > strID) {
                    return i;
                }
            }
        }
        return cardContainer.getChildCount() ;
    }

    public void addCardAtValidPosition(int position, LinearLayout ll, String str) {
        ImageView img = (ImageView) ll.getChildAt(0);
        img.setTag(str);
        img.setImageDrawable(context.getResources().getDrawable(context.getResources().getIdentifier("@drawable/" + str, null, context.getPackageName())));
        if (position == 0) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cardContainer.getChildAt(0).getLayoutParams();
            layoutParams.setMargins((int) context.getResources().getDimension(R.dimen.card_margin_left), 0, 0, 0);
            ((LinearLayout) cardContainer.getChildAt(0)).setLayoutParams(layoutParams);

            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) ll.getLayoutParams();
            layoutParams2.setMargins(0,0,0,0);
            ll.setLayoutParams(layoutParams2);

        }else {
            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) ll.getLayoutParams();
            layoutParams2.setMargins((int) context.getResources().getDimension(R.dimen.card_margin_left),0,0,0);
            ll.setLayoutParams(layoutParams2);
        }
        cardContainer.addView(ll,position);
    }

    public boolean cardContainerContains(LinearLayout ll) {
        for (int i = 0; i < cardContainer.getChildCount(); i++) {
            if (cardContainer.getChildAt(i).equals(ll)) {
                return true;
            }
        }
        return false;
    }

    public void reloadCards() {
        for (LinearLayout ll : llList) {
            //((ImageView)((LinearLayout)findViewById(i+13)).getChildAt(0)).setImageResource(0);
            cardContainer.addView(ll);
        }
        for (int i = 0; i < 13; i++) {
            if (i != 0) {

                LayoutParams layoutParams = (LinearLayout.LayoutParams) ((LinearLayout) cardContainer.getChildAt(i)).getLayoutParams();
                layoutParams.setMargins((int) context.getResources().getDimension(R.dimen.card_margin_left), 0, 0, 0);
                ((LinearLayout) cardContainer.getChildAt(i)).setLayoutParams(layoutParams);
            }

        }
    }

    public LinearLayout getCardContainer() {
        return cardContainer;
    }
}
