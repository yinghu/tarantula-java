package com.perfectday.games.earth8.inbox;

import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.util.RecoverableObject;
import com.perfectday.games.earth8.Earth8PortableRegistry;

public class PlayerAction extends RecoverableObject implements OnAccess {

    public static final String LABEL = "inbox";
    public boolean completed;
    public PlayerAction(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public PlayerAction(String name,boolean completed){
        this();
        this.name = name;
        this.completed = completed;
    }
    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return Earth8PortableRegistry.PLAYER_ACTION_CID;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeBoolean(completed);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        name = buffer.readUTF8();
        completed = buffer.readBoolean();
        return true;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(name,completed);
        return jsonObject;
    }
    @Override
    public String systemId() {
        return null;
    }

    @Override
    public void systemId(String systemId) {

    }

    @Override
    public long stub() {
        return 0;
    }

    @Override
    public void stub(long stub) {

    }

    @Override
    public long tournamentId() {
        return 0;
    }

    @Override
    public void tournamentId(long tournamentId) {

    }

    @Override
    public String typeId() {
        return null;
    }

    @Override
    public void typeId(String typeId) {

    }

    @Override
    public String ticket() {
        return null;
    }

    @Override
    public void ticket(String ticket) {

    }

    @Override
    public String command() {
        return null;
    }

    @Override
    public void command(String command) {

    }

    @Override
    public int code() {
        return 0;
    }

    @Override
    public void code(int code) {

    }

    @Override
    public String message() {
        return null;
    }

    @Override
    public void message(String message) {

    }

    @Override
    public boolean successful() {
        return false;
    }

    @Override
    public void successful(boolean successful) {

    }
}
