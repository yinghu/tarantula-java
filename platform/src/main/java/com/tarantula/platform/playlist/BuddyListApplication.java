package com.tarantula.platform.playlist;

import com.tarantula.*;
import com.tarantula.platform.*;
import com.tarantula.platform.util.PlayListContextSerializer;

/**
 * Updated by yinghu on 4/24/2018
 */
public class BuddyListApplication extends TarantulaApplicationHeader{

    private int myListSize;
    private DataStore buddyDataStore;

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        OnAccess req = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
        if(session.action().equals("onList")){
            session.write(builder.create().toJson(this.onBuddyList(session.systemId())).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onConnect")){
            OnPlay onLink = new OnBuddy(req.systemId());
            this.onBuddyList(session.systemId(),onLink);
            session.write(this.builder.create().toJson(new ResponseHeader("onConnect","connection linked",true)).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onAccept")){
            OnPlay onLink = new OnBuddy(req.systemId());
            this.onBuddyList(session.systemId(),onLink);
            session.write(this.builder.create().toJson(new ResponseHeader("onAccept","connection linked",true)).getBytes(),this.descriptor.responseLabel());
        }
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        builder.registerTypeAdapter(PlayListContext.class,new PlayListContextSerializer());
        Configuration cnf = this.context.configuration("playlist");
        this.myListSize = Integer.parseInt(cnf.property("buddyListSize"));
        this.buddyDataStore = this.context.dataStore("buddyList");
        this.context.log("Play list application started",OnLog.INFO);
    }

    private void onBuddyList(String systemId,OnPlay link){
        BuddyList buddyList = this._buddyList(systemId);
        if(buddyList.onPlay(link)){
            link.owner(buddyList.distributionKey());
            link.onEdge(true);
            buddyDataStore.create(link);
        }

    }
    private BuddyList _buddyList(String systemId){
        BuddyList _mlist = new BuddyList(systemId);
        _mlist.distributionKey(systemId);
        _mlist.size(this.myListSize);
        if(!buddyDataStore.createIfAbsent(_mlist,true)){
            buddyDataStore.list(new OnBuddyQuery(_mlist.distributionKey()),(b)->{
                _mlist.onPlay(b);
                return true;
            });
        }
        return _mlist;
    }
    private PlayListContext onBuddyList(String systemId){
        PlayListContext ftx = new PlayListContext();
        ftx.successful(true);
        ftx.myPlayList = this._buddyList(systemId);
        return ftx;
    }

}
