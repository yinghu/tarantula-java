package com.tarantula.platform.tournament;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Tournament;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

public class TournamentContext extends ResponseHeader {

    private List<Tournament> itemList;
    private Tournament.RaceBoard raceBoard;
    private Tournament.RaceBoard myRaceBoard;

    public TournamentContext(boolean successful, String message, List<Tournament> itemList){
        this.successful = successful;
        this.message = message;
        this.itemList = itemList;
    }

    public TournamentContext(Tournament.RaceBoard raceBoard, Tournament.RaceBoard myRaceBoard){
        this.successful = true;
        this.raceBoard = raceBoard;
        this.myRaceBoard = myRaceBoard;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",this.successful);
        if(!successful){
            jsonObject.addProperty("Message",message);
            return jsonObject;
        }
        if(itemList!=null){
            JsonArray alist = new JsonArray();
            itemList.forEach((v)->{
                alist.add(v.toJson());
            });
            jsonObject.add("_tournamentList",alist);
        }
        if(raceBoard!=null){
            jsonObject.add("_raceBoard",raceBoard.toJson());
        }
        if(myRaceBoard!=null){
            jsonObject.add("_myRaceBoard",myRaceBoard.toJson());
        }
        return jsonObject;
    }

    @Override
    public String toString(){
        return toJson().toString();
    }
}
