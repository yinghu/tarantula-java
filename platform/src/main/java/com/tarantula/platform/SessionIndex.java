package com.tarantula.platform;

import com.google.gson.JsonObject;


public class SessionIndex extends OnSessionTrack {

    public SessionIndex(){
       super();
    }

    public boolean write(DataBuffer buffer){
        buffer.writeInt(tournamentSlot);
        buffer.writeLong(tournamentId);
        buffer.writeDouble(tournamentScore);
        buffer.writeDouble(tournamentCredit);
        buffer.writeLong(timestamp);
        buffer.writeBoolean(disabled);
        return true;
    }
    public boolean read(DataBuffer buffer) {
        this.tournamentSlot = buffer.readInt();
        this.tournamentId = buffer.readLong();
        this.tournamentScore = buffer.readDouble();
        this.tournamentCredit = buffer.readDouble();
        this.timestamp = buffer.readLong();
        this.disabled = buffer.readBoolean();
        return true;
    }
    @Override
    public JsonObject toJson() {
        JsonObject jp = new JsonObject();
        jp.addProperty("distributionId",distributionKey());
        jp.addProperty("tournamentSlot",tournamentSlot);
        jp.addProperty("tournamentId",Long.toString(tournamentId));
        jp.addProperty("tournamentScore",tournamentScore);
        jp.addProperty("tournamentCredit",tournamentCredit);
        jp.addProperty("timestamp",Long.toString(timestamp));
        jp.addProperty("finished",disabled);
        return jp;
    }

}
