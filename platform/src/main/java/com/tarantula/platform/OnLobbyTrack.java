package com.tarantula.platform;


import com.icodesoftware.service.OnLobby;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.icodesoftware.util.RecoverableObject;


public class OnLobbyTrack extends RecoverableObject implements OnLobby {

    private String typeId;

    private boolean closed;

    private boolean resetEnabled;

    private String gameClusterId;

    private String subscriptionId;

    private int deployCode;

    public String configurationType(){
        return OnLobby.TYPE;
    }
    public OnLobbyTrack(){

    }
    public OnLobbyTrack(String typeId,int deployCode,boolean resetEnabled,boolean closed,String gameClusterId,String subscriptionId){
        this.typeId = typeId;
        this.deployCode  = deployCode;
        this.resetEnabled = resetEnabled;
        this.closed = closed;
        this.gameClusterId = gameClusterId;
        this.subscriptionId = subscriptionId;
    }
    public String gameClusterId(){
        return this.gameClusterId;
    }
    public String subscriptionId(){ return this.subscriptionId;}
    public String typeId() {
        return this.typeId;
    }
    public int deployCode(){
        return this.deployCode;
    }
    public boolean resetEnabled(){
        return this.resetEnabled;
    }
    public boolean closed(){
        return this.closed;
    }
    public void closed(boolean closed){
        this.closed = closed;
    }
    @Override
    public String toString(){
        return "Lobby["+typeId+"/"+resetEnabled+"/"+closed+"]["+gameClusterId+"]["+subscriptionId+"]";
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.ON_LOBBY_CID;
    }
    @Override
    public Key key() {
        return new AssociateKey(this.oid,this.label);
    }
}
