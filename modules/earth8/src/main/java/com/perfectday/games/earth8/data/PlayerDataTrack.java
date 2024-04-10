package com.perfectday.games.earth8.data;

import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.util.RecoverableObject;
import com.perfectday.games.earth8.Earth8PortableRegistry;

public class PlayerDataTrack extends RecoverableObject implements OnAccess {

    public static final String LABEL = "tournament";

    public long tournamentId;

    public PlayerDataTrack(long tournamentId){
        this.onEdge = true;
        this.label = LABEL;
        this.tournamentId = tournamentId;
    }

    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.PLAYER_TOURNAMENT_TRACK_CID;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(tournamentId);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        tournamentId = buffer.readLong();
        return true;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("TournamentId",tournamentId);
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
