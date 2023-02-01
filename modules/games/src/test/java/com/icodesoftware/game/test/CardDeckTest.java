package com.icodesoftware.game.test;

import com.tarantula.game.Card;
import com.tarantula.game.Deck;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CardDeckTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "deck" })
    public void deckFor4Test() {
        Deck deck = new Deck();
        Assert.assertTrue(deck.autoShuffling());
        Assert.assertEquals(deck.size(),Deck.DECK_SIZE);
        for(int i=0;i<Deck.CARDS_PER_DECK*Deck.DECK_SIZE;i++){
            Card card = deck.draw();
            Assert.assertNotNull(card);
        }
    }

    @Test(groups = { "deck" })
    public void deckFor1Test() {
        Deck deck = new Deck(1,true);
        Assert.assertTrue(deck.autoShuffling());
        Assert.assertEquals(deck.size(),1);
        for(int i=0;i<Deck.CARDS_PER_DECK*1;i++){
            Card card = deck.draw();
            Assert.assertNotNull(card);
        }
    }

    @Test(groups = { "deck" })
    public void autoShuffleFor1Test() {
        Deck deck = new Deck(1,true);
        Assert.assertTrue(deck.autoShuffling());
        Assert.assertEquals(deck.size(),1);
        for(int i=0;i<Deck.CARDS_PER_DECK*10;i++){
            Card card = deck.draw();
            Assert.assertNotNull(card);
        }
    }

    @Test(groups = { "deck" })
    public void notAutoShuffleFor1Test() {
        Deck deck = new Deck(1,false);
        Assert.assertFalse(deck.autoShuffling());
        Assert.assertEquals(deck.size(),1);
        int nullCard =0 ;
        for(int i=0; i< Deck.CARDS_PER_DECK*1;i++){
            Card card = deck.draw();
            if(card==null) {
                nullCard = i;
            }
        }
        Assert.assertTrue(nullCard>0);
    }

    @Test(groups = { "deck" })
    public void reShuffleTest() {
        Deck deck = new Deck(1,false);
        Assert.assertFalse(deck.autoShuffling());
        Assert.assertEquals(deck.size(),1);
        int shuffles =0 ;
        for(int i=0; i< Deck.CARDS_PER_DECK*10;i++){
            Card card = deck.draw();
            if(card==null){
                deck.shuffle();
                shuffles++;
                card = deck.draw();
            }
            Assert.assertNotNull(card);
        }
        Assert.assertTrue(shuffles>0);
    }

}
