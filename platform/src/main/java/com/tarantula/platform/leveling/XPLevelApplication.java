package com.tarantula.platform.leveling;

import com.tarantula.*;
import com.tarantula.Level;
import com.tarantula.platform.*;
import com.tarantula.platform.presence.PresenceContext;
import com.tarantula.platform.util.PresenceContextSerializer;


/**
 * Update by yinghu on 8/23/19
 */
public class XPLevelApplication extends TarantulaApplicationHeader{

    private XPLevelRule rule;
    private DataStore _dataStore;
    private LeaderBoardServiceProvider leaderBoardServiceProvider;
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        OnAccess acc = builder.create().fromJson(new String(payload),OnAccess.class);
        PresenceContext lcx = new PresenceContext();
        if(session.action().equals("onLevel")){
            XPLevel l = (XPLevel)this._load(session.systemId());
            lcx.level = l;
            session.write(this.builder.create().toJson(lcx).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onXP")){
            String header = acc.header("header");
            String category = acc.header("category");
            XPLevel l = (XPLevel) this._load(session.systemId());
            lcx.xp = l.list(header,category);
            session.write(this.builder.create().toJson(lcx).getBytes(),this.descriptor.responseLabel());
        }
        else{
            throw new RuntimeException("operation not supported ["+session.action()+"]");
        }
    }

    @Override
   public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        Configuration xp = this.context.configuration("xp");
        this.leaderBoardServiceProvider = this.context.serviceProvider(this.context.configuration("setup").property("leaderBoardProvider"));
        this._dataStore = this.context.dataStore("level");
        this.rule = new XPLevelRule(xp);
        this.rule.start();
        this.builder.registerTypeAdapter(PresenceContext.class,new PresenceContextSerializer());
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
        this.rule.onLevel((XPLevel)l);
        _dataStore.update(l);
        //execute XP
        if(delta.name()!=null){
            for(Statistics.Entry entry : delta.entryList()){
                XP xp = new XPGain(l.distributionKey(),l.bucket(),l.oid(),delta.name(),entry.name());
                //this.context.log(xp.key().asString(),OnLog.INFO);
                _dataStore.createIfAbsent(xp,true);
                this.executeOnXP(l,delta.owner(),xp,entry);
            }
        }
    }
}
