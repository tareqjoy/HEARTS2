package com.example.hearts;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

public class GameEvent {
    private int N;

    private ArrayList<Card> allCards;

    private CardDeck[] players;

    GameEvent(int N) {
        players = new CardDeck[N];
        allCards = new ArrayList<>();
    }

    public void generateCard() {
        for (CardType type : CardType.values()) {
            for (CardNumber number : CardNumber.values()) {
                allCards.add(new Card(type, number));
            }
        }
    }

    public void distributeCards() {
        for (int i = 0; i < N; i++) {
            select13CardsToDeck(players[i]);
        }
    }




    private void select13CardsToDeck(CardDeck player) {
        Random random = new Random();

        for (int i = 0; i < 13; i++) {

            int randomInt = 0;
            if (allCards.size() > 1)
                randomInt = random.nextInt(allCards.size() - 1);
            player.addCard(allCards.get(randomInt));
            allCards.remove(randomInt);
        }
    }
}
