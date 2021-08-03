package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;


public class TournamentJoinIndex extends TournamentInstanceHeader {

    public TournamentJoinIndex(){

    }

    @Override
    public String id() {
        return distributionKey();
    }


    @Override
    public void update(String s, Tournament.OnEntry onEntry) {

    }


    @Override
    public Tournament.Entry join(String systemId) {
        Tournament.Entry _e = new TournamentEntry(systemId);
        //this.entryIndex.putIfAbsent(_e.systemId(),_e);
        _e.owner(id());
        return _e;
    }

    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_JOIN_INDEX_CID;
    }

}
