package com.example.hearts;

import java.io.Serializable;

public class Card implements Serializable, Comparable<Card> {
    //heart_2, heart_3......heart_Q, heart_K, heart_A
    // public static final String HEART="heart",CLUB="club", SPADE="spade", DIAMOND="diamond", ACE="a",KING="k",QUEEN="q",JACK="j";

    private CardType type;
    private CardNumber number;

    public Card(CardType type, CardNumber number) {
        this.type = type;
        this.number = number;
    }

    public Card() {
    }


    public CardType getType() {
        return type;
    }

    public Card(String cardStr) {
        String typeStr = cardStr.split("_")[0];
        String numberStr = cardStr.split("_")[1];
        this.type = CardType.valueOf(typeStr);
        this.number = CardNumber.valueOf(numberStr);
    }

    public CardNumber getNumber() {
        return number;
    }

    public void setCard(CardType type, CardNumber number) {
        this.type = type;
        this.number = number;
    }

    private int getRank(CardNumber s) {
        return s.ordinal();
    }

    private int getRank(CardType s) {
        return s.ordinal();
    }

    @Override
    public int compareTo(Card o) {
        if (o == null) {
            return 0;
        } else if (type == null || o.getType() == null || o.getNumber() == null) {
            return 0;
        } else if (!type.equals(o.getType())) {
            int r1 = getRank(type);
            int r2 = getRank(o.getType());
            return r1 - r2;
        } else {
            int r1 = getRank(number);
            int r2 = getRank(o.getNumber());
            return r1 - r2;

        }

    }

    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Card)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Card c = (Card) o;


        if (type == null || c.getType() == null) {
            return false;
        }

        // Compare the data members and return accordingly
        return type.equals(c.getType()) && number.equals(c.getNumber());
    }


    public String toString() {
        return type.name() + "_" + number.name();
    }
}
