package com.tarantula.platform;

import com.icodesoftware.OnAccess;


public class OnApplicationHeader extends ResponseHeader implements OnAccess {

    protected String tournamentId;

    protected String systemId;

    protected String typeId;

    protected long stub;

    protected String ticket;




    public String systemId() {
        return systemId==null?Long.toString(this.distributionId):systemId;
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
