package com.icodesoftware.game.test;

import com.tarantula.game.Card;
import com.tarantula.game.blackjack.BlackjackGame;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BlackjackGameTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "blackjack" })
    public void autoShuffleDealTest() {
        BlackjackGame game = new BlackjackGame(1,true);
        for(int i=0;i<100;i++){
            Card[] hand = game.deal();
            Assert.assertNotNull(hand[0]);
            Assert.assertNotNull(hand[1]);
        }
    }

    @Test(groups = { "blackjack" })
    public void blackjackHandTest() {
        BlackjackGame game = new BlackjackGame(1,true);
        Card[] hand1 = new Card[]{Card.C1,Card.C10};
        Assert.assertTrue(game.blackjack(hand1));
        Card[] hand2 = new Card[]{Card.S11,Card.S1};
        Assert.assertTrue(game.blackjack(hand2));
    }
}
