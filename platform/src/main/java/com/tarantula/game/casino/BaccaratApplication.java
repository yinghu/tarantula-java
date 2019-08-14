package com.tarantula.game.casino;

import com.tarantula.*;
import com.tarantula.game.CommandResponse;
import com.tarantula.game.GameApplication;
import com.tarantula.game.GameStatisticsEntry;
import com.tarantula.game.SessionIdle;
import com.tarantula.game.casino.baccarat.*;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.presence.PresencePortableRegistry;

/**
 * Created by yinghu lu on 11/24/2018.
 */
public class BaccaratApplication extends GameApplication {

    private Baccarat baccarat;

    @Override
    public void initialize(Session session) throws Exception {
        session.joined(true);
        this.baccarat.join(session.systemId());
        session.write(this.builder.create().toJson(baccarat.setup()).getBytes(),this.descriptor.responseLabel());
    }

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onDeal")){
            ResponseHeader resp = new ResponseHeader("onDeal");
            resp.successful(this.baccarat.onAction(session.systemId()));
            session.write(this.builder.create().toJson(resp).getBytes(),"baccarat");
        }
        else if(session.action().equals("onSeat")){
            OnAccess acc = builder.create().fromJson(new String(payload),OnAccess.class);
            ResponseHeader resp = new ResponseHeader("onSeat");
            resp.successful(this.baccarat.onSeat(acc.stub(),session.systemId()));
            session.write(builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onWager")){
            OnAccess acc = builder.create().fromJson(new String(payload),OnAccess.class);
            float x = Float.parseFloat(acc.header("x"));
            float y = Float.parseFloat(acc.header("y"));
            int ix = Integer.parseInt(acc.header("index"));
            CommandResponse resp = new CommandResponse("onWager",true);
            resp.x = x;
            resp.y = y;
            resp.subscript = ix;
            resp.stub(acc.stub());
            resp.entryCost(acc.entryCost());
            resp.owner(session.systemId());
            this.baccarat.onWager(resp);
            session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onDealer")){
            CommandResponse resp = new CommandResponse("onDealer",true);
            resp.owner(session.systemId());
            this.baccarat.onDealer(resp);
            session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("offDealer")){
            CommandResponse resp = new CommandResponse("offDealer",true);
            resp.owner(session.systemId());
            this.baccarat.offDealer(resp);
            session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onLeave")){
            this.onTimeout(session);
            session.write(payload,"baccarat");
        }
        else if(session.action().equals("onStream")){
            this.onStream(session);
        }
        else if(session.action().equals("onPing")){
            session.write(payload,this.descriptor.responseLabel());
        }
    }

    @Override
    public void onTimeout(Session session) {
        this.baccarat.leave(session.systemId());
        super.onTimeout(session);
    }
    @Override
    public void onIdle(Session session){
        this.uQueue.offer(new SessionIdle("baccarat",session.systemId(),session.stub()));
    }
    public void onStatistics(String key,double value,int index){
        super.onStatistics(key,value,index);
        GameStatisticsEntry gameStatisticsEntry = this.baccarat.onStatistics[index];
        gameStatisticsEntry.value = this.statistics.value(key,0);
        this.uQueue.offer(gameStatisticsEntry);
    }
    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.builder.registerTypeAdapter(Baccarat.class,new BaccaratSerializer());
        this.builder.registerTypeAdapter(Deck.class,new DeckSerializer());
        this.builder.registerTypeAdapter(BaccaratBankerHand.class, new BaccaratBankerHandSerializer());
        this.builder.registerTypeAdapter(BaccaratPlayerHand.class, new BaccaratPlayerHandSerializer());
        this.builder.registerTypeAdapter(Baccarat.WagerTurn.class,new BaccaratTurnSerializer());
        this.builder.registerTypeAdapter(Baccarat.BankerTurn.class,new BaccaratTurnSerializer());
        this.builder.registerTypeAdapter(Baccarat.PlayerTurn.class,new BaccaratTurnSerializer());
        this.builder.registerTypeAdapter(Baccarat.PayoutTurn.class,new BaccaratTurnSerializer());
        this.builder.registerTypeAdapter(Baccarat.WagerTurn.class,new BaccaratTurnSerializer());
        Configuration cfg = this.context.configuration(this.descriptor.configurationType());
        this.baccarat = new Baccarat();
        this.baccarat.instanceId(this.context.onRegistry().distributionKey());
        this.baccarat.name("Baccarat");
        this.baccarat.entryCost(this.descriptor.entryCost());
        this.baccarat.dealerSeatFee = Double.parseDouble(cfg.property("dealerSeatFee"));
        this.baccarat.seats = Integer.parseInt(cfg.property("seats"));
        this.baccarat.deckSize = Integer.parseInt(cfg.property("deckSize"));
        this.baccarat.minWager = Double.parseDouble(cfg.property("minWager"));
        this.baccarat.maxWager = Double.parseDouble(cfg.property("maxWager"));
        this.baccarat.header = this.baccarat;
        this.baccarat.betLineListener = this;
        this.baccarat.pendingQueue = this.uQueue;
        this.baccarat.onStatistics = new GameStatisticsEntry[6];
        this.statistics = this.context.onStatistics();
        this.baccarat.onStatistics[0] = new GameStatisticsEntry("onStatistics", "baccarat", "totalWagerBanker", this.statistics.value("totalWagerBanker", 0), 0);
        this.baccarat.onStatistics[1] = new GameStatisticsEntry("onStatistics", "baccarat", "totalWagerPlayer", this.statistics.value("totalWagerPlayer", 0), 1);
        this.baccarat.onStatistics[2] = new GameStatisticsEntry("onStatistics", "baccarat", "totalWagerTie", this.statistics.value("totalWagerTie", 0), 2);

        this.baccarat.onStatistics[3] = new GameStatisticsEntry("onStatistics", "baccarat", "totalPayoutBanker", this.statistics.value("totalPayoutBanker", 0), 3);
        this.baccarat.onStatistics[4] = new GameStatisticsEntry("onStatistics", "baccarat", "totalPayoutPlayer", this.statistics.value("totalPayoutPlayer", 0), 4);
        this.baccarat.onStatistics[5] = new GameStatisticsEntry("onStatistics", "baccarat", "totalPayoutTie", this.statistics.value("totalPayoutTie", 0), 5);

        this.baccarat.start();
        this.context.schedule(this);
        this.context.schedule(baccarat);
        this.context.registerRecoverableListener(new PresencePortableRegistry()).addRecoverableFilter(PresencePortableRegistry.ON_BALANCE_CID,(t)->{
            //this.context.log("<><><>"+t.toString(),OnLog.WARN);
            OnBalance ob = (OnBalance)t;
            this.context.onRegistry().transact(t.owner(),ob.balance());
            this.baccarat.onBalance(t.owner());
        });
        this.context.log("Baccarat application started ["+descriptor.name()+"]", OnLog.INFO);
    }
}
