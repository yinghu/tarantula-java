package com.tarantula.game.casino;


import com.tarantula.game.GameRecoverableRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yinghu lu on 1/11/2019.
 */
public class Seat extends BetLine {


    public boolean bank;
    public boolean occupied;
    public boolean dealing;
    public boolean wagered;
    public boolean onTurn;

    public List<BetLine> wagerList = new ArrayList();

    public Seat(){
        this.vertex ="Seat";
        this.label = "BJS";
        this.onEdge = true;
    }
    public Seat(String systemId,int index,boolean occupied,double balance,String name,String label){
        this.systemId = systemId;
        this.subscript = index;
        this.occupied = occupied;
        this.balance = balance;
        this.name = name;
        this.label = label;
    }
    public Seat(String systemId,double balance){
        this.systemId = systemId;
        this.balance = balance;
    }
    public Seat(int index){
        this();
        this.subscript = index;
    }
    public Seat(int index, boolean bank){
        this();
        this.subscript = index;
        this.bank = bank;
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
    public boolean asPlayer(){
        return (occupied&&systemId!=null);
    }
    public boolean asPlayer(String systemId){
        return asPlayer()&&this.systemId.equals(systemId);
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
        return "Seat ["+oid+"/"+subscript+"/"+balance+"/"+occupied+"]";
    }
    public Seat setup(){
        return this;
    }
}
