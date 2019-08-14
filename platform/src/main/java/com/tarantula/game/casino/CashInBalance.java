package com.tarantula.game.casino;

import com.tarantula.game.GameComponent;

/**
 * Created by yinghu lu on 3/1/2019.
 */
public class CashInBalance extends GameComponent {

    public CashInBalance(){}
    public CashInBalance(String systemId,int subscript,double balance,String name,String label){
        this.systemId = systemId;
        this.subscript = subscript;
        this.balance = balance;
        this.name = name;
        this.label = label;
    }
}
