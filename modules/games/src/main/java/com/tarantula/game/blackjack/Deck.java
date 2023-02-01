package com.tarantula.game.blackjack;

import com.icodesoftware.service.RNG;
import com.icodesoftware.util.*;
import com.tarantula.game.Card;


public class Deck{

    public final static int DECK_SIZE = 4;
    public final static int CARDS_PER_DECK = 52;

    private static Card[] cardList = new Card[]{
            Card.S1, Card.S2, Card.S3, Card.S4, Card.S5, Card.S6, Card.S7, Card.S8, Card.S9, Card.S10, Card.S11, Card.S12, Card.S13,
            Card.H1, Card.H2, Card.H3, Card.H4, Card.H5, Card.H6, Card.H7, Card.H8, Card.H9, Card.H10, Card.H11, Card.H12, Card.H13,
            Card.D1, Card.D2, Card.D3, Card.D4, Card.D5, Card.D6, Card.D7, Card.D8, Card.D9, Card.D10, Card.D11, Card.D12, Card.D13,
            Card.C1, Card.C2, Card.C3, Card.C4, Card.C5, Card.C6, Card.C7, Card.C8, Card.C9, Card.C10, Card.C11, Card.C12, Card.C13,
    };
    private int[] deck;

    private RNG rnd;

    private final String name;
    private final int size;
    private int cutter;
    private int stub;

    public String name(){
        return name;
    }
    public int size(){
        return size;
    }

    private void _init(){
        rnd = new JvmRNG();
        deck = new int[CARDS_PER_DECK*size];
        int ix = 0;
        for(int sz=0;sz<size;sz++){
            for(int i=0;i<CARDS_PER_DECK;i++){
                deck[ix++]=i;
            }
        }
    }
    public Deck(){
        this(DECK_SIZE);
    }
    public Deck(int size){
        this.name = "deck";
        this.size = size;
        this._init();
        this._shuffle();
    }
    private void _shuffle(){
        for (int i=CARDS_PER_DECK*size-1;i>0;i--) {
            int _rx = rnd.onNext(i+1);
            int tmp = deck[_rx];
            deck[_rx] = deck[i];
            deck[i] = tmp;
        }
        cutter = CARDS_PER_DECK*size-rnd.onNext(13);
        this.stub = 0;

    }
    public synchronized void shuffle(){
        _shuffle();
    }
    public synchronized Card draw(){
        if(this.stub>=cutter){
            this._shuffle();
        }
        Card c = cardList[deck[stub++]];
        return c;
    }
}
