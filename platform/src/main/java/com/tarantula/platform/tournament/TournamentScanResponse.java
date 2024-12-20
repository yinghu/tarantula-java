package com.tarantula.platform.tournament;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

public class TournamentScanResponse extends ResponseHeader {

    private List<TournamentOnNode> tournamentOnNodes;

    public TournamentScanResponse(List<TournamentOnNode> tournamentOnNodes){
        this.tournamentOnNodes = tournamentOnNodes;
    }

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        JsonArray nodes = new JsonArray();
        tournamentOnNodes.forEach(node->{
            nodes.add(node.toJson());
        });
        resp.add("list",nodes);
        return resp;
    }

    @Override
    public byte[] toBinary() {
        return toJson().toString().getBytes();
    }
}
