package com.tarantula.platform;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.OnLobby;
import com.tarantula.platform.service.cluster.PortableRegistry;
import java.io.IOException;


/**
 * Updated by yinghu lu on 7/20/19
 */
public class OnLobbyTrack extends RecoverableObject implements OnLobby {

    private String typeId;

    private boolean closed;

    public OnLobbyTrack(){

    }
    public OnLobbyTrack(String typeId,boolean closed){
        this.typeId = typeId;
        this.closed = closed;
    }

    public String typeId() {
        return this.typeId;
    }


    public void typeId(String typeId) {
        this.typeId = typeId;
    }
    public boolean closed(){
        return this.closed;
    }
    public void closed(boolean closed){
        this.closed = closed;
    }
    @Override
    public String toString(){
        return "Lobby["+typeId+"/"+closed+"]";
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("3",typeId);
        out.writeBoolean("4",this.closed);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.typeId = in.readUTF("3");
        this.closed = in.readBoolean("4");

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
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
}
