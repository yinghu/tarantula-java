package com.tarantula.game.casino.blackjack;


import com.tarantula.game.GameRecoverableRegistry;
import com.tarantula.game.casino.CashInBalance;
import com.tarantula.game.casino.Seat;

import java.util.Map;

/**
 * Created by yinghu lu on 11/26/2018.
 */
public class BlackJackSeat extends Seat {

    public BlackJackHand[] hands;
    public BlackJack blackJack;
    public BlackJackSeat(){
        super();
        this.name = "hand";
        this.label = "blackjack";
    }
    public BlackJackSeat(int index){
        super(index);
        this.name = "hand";
        this.label = "blackjack";
    }
    public BlackJackSeat(int index, boolean bank){
        super(index,bank);
        this.name = "hand";
        this.label = "blackjack";
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",this.subscript);
        this.properties.put("2",this.occupied);
        this.properties.put("3",this.dealing);
        this.properties.put("4",this.wagered);
        this.properties.put("5",this.balance);
        this.properties.put("6",this.bank);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.subscript = ((Number)properties.get("1")).intValue();
        this.occupied =(boolean)properties.get("2");
        this.dealing =(boolean)properties.get("3");
        this.wagered =(boolean)properties.get("4");
        this.balance =((Number)properties.get("5")).doubleValue();
        this.bank =(boolean)properties.get("6");
    }
    public double payout(double odd){
        blackJack.payout = 0;
        wagerList.forEach((w)->{
            double _pay = w.wager()*odd;
            if(w.systemId()!=null){//skip robot wager
                Seat onTable = blackJack.indexing.get(w.systemId());
                if(onTable!=null){
                    this.blackJack.betLineListener.onPayout(w.systemId(),_pay);
                    this.blackJack.pendingQueue.offer(new CashInBalance(w.systemId(),onTable.subscript,this.blackJack.betLineListener.balance(w.systemId()),"balance","blackjack"));
                    onTable.wagered = false;
                    onTable.balance(blackJack.betLineListener.balance(w.systemId()));
                }
            }
            blackJack.payout += _pay;
        });
        return blackJack.payout;
    }
    @Override
    public int getFactoryId() {
        return GameRecoverableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GameRecoverableRegistry.BLACKJACK_SEAT_CID;
    }
    @Override
    public String toString(){
        return "Seat ["+oid+"/"+index+"/"+balance+"/"+occupied+"]";
    }
}
