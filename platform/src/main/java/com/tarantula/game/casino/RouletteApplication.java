package com.tarantula.game.casino;

import com.tarantula.*;
import com.tarantula.game.CommandResponse;
import com.tarantula.game.GameApplication;
import com.tarantula.game.GameStatisticsEntry;
import com.tarantula.game.casino.roulette.Roulette;
import com.tarantula.game.casino.roulette.RouletteSerializer;
import com.tarantula.game.casino.roulette.WheelStop;
import com.tarantula.game.casino.roulette.WheelStopSerializer;
import com.tarantula.platform.presence.PresencePortableRegistry;


/**
 * Created by yinghu lu on 11/24/2018.
 */
public class RouletteApplication extends GameApplication {

    private Roulette roulette;

    @Override
    public void initialize(Session session) throws Exception {
        session.joined(true);
        roulette.onJoin(session.systemId());
        session.write(this.builder.create().toJson(roulette.setup()).getBytes(),this.descriptor.responseLabel());
    }

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onWheel")){
            CommandResponse resp = new CommandResponse("onWheel",false);
            resp.successful(roulette.onWheel(session.systemId()));
            session.write(this.builder.create().toJson(resp).getBytes(),"roulette");
        }
        else if(session.action().equals("onWager")){
            OnAccess acc = builder.create().fromJson(new String(payload),OnAccess.class);
            CommandResponse resp = new CommandResponse("onWager",true);
            int ix = Integer.parseInt(acc.header("index"));
            resp.code(roulette.onWager(session.systemId(),acc.stub(),acc.entryCost(),ix));
            session.write(this.builder.create().toJson(resp).getBytes(),"roulette");
        }
        else if(session.action().equals("onPayout")){
           roulette.onPayout();
           session.write(payload,"roulette");
        }
        else if(session.action().equals("onDealer")){
            CommandResponse resp = new CommandResponse("onDealer",true);
            resp.owner(session.systemId());
            roulette.onDealer(resp);
            session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("offDealer")){
            CommandResponse resp = new CommandResponse("offDealer",true);
            resp.owner(session.systemId());
            roulette.offDealer(resp);
            session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onStream")){
            this.onStream(session);
        }
        else if(session.action().equals("onLeave")){
            this.roulette.onLeave(session.systemId());
            this.onTimeout(session);//kick off stream session
            session.write(payload,"roulette");
        }
    }
    public void onStatistics(String key,double value,int index){
        super.onStatistics(key,value,index);
        GameStatisticsEntry gameStatisticsEntry = this.roulette.onStatistics[index];
        gameStatisticsEntry.value = this.statistics.value(key,0);
        this.uQueue.offer(gameStatisticsEntry);
    }
    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.builder.registerTypeAdapter(Roulette.class,new RouletteSerializer());
        this.builder.registerTypeAdapter(WheelStop.class,new WheelStopSerializer());
        Configuration cfg = this.context.configuration(this.descriptor.configurationType());
        this.roulette = new Roulette();
        this.roulette.instanceId(this.context.onRegistry().distributionKey());
        this.roulette.name(this.descriptor.name());
        this.roulette.entryCost(this.descriptor.entryCost());
        this.roulette.dealerSeatFee = Double.parseDouble(cfg.property("dealerSeatFee"));
        this.roulette.seats = Integer.parseInt(cfg.property("seats"));
        this.roulette.minWager = Double.parseDouble(cfg.property("minWager"));
        this.roulette.maxWager = Double.parseDouble(cfg.property("maxWager"));
        this.roulette.betLineListener = this;
        this.roulette.pendingQueue = this.uQueue;
        this.statistics = this.context.onStatistics();
        this.roulette.onStatistics = new GameStatisticsEntry[5];
        this.roulette.onStatistics[0]=new GameStatisticsEntry("onStatistics","roulette","totalRed",this.statistics.value("totalRed",0),0);
        this.roulette.onStatistics[1]=new GameStatisticsEntry("onStatistics","roulette","totalBlack",this.statistics.value("totalBlack",0),1);
        this.roulette.onStatistics[2]=new GameStatisticsEntry("onStatistics","roulette","totalWager",this.statistics.value("totalWager",0),2);
        this.roulette.onStatistics[3]=new GameStatisticsEntry("onStatistics","roulette","totalPayout",this.statistics.value("totalPayout",0),3);
        this.roulette.onStatistics[4]=new GameStatisticsEntry("onStatistics","roulette","totalRounds",this.statistics.value("totalRounds",0),4);
        this.context.registerRecoverableListener(new PresencePortableRegistry()).addRecoverableFilter(PresencePortableRegistry.ON_BALANCE_CID,(t)->{
            //this.context.log("<><><>"+t.toString(),OnLog.WARN);
            OnBalance ob = (OnBalance)t;
            this.context.onRegistry().transact(t.owner(),ob.balance());
            this.roulette.onBalance(t.owner());
        });
        this.roulette.start();
        this.context.schedule(this);
        this.context.schedule(this.roulette);
        this.context.log("Roulette application started ["+descriptor.name()+"]", OnLog.INFO);
    }
}
