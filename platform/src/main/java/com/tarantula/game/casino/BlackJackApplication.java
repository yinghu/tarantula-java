package com.tarantula.game.casino;

import com.tarantula.*;
import com.tarantula.game.*;
import com.tarantula.game.casino.blackjack.*;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.presence.PresencePortableRegistry;

/**
 * Updated by yinghu lu on 4/15/2019.
 */
public class BlackJackApplication extends GameApplication{

    private BlackJack game;

     @Override
    public void initialize(Session session) throws Exception {
        session.joined(true);
        game.onJoin(session.systemId());
        if(game.tournamentEnabled()) {
            game.entryCost(this.context.onRegistry().entryCost());//overriding on tournament credits
            //game.onTournament = this.context.onRegistry().onInstance(session.systemId()).onTournament();
        }
        byte[] resp = this.builder.create().toJson(game.setup()).getBytes();
        //this.context.log(resp.length+" bytes",OnLog.WARN);
        session.write(resp,this.descriptor.responseLabel());
    }

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onStand")){
            OnAccess acc = builder.create().fromJson(new String(payload),OnAccess.class);
            if(game.onStand(session.systemId(),acc.stub())){
            }
            session.write(payload,this.descriptor.responseLabel());
        }
        else if(session.action().equals("onHit")){
            OnAccess acc = builder.create().fromJson(new String(payload),OnAccess.class);
            if(game.onHit(session.systemId(),acc.stub())){
            }
            session.write(payload,this.descriptor.responseLabel());
        }
        else if(session.action().equals("onDoubleDown")){
            OnAccess acc = builder.create().fromJson(new String(payload),OnAccess.class);
            if(game.onDoubleDown(session.systemId(),acc.stub())){
            }
            session.write(payload,this.descriptor.responseLabel());
        }
        else if(session.action().equals("onSplit")){
            OnAccess acc = builder.create().fromJson(new String(payload),OnAccess.class);
            if(game.onSplit(session.systemId(),acc.stub())){
            }
            session.write(payload,this.descriptor.responseLabel());
        }
        else if(session.action().equals("onDeal")){
           if(this.game.onDeal()){
             }
            session.write(payload,"blackjack");
        }
        else if(session.action().equals("onFaceUp")){
            this.game.onFaceUp();
             session.write(payload,this.descriptor.responseLabel());
        }
        else if(session.action().equals("onSoft17")){
            this.game.onSoft17();
            session.write(payload,this.descriptor.responseLabel());
        }
        else if(session.action().equals("onShuffle")){
            this.game.onShuffle();
            session.write(payload,this.descriptor.responseLabel());
        }
        else if(session.action().equals("onPayout")){
            this.game.onPayout();
            session.write(payload,this.descriptor.responseLabel());
        }
        else if(session.action().equals("onWager")){
            OnAccess acc = builder.create().fromJson(new String(payload),OnAccess.class);
            float x = Float.parseFloat(acc.header("x"));
            float y = Float.parseFloat(acc.header("y"));
            if(this.game.onWager(acc.entryCost(),session.systemId(),acc.stub(),x,y)){
                ResponseHeader resp = new ResponseHeader("onWager", "wagered", true);
                session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
            }
            else{
                ResponseHeader resp = new ResponseHeader("onWager", "not wagered", false);
                session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onSeat")){
            OnAccess acc = builder.create().fromJson(new String(payload),OnAccess.class);
            CommandResponse resp = new CommandResponse("onSeat",false);
            resp.subscript = Integer.parseInt(acc.header("seatNumber"));
            if(this.game.onSeat(resp.subscript,session.systemId())){
                resp.successful(true);
            }
            session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("offSeat")){
            OnAccess acc = builder.create().fromJson(new String(payload),OnAccess.class);
            CommandResponse resp = new CommandResponse("onSeat",false);
            resp.subscript = Integer.parseInt(acc.header("seatNumber"));
            if(this.game.offSeat(session.systemId())){
                resp.successful(true);
            }
            session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onDealerSeat")){
            CommandResponse resp = new CommandResponse("onDealerSeat",true);
            resp.owner(session.systemId());
            this.game.onDealerSeat(resp);
            session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("offDealerSeat")){
            CommandResponse resp = new CommandResponse("offDealerSeat",true);
            resp.owner(session.systemId());
            this.game.offDealerSeat(resp);
            session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onStream")){
            this.onStream(session);
        }
        else if(session.action().equals("onLeave")){
            CommandResponse resp = new CommandResponse("onLeave",false);
            if(this.game.offSeat(session.systemId())){
                super.onTimeout(session);//kick off stream session
                resp.successful(true);
            }
            session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());

        }
        else if(session.action().equals("onPing")){
            session.write(payload,this.descriptor.responseLabel());
        }

        //ResponseHeader nts = new ResponseHeader(session.action(), "Action["+session.action()+"]["+session.systemId()+"] on blackjack", true);

    }
    @Override
    public void onTimeout(Session session) {
        this.game.offSeat(session.systemId());//recovery
        super.onTimeout(session);
    }


    @Override
    public void onIdle(Session session){
        this.uQueue.offer(new SessionIdle("blackjack",session.systemId(),session.stub()));
    }
    public void onStatistics(String key,double value,int index){
        super.onStatistics(key,value,index);
        GameStatisticsEntry gameStatisticsEntry = this.game.onStatistics[index];
        gameStatisticsEntry.value = this.statistics.value(key,0);
        this.uQueue.offer(gameStatisticsEntry);
    }
    @Override
    public void onBucket(int bucket,int state){
        //this.context.log("Bucket->"+bucket+"/"+state,OnLog.WARN);
    }
    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        Configuration cfg = this.context.configuration(this.descriptor.configurationType());
        this.builder.registerTypeAdapter(BlackJack.class,new BlackJackSerializer());
        this.builder.registerTypeAdapter(BlackJackSeat.class,new BlackJackSeatSerializer());
        this.builder.registerTypeAdapter(BlackJack.WagerTurn.class,new BlackJackTurnSerializer());
        this.builder.registerTypeAdapter(BlackJack.ActionTurn.class,new BlackJackTurnSerializer());
        this.builder.registerTypeAdapter(BlackJack.AutoTurn.class,new BlackJackTurnSerializer());
        this.builder.registerTypeAdapter(BlackJack.DealerFaceUpTurn.class,new BlackJackTurnSerializer());
        this.builder.registerTypeAdapter(BlackJack.DealerTurn.class,new BlackJackTurnSerializer());
        this.builder.registerTypeAdapter(BlackJack.PayoutTurn.class,new BlackJackTurnSerializer());
        this.builder.registerTypeAdapter(Deck.class,new DeckSerializer());
        this.game = new BlackJack();
        this.game.tournamentEnabled(this.descriptor.tournamentEnabled());
        this.game.pendingQueue = this.uQueue;
        this.game.splittable = Boolean.parseBoolean(cfg.property("splittable"));
        this.game.dealerSeatFee = Double.parseDouble(cfg.property("dealerSeatFee"));
        this.game.seats = Integer.parseInt(cfg.property("seats"));
        this.game.deckSize = Integer.parseInt(cfg.property("deckSize"));
        this.game.minWager = Double.parseDouble(cfg.property("minWager"));
        this.game.maxWager = Double.parseDouble(cfg.property("maxWager"));

        this.game.instanceId(this.context.onRegistry().distributionKey());
        this.game.name(this.descriptor.name());
        this.game.entryCost(this.descriptor.entryCost());
        this.game.betLineListener = this;
        this.statistics = this.context.onStatistics();
        this.game.onStatistics = new GameStatisticsEntry[5];
        this.game.onStatistics[0]=new GameStatisticsEntry("onStatistics","blackjack","totalBlackjack",this.statistics.value("totalBlackjack",0),0);
        this.game.onStatistics[1]=new GameStatisticsEntry("onStatistics","blackjack","totalRounds",this.statistics.value("totalRounds",0),1);
        this.game.onStatistics[2]=new GameStatisticsEntry("onStatistics","blackjack","totalWager",this.statistics.value("totalWager",0),2);
        this.game.onStatistics[3]=new GameStatisticsEntry("onStatistics","blackjack","totalPayout",this.statistics.value("totalPayout",0),3);
        this.game.onStatistics[4]=new GameStatisticsEntry("onStatistics","blackjack","totalHands",this.statistics.value("totalRounds",0),4);
        this.context.registerRecoverableListener(new PresencePortableRegistry()).addRecoverableFilter(PresencePortableRegistry.ON_BALANCE_CID,(t)->{
            //this.context.log("<><><>"+t.toString(),OnLog.WARN);
            OnBalance ob = (OnBalance)t;
            this.context.onRegistry().transact(t.owner(),ob.balance());
            this.game.onBalance(t.owner());
        });

        this.game.start();
        this.context.schedule(this.game);
        this.context.schedule(this);
        /**
        this.context.dataStore().registerRecoverableListener(new PortableRegistry()).addRecoverableFilter(PortableRegistry.HOUSE_CID,(r)->{
            this.context.log(r.toString(),OnLog.WARN);
        });
        this.context.dataStore().registerRecoverableListener(new PortableRegistry()).addRecoverableFilter(PortableRegistry.STATISTICS_ENTRY_CID,(r)->{
            this.context.log(r.toString(),OnLog.WARN);
        });
        this.context.dataStore().registerRecoverableListener(new GameRecoverableRegistry()).addRecoverableFilter(GameRecoverableRegistry.BLACKJACK_CID,(r)->{
            this.context.log(r.toString(),OnLog.WARN);
        });
        this.context.dataStore().registerRecoverableListener(new GameRecoverableRegistry()).addRecoverableFilter(GameRecoverableRegistry.BLACKJACK_SEAT_CID,(r)->{
            this.context.log(r.toString(),OnLog.WARN);
        });**/
        this.context.log("Black jack application started ["+this.descriptor.name()+"]", OnLog.INFO);
    }
}
