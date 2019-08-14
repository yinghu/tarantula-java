package com.tarantula.game.casino.blackjack;

import com.tarantula.game.casino.BetLine;
import com.tarantula.game.casino.Card;

/**
 * Created by yinghu lu on 11/26/2018
 */
public class BlackJackHand extends BetLine {
    public Card[] hand;
    public final int subscript;
    private boolean soft;
    private int rank =0;
    public boolean standing;
    public boolean natural;
    public boolean onDeck;
    private int _index;
    public BlackJackHand(int subscript){
        this.subscript = subscript;
    }

    private void _onHand(Card card){
        rank=rank+card.rank;
        this.soft = this.soft?soft:(card.rank==1);
        hand[_index++]=card;
    }
    public int deal(Card c1,Card c2){
        hand = new Card[2];
        _index = 0;
        rank = 0;
        soft = false;
        this._onHand(c1);
        this._onHand(c2);
        return this.rank();
    }
    public int swap(Card holeCard){
        if(!onDeck){
            rank=rank+holeCard.rank;
            if(!this.soft){
                this.soft = (holeCard.rank==1);
            }
            this.natural = this.rank()==21;
            hand[1]=holeCard;
            onDeck = true;
        }
        return this.rank();
    }
    public boolean stand(){
        if(!standing){
            standing = true;
            return true;
        }else{
            return false;
        }
    }
    public int hit(Card c){
        Card[] ex = this.hand;
        this.hand = new Card[ex.length+1];
        for(int i=0;i<ex.length;i++){
            this.hand[i]=ex[i];
        }
        this._onHand(c);
        return this.rank();
    }
    public BlackJackHand[] split(){
        BlackJackHand[] sp = new BlackJackHand[2];
        BlackJackHand h1 = new BlackJackHand(0);
        h1.hand = new Card[1];
        h1._onHand(hand[0]);
        BlackJackHand h2 = new BlackJackHand(1);
        h2.hand = new Card[1];
        h2._onHand(hand[1]);
        sp[0]=h1;
        sp[1]=h2;
        return sp;
    }
    public int rank(){
        if(this.soft&&(rank+10)<=21){
            return rank+10;
        }
        else{
            return (rank);
        }
    }
    public boolean splittable(){
        return ((!(standing))&&(hand[0].rank==hand[1].rank));
    }
}
