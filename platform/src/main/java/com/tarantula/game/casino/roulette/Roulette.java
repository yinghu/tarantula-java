package com.tarantula.game.casino.roulette;

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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Updated by yinghu lu on 3/8/2019.
 */
public class Roulette extends Game implements Serviceable {

    private RNG rng = new JvmRNG();

    private HashMap<Integer,RouletteLine> betLines;
    private RouletteLine[] betIndex;

    private WheelStop[] wheelStops;

    public WheelStop wheelStop;

    public static int ON_WAGER = 1;

    public static int ON_PAYOUT = 2;

    public Seat dealer;

    private int onTurn;
    public double payout;
    public int round;

    public ConcurrentHashMap<String, Seat> seatList = new ConcurrentHashMap<>();
    private SeatSlots seatSlots;

    public Roulette(){
        this.vertex = "Roulette";
        this.label = "roulette";
    }

    public synchronized Roulette setup(){
        return this;
    }
    public void _round(){
        onTurn = ON_WAGER;
        round++;
        this.betLineListener.onStatistics("totalRounds",1,4);
        tQueue.clear();
        tQueue.offer(new Roulette.WagerTurn(this));
        tQueue.offer(new Roulette.PayoutTurn(this));
        _nextTurn();
    }
    public synchronized boolean onWheel(String systemId){
        if(dealer.occupied&&(!dealer.asPlayer(systemId))){
             return false;
        }
        return _onWheel();
    }
    public synchronized boolean onWheel(){
        return _onWheel();
    }
    public  boolean _onWheel(){
        if(onTurn!=ON_WAGER){
            return false;
        }
        wheelStop = this.wheelStops[rng.onNext(38)];
        if(wheelStop.color.equals("RED")){
            this.betLineListener.onStatistics("totalRed",1,0);
        }
        else if(wheelStop.color.equals("BLACK")){
            this.betLineListener.onStatistics("totalBlack",1,1);
        }
        this.pendingQueue.offer(wheelStop);
        onTurn = ON_PAYOUT;
        _nextTurn();
        return true;
    }
    public synchronized void robotOnWager(){
        if(onTurn!=ON_WAGER){
            return;
        }
        RouletteLine line = betIndex[rng.onNext(50)];
        BetLine bt = new BetLine(line.lineId,0,0,line.subscript,null,minWager*((line.subscript%10+1)));
        bt.label("roulette");
        this.betLineListener.onStatistics("totalWager",bt.wager(),2);
        _onWager(bt);
    }
    private void _onWager(BetLine betLine){
        if(this.dealer.occupied){
            this.betLineListener.onPayout(this.dealer.systemId(),betLine.wager());
            this.dealer.balance(this.betLineListener.balance(dealer.systemId()));
        }
        else{
            this.dealer.balance(this.betLineListener.onHouse(betLine.wager()));
        }
        betLineList.add(betLine);
        RouletteLine line = betLines.get(betLine.stub());
        line.wagerList.add(betLine);
        line.wagered = true;
        this.pendingQueue.offer(betLine);
        this.pendingQueue.offer(new CashInBalance(this.dealer.systemId(),-1,this.dealer.balance(),"dealer",label));
    }
    public synchronized int onWager(String systemId,int lineId,double wager,int index){
        if(onTurn!=ON_WAGER||dealer.asPlayer(systemId)){
            return BetLine.OFF_WAGER;
        }
        RouletteLine line = betLines.get(lineId);
        if(line!=null&&this.betLineListener.onWager(systemId,wager)){
            Seat seat = this.seatList.get(systemId);
            seat.balance(this.betLineListener.balance(systemId));
            this.pendingQueue.offer(new CashInBalance(systemId,seat.occupied?seat.subscript:-1,seat.balance(),"balance",label));
            BetLine bt = new BetLine(lineId,0,0,index,systemId,wager);
            bt.label("roulette");
            this.betLineListener.onStatistics("totalWager",wager,2);
            _onWager(bt);
            return BetLine.ON_WAGER;
        }
        else{
            return BetLine.NO_FUND;
        }
    }
    public synchronized boolean onPayout(){
        if(onTurn!=ON_PAYOUT){
            return false;
        }
        wheelStops[wheelStop.subscript].payout();
        for(WheelStop w : wheelStops){
            w.clear();
        }
        onTurn = ON_WAGER;
        wheelStop = null;
        betLineList.clear();
        this.pendingQueue.offer(dealer);
        _round();
        return true;
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
        Seat seat = new Seat(systemId,betLineListener.balance(systemId));
        seat.name("seat");
        seat.label(label);
        seat.subscript = seatSlots.onSlot();
        seat.occupied = seat.subscript>=0;
        this.seatList.put(systemId,seat);
        this.pendingQueue.add(seat);
        return true;
    }
    public synchronized void onDealer(Response response){
        if(onTurn!=ON_WAGER||dealer.occupied){
            response.code(BetLine.OFF_WAGER);
            response.message("Not available,try again later");
            return;
        }
        if(this.betLineListener.onWager(response.owner(),this.dealerSeatFee)){
            dealer.occupied = true;
            dealer.systemId(response.owner());
            Seat seat = this.seatList.get(response.owner());
            seat.balance(this.betLineListener.balance(response.owner()));
            if(seatSlots.offSlot(seat.subscript)){
                seat.occupied = false;
                this.pendingQueue.add(seat);//release seat from stage
            }
            dealer.balance(seat.balance());
            this.pendingQueue.add(dealer);
            response.code(BetLine.ON_WAGER);
            response.message("Successfully joined as a dealer");
        }
        else{
            response.code(BetLine.NO_FUND);
            response.message("Add balance,try again later");
        }
    }
    public synchronized void offDealer(Response response){
        if(onTurn!=ON_WAGER||(!dealer.occupied)||(!dealer.asPlayer(response.owner()))){
            response.code(BetLine.OFF_WAGER);
            response.message("Not available,try again later");
            return;
        }
        dealer.occupied =false;
        dealer.systemId(null);
        dealer.balance(this.betLineListener.onHouse(0));
        this.pendingQueue.add(dealer);
        Seat seat = this.seatList.get(response.owner());
        seat.subscript = this.seatSlots.onSlot();
        seat.occupied = seat.subscript>=0;
        seat.balance(this.betLineListener.balance(response.owner()));
        if(seat.occupied){
            this.pendingQueue.add(seat);
        }else{
            this.pendingQueue.offer(new CashInBalance(response.owner(),seat.occupied?seat.subscript:-1,seat.balance(),"balance",label));
        }
        response.code(BetLine.ON_WAGER);
        response.message("Successfully left from dealer");
    }
    public synchronized void onLeave(String systemId){
        Seat seat = this.seatList.remove(systemId);
        if(seat!=null){
            if(seatSlots.offSlot(seat.subscript)){
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
        this.wheelStops = new WheelStop[38];
        wheelStops[0]= new WheelStop("0","WHITE",0);
        wheelStops[1]= new WheelStop("28","BLACK",1);
        wheelStops[2]= new WheelStop("9","RED",2);
        wheelStops[3]= new WheelStop("26","BLACK",3);
        wheelStops[4]= new WheelStop("30","RED",4);
        wheelStops[5]= new WheelStop("11","BLACK",5);
        wheelStops[6]= new WheelStop("7","RED",6);
        wheelStops[7]= new WheelStop("20","BLACK",7);
        wheelStops[8]= new WheelStop("32","RED",8);
        wheelStops[9]= new WheelStop("17","BLACK",9);
        wheelStops[10]= new WheelStop("5","RED",10);
        wheelStops[11]= new WheelStop("22","BLACK",11);
        wheelStops[12]= new WheelStop("34","RED",12);
        wheelStops[13]= new WheelStop("15","BLACK",13);
        wheelStops[14]= new WheelStop("3","RED",14);
        wheelStops[15]= new WheelStop("24","BLACK",15);
        wheelStops[16]= new WheelStop("36","RED",16);
        wheelStops[17]= new WheelStop("13","BLACK",17);
        wheelStops[18]= new WheelStop("1","RED",18);
        wheelStops[19]= new WheelStop("00","WHITE",19);//00
        wheelStops[20]= new WheelStop("27","RED",20);
        wheelStops[21]= new WheelStop("10","BLACK",21);
        wheelStops[22]= new WheelStop("25","RED",22);
        wheelStops[23]= new WheelStop("29","BLACK",23);
        wheelStops[24]= new WheelStop("12","RED",24);
        wheelStops[25]= new WheelStop("8","BLACK",25);
        wheelStops[26]= new WheelStop("19","RED",26);
        wheelStops[27]= new WheelStop("31","BLACK",27);
        wheelStops[28]= new WheelStop("18","RED",28);
        wheelStops[29]= new WheelStop("6","BLACK",29);
        wheelStops[30]= new WheelStop("21","RED",30);
        wheelStops[31]= new WheelStop("33","BLACK",31);
        wheelStops[32]= new WheelStop("16","RED",32);
        wheelStops[33]= new WheelStop("4","BLACK",33);
        wheelStops[34]= new WheelStop("23","RED",34);
        wheelStops[35]= new WheelStop("35","BLACK",35);
        wheelStops[36]= new WheelStop("14","RED",36);
        wheelStops[37]= new WheelStop("2","BLACK",37);
        this.betLines = new HashMap();
        this.betIndex = new RouletteLine[50];
        for(WheelStop w : wheelStops){
            int lid = Integer.parseInt("35"+w.symbol);
            int ix;
            if(!w.symbol.equals("00")){
                ix = Integer.parseInt(w.symbol);
            }
            else{
                ix = 37;
            }
            RouletteLine rl = new RouletteLine(lid,w.symbol,35d,ix,this);
            rl.stub(w.subscript);
            this.betIndex[ix]=rl;
            this.betLines.put(lid,rl);
            w.lineList.add(rl);
        }
        this.betLines.put(201,new RouletteLine(201,"1st column",2d,38,this));
        betIndex[38]=this.betLines.get(201);
        int[] firstColumn = {1,4,7,10,13,16,19,22,25,28,31,34};
        for(int i=0;i<firstColumn.length;i++){
            WheelStop w = wheelStops[betLines.get(Integer.parseInt("35"+firstColumn[i])).stub()];
            w.lineList.add(betLines.get(201));
        }
        this.betLines.put(202,new RouletteLine(202,"2st column",2d,39,this));
        betIndex[39]=this.betLines.get(202);
        for(int i=0;i<firstColumn.length;i++){
            WheelStop w = wheelStops[betLines.get(Integer.parseInt("35"+(firstColumn[i]+1))).stub()];
            w.lineList.add(betLines.get(202));
        }
        this.betLines.put(203,new RouletteLine(203,"3st column",2d,40,this));
        betIndex[40]=this.betLines.get(203);
        for(int i=0;i<firstColumn.length;i++){
            WheelStop w = wheelStops[betLines.get(Integer.parseInt("35"+(firstColumn[i]+2))).stub()];
            w.lineList.add(betLines.get(203));
        }
        this.betLines.put(204,new RouletteLine(204,"1st dozen (1-12)",2d,41,this));
        betIndex[41]=this.betLines.get(204);
        int[] firstDezon = {1,2,3,4,5,6,7,8,9,10,11,12};
        for(int i=0;i<firstDezon.length;i++){
            WheelStop w = wheelStops[betLines.get(Integer.parseInt("35"+firstDezon[i])).stub()];
            w.lineList.add(betLines.get(204));
        }
        this.betLines.put(205,new RouletteLine(205,"2st dozen (13-24)",2d,42,this));
        betIndex[42]=this.betLines.get(205);
        for(int i=0;i<firstDezon.length;i++){
            WheelStop w = wheelStops[betLines.get(Integer.parseInt("35"+(firstDezon[i]+12))).stub()];
            w.lineList.add(betLines.get(205));
        }
        this.betLines.put(206,new RouletteLine(206,"3st dozen (25-36)",2d,43,this));
        betIndex[43]=this.betLines.get(206);
        for(int i=0;i<firstDezon.length;i++){
            WheelStop w = wheelStops[betLines.get(Integer.parseInt("35"+(firstDezon[i]+24))).stub()];
            w.lineList.add(betLines.get(206));
        }
        this.betLines.put(101,new RouletteLine(101,"odd",1d,48,this));
        betIndex[48]=this.betLines.get(101);
        this.betLines.put(102,new RouletteLine(102,"even",1d,45,this));
        betIndex[45]=this.betLines.get(102);
        this.betLines.put(105,new RouletteLine(105,"1-18",1d,44,this));
        betIndex[44]=this.betLines.get(105);
        this.betLines.put(106,new RouletteLine(106,"19-36",1d,49,this));
        betIndex[49]=this.betLines.get(106);
        this.betLines.put(103,new RouletteLine(103,"red",1d,46,this));
        betIndex[46]=this.betLines.get(103);
        this.betLines.put(104,new RouletteLine(104,"black",1d,47,this));
        betIndex[47]=this.betLines.get(104);
        for(int i=1;i<37;i++){
            WheelStop w = wheelStops[betLines.get(Integer.parseInt("35"+i)).stub()];
            int sbm = Integer.parseInt(w.symbol);
            if((sbm&1)==0){
                w.lineList.add(betLines.get(101));
            }
            else{
                w.lineList.add(betLines.get(102));
            }
            if(sbm<19){
                w.lineList.add(betLines.get(105));
            }
            else{
                w.lineList.add(betLines.get(106));
            }
            if(w.color.equals("RED")){
                w.lineList.add(betLines.get(103));
            }
            else{
                w.lineList.add(betLines.get(104));
            }
        }
        this.dealer = new Seat();
        dealer.name("dealerSeat");
        dealer.label("roulette");
        this.dealer.balance(this.betLineListener.onHouse(0));
        this.onTurn = ON_WAGER;
        this.betLineList = new CopyOnWriteArrayList<>();
        this.seatSlots = new SeatSlots(this.seats);
        _round();
    }

    @Override
    public void shutdown() throws Exception {

    }


    public static class WagerTurn extends RouletteTurn {
        public WagerTurn(Roulette roulette){
            this.roulette = roulette;
            this.duration = Game.CHECK_POINT_INTERVAL*4;
            this.event = "Wager";
            this.name = "turn";
            this.label = "roulette";
        }

        @Override
        public void reset() {
            if(SystemUtil.timeout(timestamp)) {
                if(!roulette.onWheel()){
                    this.timestamp = SystemUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(duration));
                }
            }
            else{
                //per wager on each check point
                roulette.robotOnWager();
            }
        }
    }
    public static class PayoutTurn extends RouletteTurn {
        public PayoutTurn(Roulette roulette){
            this.roulette = roulette;
            this.duration = Game.CHECK_POINT_INTERVAL*4;
            this.event = "Wager";
            this.name = "turn";
            this.label = "roulette";
        }

        @Override
        public void reset() {
            if(SystemUtil.timeout(timestamp)) {
                roulette.onPayout();
            }
        }
    }
}
