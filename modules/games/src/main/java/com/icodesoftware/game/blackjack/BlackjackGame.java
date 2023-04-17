package com.icodesoftware.game.blackjack;

import com.icodesoftware.game.Card;
import com.icodesoftware.game.Deck;

public class BlackjackGame {

    private final Deck deck;
    private final boolean hitWithSoft17;

    public BlackjackGame(int deckSize,boolean autoShuffling,boolean hitWithSoft17){
        this.hitWithSoft17 = hitWithSoft17;
        deck = new Deck(deckSize,autoShuffling);
    }

    public String name(){
        return "Blackjack";
    }

    public boolean hitWithSoft17(){
        return hitWithSoft17;
    }

    public Card[] deal(){
        Card card1 = deck.draw();
        Card card2 = deck.draw();
        return new Card[]{card1,card2};
    }

    public Card draw(){
        return deck.draw();
    }

    public boolean blackjack(Card[] hand){
        return hand[0].alter + hand[1].alter == 21;
    }

    public int rank(Card[] hand){
        int pts = 0;
        int aceCard = 0;
        for(Card card : hand){
            pts += card.rank;
            if(card.rank == 1) aceCard++;
        }
        if(aceCard == 0) return pts;
        if(pts + 10 > 21) return pts;
        if(pts + 10 > pts) return pts + 10;
        return pts;
    }

    public boolean bust(Card[] hand){
        return rank(hand) > 21;
    }

    public boolean dealerHit(Card[] hand){
        int pts = 0;
        int aceCard = 0;
        for(Card card : hand){
            pts += card.rank;
            if(card.rank == 1) aceCard++;
        }
        if(aceCard == 0) return pts < 17;
        if(hitWithSoft17) return pts + 10 <= 17;
        return pts + 10 < 17;
    }


}
