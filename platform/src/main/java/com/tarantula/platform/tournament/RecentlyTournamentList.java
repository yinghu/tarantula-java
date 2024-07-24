package com.tarantula.platform.tournament;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.FIFOBuffer;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;


public class RecentlyTournamentList extends RecoverableObject {



    private FIFOBuffer<Long> tournamentIndex;

    private int size;

    public RecentlyTournamentList(){
    }



    private RecentlyTournamentList(String name,int size){
        this();
        this.name = name;
        this.size = size;
        tournamentIndex = new FIFOBuffer<>(size,new Long[size]);
        fill();
    }

    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }
    public int getClassId() {
        return TournamentPortableRegistry.RECENTLY_TOURNAMENT_LIST_CID;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        //buffer.writeUTF8(name);
        buffer.writeInt(size);
        Long[] ids = tournamentIndex.list(new Long[size]);
        for(int i=0;i<size;i++){
            Long id = ids[i];
            buffer.writeLong(id);
        }
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        //name = buffer.readUTF8();
        int sz = buffer.readInt();
        if(size==0) size = sz;
        tournamentIndex = new FIFOBuffer<>(size,new Long[size]);
        fill();
        for(int i=0;i<sz;i++){
            tournamentIndex.push(buffer.readLong());
        }
        return true;
    }

    public Long[] pop(){
        return tournamentIndex.list(new Long[size]);
    }

    public void push(TournamentManager tournamentManager){
        tournamentIndex.push(tournamentManager.distributionId());
    }


    public static RecentlyTournamentList lookup(DataStore dataStore,String type,int size){
        RecentlyTournamentList recentlyTournamentList = new RecentlyTournamentList(type,size);
        dataStore.createIfAbsent(recentlyTournamentList,true);
        recentlyTournamentList.dataStore(dataStore);
        return recentlyTournamentList;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        Long[] ids = pop();
        JsonArray arrs = new JsonArray();
        for(Long i : ids){
            arrs.add(i==null? "0": i.toString());
        }
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("size",size);
        jsonObject.add("index",arrs);
        return jsonObject;
    }

    @Override
    public boolean readKey(Recoverable.DataBuffer buffer){
        name = buffer.readUTF8();
        return true;
    }
    @Override
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(name==null) return false;
        buffer.writeUTF8(name);
        return true;
    }
    public Key key(){
        return new NaturalKey(this.name);
    }

    private void fill(){
        for(int i=0;i<size;i++){
            tournamentIndex.push(0L);
        }
    }
}
