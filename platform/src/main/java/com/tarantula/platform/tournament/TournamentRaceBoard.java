package com.tarantula.platform.tournament;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TournamentRaceBoard extends RecoverableObject implements Tournament.RaceBoard, Portable {

    private List<Tournament.Entry> onBoard = new ArrayList<>();
    private Portable[] pending;
    private int size;
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writePortableArray("1",pending);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        for(Portable p : portableReader.readPortableArray("1")){
            onBoard.add((Tournament.Entry)p);
        }
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.TOURNAMENT_RACE_BOARD_CID;
    }
    @Override
    public int size() {
        return size;
    }

    @Override
    public List<Tournament.Entry> list() {
        return onBoard;
    }

    public void addEntry(Tournament.Entry entry){
        synchronized (onBoard){
            onBoard.add(entry);
            size++;
        }
    }
    public void reset(){
        synchronized (onBoard){
            pending = new Portable[size];
            for(int i=0;i<size;i++){
                pending[i]=(Portable)onBoard.get(i);
            }
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        Collections.sort(onBoard,new TournamentEntryComparator());
        JsonArray plist = new JsonArray();
        int[] rank = {1};
        onBoard.forEach((v)->{
            ((TournamentEntry)v).rank(rank[0]++);
            plist.add(v.toJson());
        });
        jsonObject.add("_board",plist);
        return jsonObject;
    }
}
