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
        BlackjackGame game = new BlackjackGame(1,true,true);
        for(int i=0;i<100;i++){
            Card[] hand = game.deal();
            Assert.assertNotNull(hand[0]);
            Assert.assertNotNull(hand[1]);
        }
    }

    @Test(groups = { "blackjack" })
    public void blackjackHandTest() {
        BlackjackGame game = new BlackjackGame(1,true,true);
        Card[] hand1 = new Card[]{Card.C1,Card.C10};
        Assert.assertTrue(game.blackjack(hand1));
        Card[] hand2 = new Card[]{Card.S11,Card.S1};
        Assert.assertTrue(game.blackjack(hand2));
        Card[] hand3 = new Card[]{Card.S11,Card.D1};
        Assert.assertTrue(game.blackjack(hand3));
    }

    @Test(groups = { "blackjack" })
    public void rankHandTest() {
        BlackjackGame game = new BlackjackGame(1,true,true);
        //21
        Card[] hand1 = new Card[]{Card.C1,Card.C10};
        Assert.assertTrue(game.rank(hand1)==21);

        //11
        Card[] hand2 = new Card[]{Card.S4,Card.D7};
        Assert.assertTrue(game.rank(hand2)==11);

        //19
        Card[] hand3 = new Card[]{Card.S4,Card.D7,Card.C8};
        Assert.assertTrue(game.rank(hand3)==19);

        //12
        Card[] hand4 = new Card[]{Card.S1,Card.D1,Card.C10};
        Assert.assertTrue(game.rank(hand4)==12);

        //21
        Card[] hand5 = new Card[]{Card.S1,Card.D1,Card.C6,Card.H3};
        Assert.assertTrue(game.rank(hand5)==21);

        //12
        Card[] hand6 = new Card[]{Card.S1,Card.D1,Card.C6,Card.H4};
        Assert.assertTrue(game.rank(hand6)==12);

        //20
        Card[] hand7 = new Card[]{Card.S1,Card.D1,Card.C5,Card.H3};
        Assert.assertTrue(game.rank(hand7)==20);
    }

    @Test(groups = { "blackjack" })
    public void bustHandTest() {
        BlackjackGame game = new BlackjackGame(1,true,true);
        //22
        Card[] hand1 = new Card[]{Card.C1,Card.C10,Card.C6,Card.D5};
        Assert.assertTrue(game.rank(hand1)==22);
        Assert.assertTrue(game.bust(hand1));

        //23
        Card[] hand2 = new Card[]{Card.C2,Card.C8,Card.C4,Card.D1,Card.C8};
        Assert.assertTrue(game.rank(hand2)==23);
        Assert.assertTrue(game.bust(hand2));

    }

    @Test(groups = { "blackjack" })
    public void dealerHitHandTest() {
        BlackjackGame game = new BlackjackGame(1,true,true);

        Assert.assertTrue(game.hitWithSoft17());
        //soft17
        Card[] hand1 = new Card[]{Card.C1,Card.C6};
        Assert.assertTrue(game.rank(hand1)==17);
        Assert.assertTrue(game.dealerHit(hand1));

        //16
        Card[] hand2 = new Card[]{Card.C6,Card.C10};
        Assert.assertTrue(game.rank(hand2)==16);
        Assert.assertTrue(game.dealerHit(hand2));

        //12
        Card[] hand3 = new Card[]{Card.C1,Card.D1};
        Assert.assertTrue(game.rank(hand3)==12);
        Assert.assertTrue(game.dealerHit(hand3));
    }


}
