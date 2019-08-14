package com.tarantula.game.casino.blackjack;

import com.tarantula.RecoverableFactory;
import com.tarantula.game.GameRecoverableRegistry;


/**
 * Created by yinghu on 12/29/2018.
 */
public class BlackJackSeatQuery implements RecoverableFactory<BlackJackSeat> {

    String instanceId;

    public BlackJackSeatQuery(String instanceId){
        this.instanceId = instanceId;
    }

    public BlackJackSeat create() {
        return new BlackJackSeat();
    }


    public String distributionKey() {
        return this.instanceId;
    }


    public  int registryId(){
        return GameRecoverableRegistry.BLACKJACK_SEAT_CID;
    }

    public String label(){
        return "BJS";
    }
    public boolean onEdge(){
        return true;
    }
}
