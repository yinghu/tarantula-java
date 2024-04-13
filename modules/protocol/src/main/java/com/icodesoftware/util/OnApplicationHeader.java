package com.icodesoftware.util;

import com.icodesoftware.OnAccess;


public class OnApplicationHeader extends ResponseHeader implements OnAccess {

    protected long tournamentId;

    protected long systemId;

    protected String typeId;

    protected long stub;

    protected String ticket;



    public long systemId() {
        return systemId;
    }


    public void systemId(long systemId) {
        this.systemId = systemId;
    }


    public long tournamentId() {
        return this.tournamentId;
    }


    public void tournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String typeId(){
        return this.typeId;
    }
    public void typeId(String typeId){
        this.typeId = typeId;
    }

    public long stub(){return this.stub;}
    public void stub(long stub){ this.stub = stub;}

    public String ticket(){
        return this.ticket;
    }
    public void ticket(String ticket){
        this.ticket = ticket;
    }
}
