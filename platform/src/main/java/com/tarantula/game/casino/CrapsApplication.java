package com.tarantula.game.casino;

import com.tarantula.*;
import com.tarantula.game.*;
import com.tarantula.game.casino.craps.*;
import com.tarantula.platform.presence.PresencePortableRegistry;

/**
 * updated by yinghu lu on 4/22/2019.
 */
public class CrapsApplication extends GameApplication {

    private Craps craps;

    @Override
    public void initialize(Session session) throws Exception {
        session.joined(true);
        craps.onJoin(session.systemId());
        session.write(this.builder.create().toJson(craps.setup()).getBytes(),this.descriptor.responseLabel());
    }

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onRoll")){
            CommandResponse resp = new CommandResponse("onRoll",false);
            resp.successful(craps.onRoll());
            session.write(builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onWager")){
            OnAccess acc = builder.create().fromJson(new String(payload),OnAccess.class);
            float x = Float.parseFloat(acc.header("x"));
            float y = Float.parseFloat(acc.header("y"));
            int ix = Integer.parseInt(acc.header("index"));
            CommandResponse resp = new CommandResponse("onWager",true);
            resp.code(craps.onWager(session.systemId(),acc.stub(),acc.entryCost(),x,y,ix));
            session.write(this.builder.create().toJson(resp).getBytes(),"craps");
        }
        else if(session.action().equals("onDealer")){
            CommandResponse resp = new CommandResponse("onDealer",true);
            resp.owner(session.systemId());
            craps.onDealer(resp);
            session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("offDealer")){
            CommandResponse resp = new CommandResponse("onDealer",true);
            resp.code(craps.offDealer(session.systemId()));
            session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onStream")){
            this.onStream(session);
        }
        else if(session.action().equals("onLeave")){
            this.craps.onLeave(session.systemId());
            this.onTimeout(session);//kick off stream session
            session.write(payload,"craps");
        }
    }
    @Override
    public void onTimeout(Session session) {
        this.craps.onLeave(session.systemId());
        super.onTimeout(session);
    }

    @Override
    public void onIdle(Session session){
        this.uQueue.offer(new SessionIdle("craps",session.systemId(),session.stub()));
    }
    public void onStatistics(String key,double value,int index){
        super.onStatistics(key,value,index);
        GameStatisticsEntry gameStatisticsEntry = this.craps.onStatistics[index];
        gameStatisticsEntry.value = this.statistics.value(key,0);
        this.uQueue.offer(gameStatisticsEntry);
    }
    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.builder.registerTypeAdapter(Craps.class,new CrapsSerializer());
        this.builder.registerTypeAdapter(Puck.class,new PuckSerializer());
        this.builder.registerTypeAdapter(DiceStop.class,new DiceStopSerializer());
        Configuration cfg = this.context.configuration(this.descriptor.configurationType());
        this.craps = new Craps();
        this.craps.instanceId(this.context.onRegistry().distributionKey());
        this.craps.name(this.descriptor.name());
        this.craps.entryCost(this.descriptor.entryCost());
        this.craps.tournamentEnabled(this.context.onRegistry().tournamentEnabled());
        this.craps.dealerSeatFee = Double.parseDouble(cfg.property("dealerSeatFee"));
        this.craps.seats = Integer.parseInt(cfg.property("seats"));
        this.craps.minWager = Double.parseDouble(cfg.property("minWager"));
        this.craps.maxWager = Double.parseDouble(cfg.property("maxWager"));
        this.craps.betLineListener = this;
        this.craps.pendingQueue = this.uQueue;
        this.statistics = this.context.onStatistics();
        this.craps.onStatistics = new GameStatisticsEntry[5];
        this.craps.onStatistics[0]=new GameStatisticsEntry("onStatistics","craps","totalPassLine",this.statistics.value("totalPassLine",0),0);
        this.craps.onStatistics[1]=new GameStatisticsEntry("onStatistics","craps","totalNotPassLine",this.statistics.value("totalNotPassLine",0),1);
        this.craps.onStatistics[2]=new GameStatisticsEntry("onStatistics","craps","totalWager",this.statistics.value("totalWager",0),2);
        this.craps.onStatistics[3]=new GameStatisticsEntry("onStatistics","craps","totalPayout",this.statistics.value("totalPayout",0),3);
        this.craps.onStatistics[4]=new GameStatisticsEntry("onStatistics","craps","totalRounds",this.statistics.value("totalRounds",0),4);
        this.context.registerRecoverableListener(new PresencePortableRegistry()).addRecoverableFilter(PresencePortableRegistry.ON_BALANCE_CID,(t)->{
            //this.context.log("<><><>"+t.toString(),OnLog.WARN);
            OnBalance ob = (OnBalance)t;
            this.context.onRegistry().transact(t.owner(),ob.balance());
            this.craps.onBalance(t.owner());
        });
        this.craps.start();
        this.context.schedule(this);
        this.context.schedule(this.craps);
        this.craps.betLines.forEach((k,v)->{
            this.builder.registerTypeAdapter(v.getClass(),new BetLineSerializer());
        });
        this.context.log("Craps application started ["+descriptor.name()+"]", OnLog.INFO);
    }
}
