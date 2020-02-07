package com.example.hearts;

public class PlayerCard {


    private String player;
    private Card card;

    PlayerCard() {
    }

    ;

    PlayerCard(String uuid, Card card) {
        this.player = uuid;
        this.card = card;
    }


    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }


    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }
}
