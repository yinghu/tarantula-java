package com.tarantula.platform.tournament;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TournamentRaceBoard extends RecoverableObject implements Tournament.RaceBoard, Portable {

    private List<Tournament.Entry> onBoard = new ArrayList<>();
    private int size;
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        Portable[] entries;
        synchronized (onBoard){
            entries = new Portable[size];
            for(int i=0;i<size;i++){
                entries[i]=(Portable)onBoard.get(i);
            }
        }
        portableWriter.writePortableArray("1",entries);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        for(Portable p : portableReader.readPortableArray("1")){
            onBoard.add((Tournament.Entry)p);
        }
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

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        JsonArray plist = new JsonArray();
        onBoard.forEach((v)->{
            plist.add(v.toJson());
        });
        jsonObject.add("board",plist);
        return jsonObject;
    }
}
