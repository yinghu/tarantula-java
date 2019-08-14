package com.tarantula.game.casino.craps;

import com.tarantula.game.casino.BetLine;
import com.tarantula.game.casino.CashInBalance;
import com.tarantula.game.casino.Seat;


/**
 * Created by yinghu lu on 1/20/2019.
 */
public class CrapsLine extends Seat {

    public Craps craps;
    public CrapsLine(int lineId,String symbol,double odd,Craps craps){
        this.name = "payout";
        this.label = "craps";
        this.stub = lineId;
        this.symbol = symbol;
        this.odd = odd;
        this.craps = craps;
    }
    public int wager(String systemId,double wager,float x,float y,int ix){
        if(systemId!=null){
            if(this.craps.betLineListener.onWager(systemId,wager)){
                Seat seat = this.craps.seatList.get(systemId);
                seat.balance(this.craps.betLineListener.balance(systemId));
                this.craps.pendingQueue.offer(new CashInBalance(systemId,seat.occupied?seat.subscript:-1,seat.balance(),"balance","craps"));
                BetLine betLine = new BetLine(stub,x,y,ix,systemId,wager);
                betLine.label("craps");
                _onWager(betLine);
                this.craps.betLineListener.onStatistics("totalWager",wager,2);
                return BetLine.ON_WAGER;
            }
            else{
                return BetLine.NO_FUND;
            }
        }
        else{
            this.craps.betLineListener.onStatistics("totalWager",wager,2);
            BetLine betLine = new BetLine(stub,x,y,ix,systemId,wager);
            betLine.label("craps");
            _onWager(betLine);
            return BetLine.ON_WAGER;
        }
    }
    private void _onWager(BetLine betLine){
        wagered = true;
        wagerList.add(betLine);
        if(this.craps.dealer.occupied){
            this.craps.betLineListener.onPayout(this.craps.dealer.systemId(),betLine.wager());
            this.craps.dealer.balance(this.craps.betLineListener.balance(this.craps.dealer.systemId()));
        }else{
            this.craps.dealer.balance(this.craps.betLineListener.onHouse(betLine.wager()));
        }
        this.craps.pendingQueue.offer(betLine);
        this.craps.pendingQueue.offer(new CashInBalance(this.craps.dealer.systemId(),-1,this.craps.dealer.balance(),"dealer","craps"));
    }
    public double payout(){
        craps.payout = 0;
        wagerList.forEach((v)->{
            double _pay = v.wager()+v.wager()*odd;
            if(v.systemId()!=null){
                craps.betLineListener.onPayout(v.systemId(),_pay);
                Seat seat = craps.seatList.get(v.systemId());
                if(seat!=null){//skip player left
                    seat.balance(craps.betLineListener.balance(v.systemId()));
                    this.craps.pendingQueue.offer(new CashInBalance(v.systemId(),seat.occupied?seat.subscript:-1,seat.balance(),"balance","craps"));
                }
            }
            craps.payout = craps.payout+_pay;
        });
        if(craps.payout>0){
            this.craps.betLineListener.onStatistics("totalPayout",craps.payout,3);
            if(craps.dealer.occupied){
                if(this.craps.betLineListener.onWager(craps.dealer.systemId(),craps.payout)){
                    //kick off
                }
                this.craps.dealer.balance(this.craps.betLineListener.balance(craps.dealer.systemId()));
            }else{
                craps.dealer.balance(craps.betLineListener.onHouse((-1)*craps.payout));
                this.craps.pendingQueue.offer(new CashInBalance(this.craps.dealer.systemId(),-1,this.craps.dealer.balance(),"dealer","craps"));
            }
            craps.pendingQueue.add(new CashInBalance(craps.dealer.systemId(),-1,craps.dealer.balance(),"dealer","craps"));
        }
        return craps.payout;
    }
    public void clear(){
        if(wagered){
            ///System.out.println("BET INDEX->"+this.index+"/"+this.symbol);
            wagerList.clear();
            this.craps.pendingQueue.offer(this);
            wagered = false;
        }
    }
}
