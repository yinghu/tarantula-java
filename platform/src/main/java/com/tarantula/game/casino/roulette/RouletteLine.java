package com.tarantula.game.casino.roulette;

import com.tarantula.game.casino.CashInBalance;
import com.tarantula.game.casino.Seat;


/**
 * Created by yinghu lu on 1/12/2019.
 */
public class RouletteLine extends Seat {
    public Roulette roulette;
    public int lineId;
    public RouletteLine(int lineId,String symbol,double odd,int index,Roulette roulette){
        this.lineId = lineId;
        this.symbol = symbol;
        this.odd = odd;
        this.subscript = index;
        this.roulette = roulette;
    }

    public double payout(){
        roulette.payout = 0;
        wagerList.forEach((v)->{
            double _pay = v.wager()*odd;
            if(v.systemId()!=null){
                if(roulette.betLineListener.onPayout(v.systemId(),_pay)){
                    Seat seat = roulette.seatList.get(v.systemId());
                    seat.balance(roulette.betLineListener.balance(v.systemId()));
                    roulette.payout = roulette.payout+_pay;
                    this.roulette.pendingQueue.offer(new CashInBalance(v.systemId(),seat.occupied?seat.subscript:-1,seat.balance(),"balance","roulette"));
                }
            }
        });
        if(roulette.payout>0){
            this.roulette.betLineListener.onStatistics("totalPayout",roulette.payout,3);
            if(roulette.dealer.asPlayer()){
                //pay by player occupied
                if(!this.roulette.betLineListener.onWager(roulette.dealer.systemId(),roulette.payout)){
                    //kick off
                    System.out.print(">>>>>>>>>>>>>>>>>>KICK OFF");
                }
                roulette.dealer.balance(roulette.betLineListener.balance(roulette.dealer.systemId()));
            }else {
                roulette.dealer.balance(roulette.betLineListener.onHouse((-1) * roulette.payout));
            }
            roulette.pendingQueue.add(new CashInBalance(roulette.dealer.systemId(),-1,roulette.dealer.balance(),"dealer","roulette"));
        }
        return roulette.payout;
    }
    public void clear(){
        wagerList.clear();
    }
}
