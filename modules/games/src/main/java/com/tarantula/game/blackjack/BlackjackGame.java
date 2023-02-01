package com.tarantula.game.blackjack;

import com.tarantula.game.Card;
import com.tarantula.game.Deck;

public class BlackjackGame {

    private final Deck deck;

    public BlackjackGame(int deckSize,boolean autoShuffling){
        deck = new Deck(deckSize,autoShuffling);
    }
    public String name(){
        return "Blackjack";
    }
    public Card[] deal(){
        Card card1 = deck.draw();
        Card card2 = deck.draw();
        return new Card[]{card1,card2};
    }

    public boolean blackjack(Card[] hand){
        return hand[0].alter+hand[1].alter == 21;
    }

    public boolean bust(Card[] hand){
        int pts = 0;
        int aceCard = 0;
        for(Card card : hand){
            pts += card.rank;
            if(card.rank==1) aceCard++;
        }

        return pts > 21;
    }

    public boolean soft17(Card[] hand){
        return hand[0].alter+hand[1].alter == 17;
    }


}
