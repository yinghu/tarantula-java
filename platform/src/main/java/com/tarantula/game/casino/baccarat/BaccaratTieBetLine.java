package com.tarantula.game.casino.baccarat;

import com.tarantula.game.casino.BetLine;
import com.tarantula.game.casino.CashInBalance;
import com.tarantula.game.casino.Seat;

/**
 * Created by yinghu lu on 12/26/2018.
 */
public class BaccaratTieBetLine extends Seat {

    private Baccarat baccarat;
    public BaccaratTieBetLine(Baccarat baccarat){
        this.stub = Baccarat.TIE_LINE_ID;
        this.name = "BaccaratTieBetLine";
        this.baccarat = baccarat;
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
                if(baccarat.betLineListener.onPayout(v.systemId(),v.wager()*9)){
                    Seat seat = baccarat.seatList.get(v.systemId());
                    seat.balance(baccarat.betLineListener.balance(v.systemId()));
                    baccarat.pendingQueue.offer(new CashInBalance(v.systemId(),seat.subscript,seat.balance(),"balance","baccarat"));
                }
            }
            baccarat.payout = baccarat.payout+(v.wager()*9);
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
    }
    public BaccaratTieBetLine setup(){
        return this;
    }
}
