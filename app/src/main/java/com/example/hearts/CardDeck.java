package com.example.hearts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

public class CardDeck implements Serializable {
    private ArrayList<Card> cards;
    CardDeck(){
        cards=new ArrayList<>();
    }
    CardDeck(ArrayList<Card> cards){
        this.cards=cards;
        Collections.sort(cards);
    }

    public void addCard(Card a){
        cards.add(a);
        Collections.sort(cards);
    }

    public int getIndexToAdd(Card c1){
        cards.add(c1);
        Collections.sort(cards);
        int i=0;
        for(Card c2: cards){
            if(c1.equals(c2)){
                cards.remove(c1);
                return i;
            }
            i++;
        }
        cards.remove(c1);
        return -1;
    }

    public void removeCard(Card a){
        cards.remove(a);
    }

    public int size(){
        return cards.size();
    }

    public Card get(int index){
        return (Card) cards.get(index);
    }

    public boolean hasType(CardType type){
        for(Card c: cards){
            if(c.getType().equals(type)){
                return true;
            }
        }
        return false;
    }

    public boolean allSameTypeCard(CardType type){
        for(Card c: cards){
            if(!c.getType().equals(type)){
                return false;
            }
        }
        return true;
    }

    public boolean hasPoint(){
        for(Card c: cards){
            if(c.getType().equals(CardType.HEART) || (c.getType().equals(CardType.SPADE) && c.getNumber().equals(CardNumber.cQ))){
                return true;
            }
        }
        return false;
    }

    public void clear(){
        cards.clear();
    }

    public boolean contains(Card c){
        return cards.contains(c);
    }

    public boolean isEmpty(){
        return cards.isEmpty();
    }
}
