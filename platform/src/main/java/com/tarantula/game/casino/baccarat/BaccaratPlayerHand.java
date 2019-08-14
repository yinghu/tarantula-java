package com.tarantula.game.casino.baccarat;

import com.tarantula.game.casino.BetLine;
import com.tarantula.game.casino.Card;
import com.tarantula.game.casino.CashInBalance;
import com.tarantula.game.casino.Seat;



/**
 * Created by yinghu lu on 12/26/2018.
 */
public class BaccaratPlayerHand extends Seat {
    public Card[] hand;
    public boolean standing;

    private Baccarat baccarat;
    public BaccaratPlayerHand(Baccarat baccarat){
        this.stub = Baccarat.PLAYER_LINE_ID;
        this.name = "BaccaratPlayerHand";
        this.label = "baccarat";
        this.baccarat = baccarat;
        this.odd = 2d;
    }
    public int deal(Card c1,Card c2){
        hand = new Card[]{c1,c2};
        return this.rank();
    }
    public boolean thirdCard(Card c3){
        if(standing){
            return false;
        }
        Card[] th = new Card[]{hand[0],hand[1],c3};
        hand = th;
        standing = true;
        this.baccarat.pendingQueue.offer(this);
        return true;
    }

    public int checkThirdCard(){
        if(hand.length==2){
            return -1; //no third card
        }
        return hand[2].rank%10;
    }
    public int rank(){
        int rank = 0;
        for(Card c : hand){
            rank = (rank+c.rank);
        }
        return rank%10;
    }
    public void wager(BetLine wager){
        this.balance = balance+wager.wager();
        wagerList.add(wager);
        wagered = true;
    }
    public double payout(){
        baccarat.payout = 0;
        wagerList.forEach((v)->{
            if(v.systemId()!=null){
                if(baccarat.betLineListener.onPayout(v.systemId(),v.wager()*odd)){
                    Seat seat = baccarat.seatList.get(v.systemId());
                    seat.balance(baccarat.betLineListener.balance(v.systemId()));
                    baccarat.pendingQueue.offer(new CashInBalance(v.systemId(),seat.subscript,seat.balance(),"balance","baccarat"));
                }
            }
            baccarat.payout = baccarat.payout+v.wager()*odd;
        });
        if(baccarat.houseSeat.asPlayer()){
            if(baccarat.betLineListener.onWager(baccarat.houseSeat.systemId(),baccarat.payout)){
                Seat st = baccarat.seatList.get(baccarat.houseSeat.systemId());
                st.balance(baccarat.betLineListener.balance(baccarat.houseSeat.systemId()));
                baccarat.houseSeat.balance(st.balance());
            }
        }else{
            baccarat.houseSeat.balance(baccarat.betLineListener.onHouse((-1)*baccarat.payout));
        }
        baccarat.pendingQueue.add(new CashInBalance(baccarat.houseSeat.systemId(),-1,baccarat.houseSeat.balance(),"dealer","baccarat"));
        return baccarat.payout;
    }
    public void clear(){
        wagerList.clear();
        wagered = false;
        this.hand = null;
        this.standing = false;
    }
    public BaccaratPlayerHand setup(){
        return this;
    }
}
