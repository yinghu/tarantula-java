package com.tarantula.platform.playlist;

import com.tarantula.*;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.util.PlayListContextSerializer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Updated 4/24/2018 yinghu lu
 */
public class RecentlyPlayListApplication extends TarantulaApplicationHeader {

    private ConcurrentHashMap<String,PlayListContext> onRecentPlayList = new ConcurrentHashMap();
    private Set<String> availableList;
    private int recentListSize = 16;

    private DataStore dataStore;

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        OnAccess req = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
        if(session.action().equals("onRecentPlayList")){//by category
            session.write(builder.create().toJson(this.onRecentPlayList(req.header("category"))).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onAvailableRecentPlayLists")){
            PlayListContext ftx = new PlayListContext();
            ftx.successful(true);
            ftx.availableList = availableList;
            session.write(builder.create().toJson(ftx).getBytes(),this.descriptor.responseLabel());
        }
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        builder.registerTypeAdapter(PlayListContext.class,new PlayListContextSerializer());
        availableList = new ConcurrentSkipListSet<>();
        Configuration cnf = this.context.configuration("recentlyplaylist");
        this.recentListSize = Integer.parseInt(cnf.property("recentListSize"));
        this.dataStore = this.context.dataStore("buddyList");
        this.dataStore.registerRecoverableListener(new BuddyListPortableRegistry()).addRecoverableFilter(BuddyListPortableRegistry.ON_PLAY_CID,(p)->{
            OnPlay op = (OnPlay)p;
            onRecentPlayList(op.category()).recentPlayList.onPlay(op);
        });
        this.context.log("Runtime Recently Play List Application Started", OnLog.INFO);

    }

    private PlayListContext onRecentPlayList(String category){
        return this.onRecentPlayList.computeIfAbsent(category,(String k)->{
            PlayListContext ftx = new PlayListContext();
            ftx.successful(true);
            RecentPlayList recentPlayList = new RecentPlayList();
            recentPlayList.name(category);
            recentPlayList.size(this.recentListSize);
            if(this.dataStore.create(recentPlayList)){
                ftx.recentPlayList = recentPlayList;
                ftx.recentPlayList.name(category);
                ftx.recentPlayList.start();
                availableList.add(category);
            }
            return ftx;
        });
    }
}
