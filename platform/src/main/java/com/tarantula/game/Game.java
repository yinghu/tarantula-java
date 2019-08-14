package com.tarantula.game;

import com.tarantula.SchedulingTask;
import com.tarantula.game.casino.BetLine;
import com.tarantula.game.casino.BetLineListener;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Updated by yinghu lu on 4/22/2019.
 */
public class Game extends CheckPoint implements SchedulingTask {

    public int deckSize;
    public double minWager;
    public double maxWager;
    public double dealerSeatFee;
    public int seats;
    public GameStatisticsEntry[] onStatistics;
    public ConcurrentLinkedQueue<GameComponent> pendingQueue;
    public CheckPoint currentCheckPoint;
    public Deque<CheckPoint> tQueue = new ArrayDeque<>(7);//
    public CopyOnWriteArrayList<BetLine> betLineList;
    public BetLineListener betLineListener;

    public static final int CHECK_POINT_INTERVAL = 4;


    public Game(){}

    public Game(int deckSize,double min,double max,double dealerSeatFee){
        this.deckSize = deckSize;
        this.minWager = min;
        this.maxWager = max;
        this.dealerSeatFee = dealerSeatFee;
    }
    public Game(Game c){
        this.deckSize = c.deckSize;
        this.minWager = c.minWager;
        this.maxWager = c.maxWager;
        this.dealerSeatFee = c.dealerSeatFee;
    }

    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return CHECK_POINT_INTERVAL*1000;
    }

    @Override
    public long delay() {
        return CHECK_POINT_INTERVAL*1000;

    }
    public void _nextTurn(){
        CheckPoint next = tQueue.poll();
        if(next!=null){
            this.currentCheckPoint = next;
            this.currentCheckPoint.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(currentCheckPoint.duration())));
        }
    }
    @Override
    public synchronized void run() {
        try {
            this.currentCheckPoint.reset();
            this.currentCheckPoint.label(this.label);
            this.pendingQueue.offer(this.currentCheckPoint);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
