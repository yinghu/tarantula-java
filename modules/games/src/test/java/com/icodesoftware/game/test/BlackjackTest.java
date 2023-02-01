package com.icodesoftware.game.test;

import com.tarantula.game.Card;
import com.tarantula.game.blackjack.Deck;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BlackjackTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "blackjack" })
    public void deckFor4Test() {
        Deck deck = new Deck();
        Assert.assertEquals(deck.size(),Deck.DECK_SIZE);
        for(int i=0;i<Deck.CARDS_PER_DECK*Deck.DECK_SIZE;i++){
            Card card = deck.draw();
            Assert.assertNotNull(card);
        }
    }

    @Test(groups = { "blackjack" })
    public void deckFor1Test() {
        Deck deck = new Deck(1);
        Assert.assertEquals(deck.size(),1);
        for(int i=0;i<Deck.CARDS_PER_DECK*1;i++){
            Card card = deck.draw();
            Assert.assertNotNull(card);
        }
    }

    @Test(groups = { "blackjack" })
    public void autoShuffleFor1Test() {
        Deck deck = new Deck(1);
        Assert.assertEquals(deck.size(),1);
        for(int i=0;i<Deck.CARDS_PER_DECK*10;i++){
            Card card = deck.draw();
            Assert.assertNotNull(card);
        }
    }

}
