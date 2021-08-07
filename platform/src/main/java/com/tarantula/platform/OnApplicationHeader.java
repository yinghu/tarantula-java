package com.tarantula.platform;

import com.icodesoftware.OnAccess;


public class OnApplicationHeader extends ResponseHeader implements OnAccess {


    protected String tournamentId;

    protected String systemId;
    protected String name;
    protected String typeId;

    protected int accessMode;

    protected double balance;

    protected int stub;

    protected String ticket;

    public double balance() {
        return this.balance;
    }


    public void balance(double balance) {
        this.balance = balance;
    }



    public String systemId() {
        return this.systemId;
    }


    public void systemId(String systemId) {
        this.systemId = systemId;
    }


    public String tournamentId() {
        return this.tournamentId;
    }


    public void tournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }


    public int accessMode(){
        return this.accessMode;
    }
    public void accessMode(int mode){
        this.accessMode = mode;
    }

    public String name(){
        return this.name;
    }
    public void name(String name){
        this.name = name;
    }
    public String typeId(){
        return this.typeId;
    }
    public void typeId(String typeId){
        this.typeId = typeId;
    }

    public int stub(){return this.stub;}
    public void stub(int stub){ this.stub = stub;}

    public String ticket(){
        return this.ticket;
    }
    public void ticket(String ticket){
        this.ticket = ticket;
    }
}
