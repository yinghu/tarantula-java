package com.tarantula.platform.leveling;

import com.tarantula.*;
import com.tarantula.Level;
import com.tarantula.platform.*;
import com.tarantula.platform.util.LevelContextSerializer;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * Update by yinghu on 8/23/29
 */
public class XPLevelApplication extends TarantulaApplicationHeader{

    private XPLevelRule rule;
    private DataStore _dataStore;
    private Set<XPHeader> _headers;
    private LeaderBoardServiceProvider leaderBoardServiceProvider;
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        OnAccess acc = builder.create().fromJson(new String(payload),OnAccess.class);
        String header = acc.header("header");
        String category = acc.header("category");
        XPLevel l = (XPLevel) this._load(session.systemId());
        rule.onLevel(l);
        LevelContext lcx = new LevelContext();
        if(header==null){
            lcx.level = l;
            lcx.headers = _headers;
        }else{
            lcx.xp = l.list(header,category);
        }
        session.write(this.builder.create().toJson(lcx).getBytes(),this.descriptor.responseLabel());
    }

    @Override
   public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        _headers = new CopyOnWriteArraySet<>();
        Configuration xp = this.context.configuration("xp");
        this.leaderBoardServiceProvider = this.context.serviceProvider(this.context.configuration("setup").property("leaderBoardProvider"));
        this._dataStore = this.context.dataStore("level");
        this.rule = new XPLevelRule(xp);
        this.rule.start();
        this.builder.registerTypeAdapter(LevelContext.class,new LevelContextSerializer());
        this.context.registerRecoverableListener(new LevelingPortableRegistry()).addRecoverableFilter(LevelingPortableRegistry.ON_STATS_CID,(t)->{
            OnStatistics statistics = (OnStatistics)t;
            Level l = this._loadLevel(statistics.owner());
            this.executeOnLevel(l,statistics);
        });
        this.context.log("Level application started on ["+descriptor.tag()+"]", OnLog.INFO);
    }

    private Level _load(String systemId){
        Level _px = new XPLevel();
        _px.distributionKey(systemId);
        _px.onDailyGainReset();
        _px.onWeeklyGainReset();
        _px.onMonthlyGainReset();
        _px.onYearlyGainReset();
        this._dataStore.createIfAbsent(_px,true);
        this._dataStore.list(new XPQuery(_px.distributionKey())).forEach((x)->{
            _px.xp(x);
        });
        return _px;
    }
    private Level _loadLevel(String systemId){
        Level _px = new XPLevel();
        _px.distributionKey(systemId);
        _px.onDailyGainReset();
        _px.onWeeklyGainReset();
        _px.onMonthlyGainReset();
        _px.onYearlyGainReset();
        this._dataStore.createIfAbsent(_px,true);
        return _px;
    }
    private void executeOnXP(Level l,String systemId,XP _xp,Statistics.Entry xlist){
        if(l.onDailyGainReset()){
            _xp.reset(LeaderBoard.DAILY);//daily reset
        }
        if(l.onWeeklyGainReset()){
            _xp.reset(LeaderBoard.WEEKLY);//weekly reset
        }
        if(l.onMonthlyGainReset()){
            _xp.reset(LeaderBoard.MONTHLY);//monthly reset
        }
        if(l.onYearlyGainReset()){
            _xp.reset(LeaderBoard.YEARLY);//yearly reset
        }
        LeaderBoard.Entry[] entries = new LeaderBoard.Entry[5];
        entries[0] = _xp.totalGain(xlist.value());
        entries[1] = _xp.dailyGain(xlist.value());
        entries[2] = _xp.weeklyGain(xlist.value());
        entries[3] = _xp.monthlyGain(xlist.value());
        entries[4] = _xp.yearlyGain(xlist.value());
        this._dataStore.update(_xp);
        this.leaderBoardServiceProvider.onLeaderBoard(systemId,entries);
    }
    private void executeOnLevel(Level l,OnStatistics delta){
        l.levelXP(delta.xpDelta());
        this.rule.execute(l);
        _dataStore.update(l);
        //execute XP
        if(delta.name()!=null){
            for(Statistics.Entry entry : delta.entryList()){
                _headers.add(new XPHeader(delta.name(),entry.name()));
                XP xp = new XPGain(l.distributionKey(),l.bucket(),l.oid(),delta.name(),entry.name());
                this.context.log(xp.key().asString(),OnLog.INFO);
                _dataStore.createIfAbsent(xp,true);
                this.executeOnXP(l,delta.owner(),xp,entry);
            }
        }
    }
}
