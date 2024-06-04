package com.tarantula.platform.tournament;

import com.icodesoftware.DataStore;
import com.icodesoftware.util.FIFOBuffer;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;


public class RecentlyTournamentList extends RecoverableObject {

    public static final String LABEL = "recently_tournament_list";

    private FIFOBuffer<Long> tournamentIndex;

    private int size;

    public RecentlyTournamentList(){
        this.onEdge = true;
        this.label = LABEL;
    }



    public RecentlyTournamentList(String name,int size){
        this();
        this.name = name;
        this.size = size;
        tournamentIndex = new FIFOBuffer<>(size,new Long[size]);
    }

    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }
    public int getClassId() {
        return TournamentPortableRegistry.RECENTLY_TOURNAMENT_LIST_CID;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeInt(size);
        Long[] ids = tournamentIndex.list(new Long[size]);
        for(int i=0;i<size;i++){
            Long id = ids[i];
            buffer.writeLong(id==null?0:id);
        }
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        name = buffer.readUTF8();
        size = buffer.readInt();
        tournamentIndex = new FIFOBuffer<>(size,new Long[size]);
        for(int i=0;i<size;i++){
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


    public static RecentlyTournamentList lookup(DataStore dataStore,long gameClusterId, String type,int size){
        RecentlyTournamentList[] ret = new RecentlyTournamentList[]{null};
        dataStore.list(new RecentlyTournamentListQuery(gameClusterId),list->{
            if(list.name !=null && list.name.equals(type)){
                ret[0]=list;
                ret[0].dataStore = dataStore;
                return false;
            }
            return true;
        });
        if(ret[0]!=null) return ret[0];
        ret[0] = new RecentlyTournamentList(type,size);
        ret[0].ownerKey(SnowflakeKey.from(gameClusterId));
        dataStore.create(ret[0]);
        ret[0].dataStore = dataStore;
        return ret[0];
    }

}
