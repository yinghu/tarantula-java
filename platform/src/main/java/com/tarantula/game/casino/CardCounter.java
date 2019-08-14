package com.tarantula.game.casino;

import java.util.concurrent.ConcurrentHashMap;

public class CardCounter{
    private int[] countByRank;
    private ConcurrentHashMap<Card,Integer> counterByCard;

    public CardCounter(){
        this.countByRank = new int[10];
        this.counterByCard = new ConcurrentHashMap<>();
    }
    public int count(Card card,int c){
        return counterByCard.compute(card,(k,v)->{
            if (v==null){
                v = 0;
            }
            v +=c;
            countByRank[card.rank-1] +=c;
            return v;
        });
    }
    public void listByRank(OnCounterByRank onCounter){
        for(int i=0;i<10;i++){
            onCounter.on(i+1,countByRank[i]);
        }
    }
    public void listByCard(OnCounterByCard onCounter){
        counterByCard.forEach((k,c)->{
            onCounter.on(k,c);
        });
    }
    public void clear(){
        for(int i=0 ;i<10;i++){
            countByRank[i]=0;
        }
        counterByCard.clear();
    }
    public interface OnCounterByRank{
        void on(int rank,int count);
    }
    public interface OnCounterByCard{
        void on(Card card,int count);
    }
}
