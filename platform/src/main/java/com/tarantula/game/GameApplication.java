package com.tarantula.game;

import com.tarantula.*;
import com.tarantula.game.casino.*;
import com.tarantula.platform.TarantulaApplicationHeader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by yinghu lu on 11/24/2018.
 */
public class GameApplication extends TarantulaApplicationHeader implements SessionTimeoutListener, BetLineListener, SchedulingTask,GameComponentListener {

    protected ConcurrentHashMap<String,Session> _onStream = new ConcurrentHashMap<>();

    protected ConcurrentLinkedQueue<GameComponent> uQueue = new ConcurrentLinkedQueue<>();

    protected Statistics statistics;

    protected long SERVER_PUSH_INTERVAL = 250;

    public void onStream(Session session){
        this._onStream.put(session.systemId(),session);
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.builder.registerTypeAdapter(Seat.class,new SeatSerializer());
        this.builder.registerTypeAdapter(BetLine.class,new BetLineSerializer());
        this.builder.registerTypeAdapter(CountOnRank.class,new CountOnRankSerializer());
        this.builder.registerTypeAdapter(GameStatisticsEntry.class,new GameStatisticsEntrySerializer());
        this.builder.registerTypeAdapter(CashInBalance.class,new CashInBalanceSerializer());
        this.builder.registerTypeAdapter(SessionIdle.class,new SessionIdleSerializer());
        this.builder.registerTypeAdapter(CommandResponse.class,new CommandResponseSerializer());
    }

    @Override
    public void onTimeout(Session session) {
        if(!this.descriptor.singleton()){
            this.context.onRegistry().onLeave(session);
        }
        this._onStream.remove(session.systemId());
    }

    @Override
    public boolean onWager(String systemId,double wager) {
       return this.context.onRegistry().transact(systemId,wager*(-1));
    }

    @Override
    public boolean onPayout(String systemId,double payout) {
        return this.context.onRegistry().transact(systemId,payout);
    }
    @Override
    public double balance(String systemId){
        return this.context.onRegistry().balance(systemId);
    }
    @Override
    public double onHouse(double delta){
        this.context.onRegistry().house().transact(delta);
        return this.context.onRegistry().house().balance();
    }
    public void onStatistics(String key,double value,int index){
        this.statistics.value(key,value);
    }

    @Override
    public void onIdle(Session session){

    }
    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return SERVER_PUSH_INTERVAL;
    }

    @Override
    public long delay() {
        return SERVER_PUSH_INTERVAL;
    }

    @Override
    public void run() {
        try{
            GameComponent updated;
            do{
                updated = uQueue.poll();
                if(updated!=null){
                    final String label = updated.label();
                    byte[] delta = this.builder.create().toJson(updated).getBytes();
                    if((!updated.broadcasting)&&(updated.systemId()!=null)){
                        _onStream.get(updated.systemId()).write(delta,label);
                    }else{//broadcasting
                        _onStream.forEach((k,v)->{
                            v.write(delta,label);
                        });
                    }
                }
            }while (updated!=null);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onUpdated(GameComponent updated) {
        uQueue.offer(updated);
    }
}
