package com.tarantula.game.casino;

import com.tarantula.RNG;
import com.tarantula.game.GameComponent;
import com.tarantula.platform.util.JvmRNG;

/**
 * Created by yinghu lu on 11/26/2018
 */
public class Deck extends GameComponent {
    private static Card[] cardList = new Card[]{
            Card.S1, Card.S2, Card.S3, Card.S4, Card.S5, Card.S6, Card.S7, Card.S8, Card.S9, Card.S10, Card.S11, Card.S12, Card.S13,
            Card.H1, Card.H2, Card.H3, Card.H4, Card.H5, Card.H6, Card.H7, Card.H8, Card.H9, Card.H10, Card.H11, Card.H12, Card.H13,
            Card.D1, Card.D2, Card.D3, Card.D4, Card.D5, Card.D6, Card.D7, Card.D8, Card.D9, Card.D10, Card.D11, Card.D12, Card.D13,
            Card.C1, Card.C2, Card.C3, Card.C4, Card.C5, Card.C6, Card.C7, Card.C8, Card.C9, Card.C10, Card.C11, Card.C12, Card.C13,
    };
    private int[] deck;

    private RNG rnd = new JvmRNG();

    private int size;
    public int cutter;
    public CardCounter cardCounter;
    private void _init(){
        deck = new int[52*size];
        int ix = 0;
        for(int sz=0;sz<size;sz++){
            for(int i=0;i<52;i++){
                deck[ix++]=i;
            }
        }
    }
    public Deck(){

    }
    public Deck(int size){
        this.name = "deck";
        this.size = size;
        this.cardCounter = new CardCounter();
        this._init();
        this._shuffle();
    }
    public void _shuffle(){
        for (int i=52*size-1;i>0;i--) {
            int _rx = rnd.onNext(i+1);
            int tmp = deck[_rx];
            deck[_rx] = deck[i];
            deck[i] = tmp;
        }
        cutter = 52*size-rnd.onNext(13);
        this.subscript = 0;
        cardCounter.clear();
    }
    public Card draw(){
        if(this.subscript>=cutter){
            this._shuffle();
        }
        Card c = cardList[deck[subscript++]];
        cardCounter.count(c,1);
        return c;
    }
    public Deck setup(){
        Deck _cp = new Deck();
        _cp.duplicate(this.systemId,this.componentId,this.name,this.label,this.subscript,this.broadcasting);
        _cp.cutter = this.cutter;
        return _cp;
    }
}
