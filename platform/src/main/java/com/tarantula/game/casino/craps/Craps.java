package com.tarantula.game.casino.craps;

import com.tarantula.RNG;
import com.tarantula.Response;
import com.tarantula.Serviceable;
import com.tarantula.game.Game;
import com.tarantula.game.casino.BetLine;
import com.tarantula.game.casino.CashInBalance;
import com.tarantula.game.casino.Seat;
import com.tarantula.game.casino.SeatSlots;
import com.tarantula.platform.util.JvmRNG;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Updated by yinghu lu on 3/29/2019.
 */
public class Craps extends Game implements Serviceable {

    static final int PASS_LINE = 100;
    static final int COME_LINE = 101;
    static final int DO_NOT_PASS_LINE = 200;
    static final int DO_NOT_PASS_LINE_2 = 201;
    static final int DO_NOT_COME_LINE = 202;
    static final int PLACE_4 = 104;
    static final int PLACE_5 = 105;
    static final int PLACE_6 = 106;
    static final int PLACE_8 = 108;
    static final int PLACE_9 = 109;
    static final int PLACE_10 = 110;

    static final int PLACE_NOT_4 = 204;
    static final int PLACE_NOT_5 = 205;
    static final int PLACE_NOT_6 = 206;
    static final int PLACE_NOT_8 = 208;
    static final int PLACE_NOT_9 = 209;
    static final int PLACE_NOT_10 = 210;

    static final int BIG_6 = 306;
    static final int BIG_8 = 308;
    static final int HARD_4 = 322;
    static final int HARD_6 = 333;
    static final int HARD_8 = 344;
    static final int HARD_10 = 355;

    static final int FIELD = 400;
    static final int ANY_SEVEN = 401;
    static final int ANY_CRAPS = 402;
    static final int ON_2 = 403;
    static final int ON_3 = 404;
    static final int ON_11 = 405;
    static final int ON_11_B = 406;
    static final int ON_12 = 407;

    private RNG rng = new JvmRNG();

    public HashMap<Integer,CrapsLine> betLines;
    public CrapsLine[] betIndex;
    public DiceStop diceStop;

    private SeatSlots occupations;
    public ConcurrentHashMap<String, Seat> seatList;
    public Seat dealer;
    public double payout;

    public int round;

    public Craps(){
        this.vertex = "Craps";
        this.label = "craps";
    }
    public void _round(){
        tQueue.clear();
        tQueue.offer(new Craps.WagerTurn(this));
        _nextTurn();
        round++;
        this.betLineListener.onStatistics("totalRounds",1,4);
    }

    public synchronized Craps setup(){
        return this;
    }
    public synchronized boolean onRoll(){
        int d1 = rng.onNext(6)+1;
        int d2 = rng.onNext(6)+1;
        this.diceStop.stop(d1,d2);
        betLines.forEach((k,l)->{
            l.payout();
        });
        if(!this.diceStop.puck.on){//round reset if puck is off
            this.diceStop.puck.point =0;
        }
        this.pendingQueue.offer(this.diceStop);
        _round();
        return true;
    }
    public synchronized void robotOnWager(){
        CrapsLine cp = betIndex[rng.onNext(25)];
        cp.wager(null,minWager*(1+cp.subscript%10),0,0,cp.subscript);
    }
    public synchronized int onWager(String systemId,int lineId,double wager,float x,float y,int inx){
        if(dealer.occupied&&dealer.systemId().equals(systemId)){
            return BetLine.OFF_WAGER;
        }
        return this.betLines.get(lineId).wager(systemId,wager,x,y,inx);
    }
    public synchronized boolean onBalance(String systemId){
        Seat seat = this.seatList.get(systemId);
        if(seat==null){
            return false;
        }
        seat.balance(this.betLineListener.balance(systemId));
        this.pendingQueue.offer(new CashInBalance(systemId,seat.occupied?seat.subscript:-1,seat.balance(),"balance",label));
        return true;
    }
    public synchronized boolean onJoin(String systemId){
        Seat seat = new Seat(systemId,this.betLineListener.balance(systemId));
        seat.label("craps");
        seat.name("seat");
        seat.subscript = occupations.onSlot();
        seat.occupied = seat.subscript>=0;
        this.seatList.put(systemId,seat);
        this.pendingQueue.add(seat);
        return true;
    }
    public synchronized void onDealer(Response response){
        if(!dealer.occupied){
            if(this.betLineListener.onWager(response.owner(),this.dealerSeatFee)){
                dealer.occupied = true;
                dealer.systemId(response.owner());
                Seat seat = this.seatList.get(response.owner());
                seat.balance(this.betLineListener.balance(response.owner()));
                if(occupations.offSlot(seat.subscript)){
                    seat.occupied = false;
                    this.pendingQueue.add(seat);//release seat from stage
                }
                dealer.balance(this.betLineListener.balance(response.owner()));
                this.pendingQueue.add(dealer);
                response.message("joined sealer seat");
                response.code(BetLine.ON_WAGER);
            }
            else{
                response.message("not enough fund to join");
                response.code(BetLine.NO_FUND);
            }
        }
        else{
            response.message("not available, try later");
            response.code(BetLine.OFF_WAGER);
        }
    }
    public synchronized int offDealer(String systemId){
        if(dealer.occupied&&dealer.asPlayer(systemId)){
            dealer.occupied =false;
            dealer.systemId(null);
            dealer.balance(this.betLineListener.onHouse(0));
            this.pendingQueue.add(dealer);
            Seat seat = this.seatList.get(systemId);
            seat.subscript = this.occupations.onSlot();
            seat.occupied = seat.subscript>=0;
            seat.balance(this.betLineListener.balance(systemId));
            if(seat.occupied){
                this.pendingQueue.add(seat);
            }else{
                this.pendingQueue.offer(new CashInBalance(systemId,seat.occupied?seat.subscript:-1,seat.balance(),"balance",label));
            }
            return BetLine.ON_WAGER;
        }
        else{
            return BetLine.OFF_WAGER;
        }
    }
    public synchronized void onLeave(String systemId){
        Seat seat = this.seatList.remove(systemId);
        if(seat!=null){
            if(occupations.offSlot(seat.subscript)){
                seat.occupied = false;
                this.pendingQueue.add(seat);
            }
        }
        if(dealer.occupied&&dealer.systemId().equals(systemId)){
            dealer.occupied =false;
            dealer.systemId(null);
            dealer.balance(this.betLineListener.onHouse(0));
            this.pendingQueue.add(dealer);
        }
    }


    @Override
    public void start() throws Exception {
        this.diceStop = new DiceStop(0,this);
        this.rng.onNext(6);
        this.betLines = new HashMap<>();
        this.betIndex = new CrapsLine[25];
        int ix = 0;
        PassLine passLine = new PassLine(this);
        betLines.put(passLine.stub(),passLine);
        betIndex[ix++]=passLine;
        DoNotPassLine doNotPassLine = new DoNotPassLine(this);
        betLines.put(doNotPassLine.stub(),doNotPassLine);
        betIndex[ix++]=doNotPassLine;
        //h line on UI
        DoNotPassLine2 doNotPassLine2 = new DoNotPassLine2(this);
        betLines.put(doNotPassLine2.stub(),doNotPassLine2);
        betIndex[ix++]=doNotPassLine;
        Place4Line place4Line = new Place4Line(this);
        betLines.put(place4Line.stub(),place4Line);
        betIndex[ix++]=place4Line;
        Place5Line place5Line = new Place5Line(this);
        betLines.put(place5Line.stub(),place5Line);
        betIndex[ix++]=place5Line;
        Place6Line place6Line = new Place6Line(this);
        betLines.put(place6Line.stub(),place6Line);
        betIndex[ix++]=place6Line;
        Place8Line place8Line = new Place8Line(this);
        betLines.put(place8Line.stub(),place8Line);
        betIndex[ix++]=place8Line;
        Place9Line place9Line = new Place9Line(this);
        betLines.put(place9Line.stub(),place9Line);
        betIndex[ix++]=place9Line;
        Place10Line place10Line = new Place10Line(this);
        betLines.put(place10Line.stub(),place10Line);
        betIndex[ix++]=place10Line;
        FieldLine fieldLine = new FieldLine(this);
        betLines.put(fieldLine.stub(),fieldLine);
        betIndex[ix++]=fieldLine;
        Big6Line big6Line = new Big6Line(this);
        betLines.put(big6Line.stub(),big6Line);
        betIndex[ix++]=big6Line;
        Big8Line big8Line = new Big8Line(this);
        betLines.put(big8Line.stub(),big8Line);
        betIndex[ix++]=big8Line;
        ComeLine comeLine = new ComeLine(this);
        betLines.put(comeLine.stub(),comeLine);
        betIndex[ix++]=comeLine;
        DoNotComeLine doNotComeLine = new DoNotComeLine(this);
        betLines.put(doNotComeLine.stub(),doNotComeLine);
        betIndex[ix++]=doNotComeLine;

        AnySevenLine anySevenLine = new AnySevenLine(this);
        betLines.put(anySevenLine.stub(),anySevenLine);
        betIndex[ix++]=anySevenLine;
        Hard4Line hard4Line = new Hard4Line(this);
        betLines.put(hard4Line.stub(),hard4Line);
        betIndex[ix++]=hard4Line;
        Hard6Line hard6Line = new Hard6Line(this);
        betLines.put(hard6Line.stub(),hard6Line);
        betIndex[ix++]=hard6Line;
        Hard8Line hard8Line = new Hard8Line(this);
        betLines.put(hard8Line.stub(),hard8Line);
        betIndex[ix++]=hard8Line;
        Hard10Line hard10Line = new Hard10Line(this);
        betLines.put(hard10Line.stub(),hard10Line);
        betIndex[ix++]=hard10Line;

        On2Line on2Line = new On2Line(this);
        betLines.put(on2Line.stub(),on2Line);
        betIndex[ix++]=on2Line;
        On3Line on3Line = new On3Line(this);
        betLines.put(on3Line.stub(),on3Line);
        betIndex[ix++]=on3Line;
        On12Line on12Line = new On12Line(this);
        betLines.put(on12Line.stub(),on12Line);
        betIndex[ix++]=on12Line;
        On11Line on11Line = new On11Line(this);
        betLines.put(on11Line.stub(),on11Line);
        betIndex[ix++]=on11Line;
        On11Line2 on11Line2 = new On11Line2(this);
        betLines.put(on11Line2.stub(),on11Line2);
        betIndex[ix++]=on11Line2;
        AnyCrapsLine anyCrapsLine = new AnyCrapsLine(this);
        betLines.put(anyCrapsLine.stub(),anyCrapsLine);
        betIndex[ix++]=anyCrapsLine;

        //do not come point wager list
        PlaceNot4Line pn4 = new PlaceNot4Line(this);
        betLines.put(pn4.stub(),pn4);
        PlaceNot5Line pn5 = new PlaceNot5Line(this);
        betLines.put(pn5.stub(),pn5);
        PlaceNot6Line pn6 = new PlaceNot6Line(this);
        betLines.put(pn6.stub(),pn6);
        PlaceNot8Line pn8 = new PlaceNot8Line(this);
        betLines.put(pn8.stub(),pn8);
        PlaceNot9Line pn9 = new PlaceNot9Line(this);
        betLines.put(pn9.stub(),pn9);
        PlaceNot10Line pn10 = new PlaceNot10Line(this);
        betLines.put(pn10.stub(),pn10);

        this.seatList = new ConcurrentHashMap<>();
        this.dealer = new Seat();
        this.dealer.name("dealerSeat");
        this.dealer.bank = true;
        this.dealer.label("craps");
        this.dealer.balance(this.betLineListener.onHouse(0));
        Puck puck = new Puck();
        puck.name("puck");
        puck.label("craps");
        this.diceStop.puck = puck;
        this.occupations = new SeatSlots(this.seats);
        this._round();
    }

    @Override
    public void shutdown() throws Exception {

    }

    public static class WagerTurn extends CrapsTurn {
        public WagerTurn(Craps craps){
            this.craps = craps;
            this.duration = Game.CHECK_POINT_INTERVAL*2;
            this.event = "Wager";
            this.name = "turn";
            this.label = "craps";
        }

        @Override
        public void reset() {
            if(SystemUtil.timeout(timestamp)) {
                if(!craps.onRoll()){
                    this.timestamp = SystemUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(duration));
                }
            }
            else{
                //per wager on each check point
                craps.robotOnWager();
            }
        }
    }
}
