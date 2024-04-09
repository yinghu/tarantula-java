package com.perfectday.games.earth8.tournament;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.icodesoftware.OnAccess;
import com.icodesoftware.util.RecoverableObject;
import com.perfectday.games.earth8.Earth8PortableRegistry;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTournamentTrack extends RecoverableObject implements OnAccess {

    public static final String LABEL = "tournament";

    public ConcurrentHashMap<Long, Integer> tournamentIDToLevel = new ConcurrentHashMap<>();

    public PlayerTournamentTrack(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public PlayerTournamentTrack(String name){
        this();
        this.name = name;
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
        buffer.writeUTF8(name);
        buffer.writeUTF8(tournamentIDToLevel.toString());
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        name = buffer.readUTF8();
        Gson gson = new Gson();
        tournamentIDToLevel = gson.fromJson(gson.toJson(buffer.readUTF8()), new TypeToken<ConcurrentHashMap<Long, Integer>>(){}.getType());
        return true;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Name",name);
        jsonObject.addProperty("TournamentIDToLevel",tournamentIDToLevel.toString());
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
