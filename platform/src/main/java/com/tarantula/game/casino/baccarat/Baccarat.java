package com.tarantula.game.casino.baccarat;

import com.tarantula.Response;
import com.tarantula.Serviceable;
import com.tarantula.game.CommandResponse;
import com.tarantula.game.Game;
import com.tarantula.game.casino.*;
import com.tarantula.platform.util.FIFOBuffer;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yinghu lu on 12/10/2018.
 */
public class Baccarat extends Game implements Serviceable {

    public Game header;

    public Deck deck;

    public static int PLAYER_LINE_ID = 1;
    public static int BANKER_LINE_ID = 2;
    public static int TIE_LINE_ID = 3;

    public static int ON_WAGER = 1;
    public static int ON_DEAL = 2;
    public static int ON_PAYOUT = 3;

    public Seat houseSeat;

    public double payout;
    public BaccaratPlayerHand playerHand ; //1 : 1
    public BaccaratBankerHand bankerHand; // 19 : 20  5% of winner wager
    public BaccaratTieBetLine baccaratTieBetLine; //1 : 8

    public int onTurn = ON_WAGER;

    public Seat[] onStage;
    public ConcurrentHashMap<String,Seat> seatList = new ConcurrentHashMap<>();

    public FIFOBuffer<String> roundResult = new FIFOBuffer<>(20,new String[20]);

    public int round;

    public CountOnRank[] countOnRanks;

    public Baccarat(){
        this.vertex = "Baccarat";
        this.label = "baccarat";
    }

    public synchronized Baccarat setup(){
        Baccarat ct = new Baccarat();
        ct.header = header;
        ct.houseSeat = houseSeat.setup();
        ct.bankerHand = bankerHand.setup();
        ct.playerHand = playerHand.setup();
        ct.baccaratTieBetLine = baccaratTieBetLine.setup();
        ct.deck = deck.setup();
        ct.seatList = seatList;
        ct.roundResult = roundResult;
        ct.betLineList = betLineList;
        return this;
    }
    public void _round(){
        onTurn = ON_WAGER;
        round++;
        tQueue.clear();
        deck.cardCounter.listByRank((r,c)->{
            countOnRanks[r-1].count = c;
            pendingQueue.add(countOnRanks[r-1]);
        });
        this.currentCheckPoint = new Baccarat.WagerTurn(this);
        this.currentCheckPoint.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(currentCheckPoint.duration())));

    }
    public synchronized void join(String systemId){
        Seat seat = new Seat(systemId,betLineListener.balance(systemId));
        seat.name("seat");
        seat.label("baccarat");
        seat.subscript = -1;
        seatList.put(systemId,seat);
        this.pendingQueue.add(seat);
        this.pendingQueue.offer(new CashInBalance(systemId,seat.subscript,seat.balance(),"balance","baccarat"));
    }
    public synchronized boolean onSeat(int sn,String systemId){
        if(houseSeat.asPlayer(systemId)){
            return false;
        }
        Seat seat = seatList.get(systemId);
        Seat ss = onStage[sn];
        if(seat.subscript<0&&(!ss.asPlayer())){//new seat
            ss.occupied = true;
            ss.systemId(systemId);
            ss.balance(seat.balance());
            seat.subscript = sn;
            pendingQueue.offer(ss);
            return true;
        }
        else if(seat.subscript>=0&&seat.subscript!=sn&&(!ss.asPlayer())){//swapping
            Seat rs = onStage[seat.subscript];
            rs.occupied = false;
            rs.systemId(null);
            pendingQueue.offer(rs);
            ss.occupied = true;
            ss.systemId(systemId);
            ss.balance(seat.balance());
            seat.subscript = sn;//new seat
            pendingQueue.offer(ss);
            return true;
        }
        else{
            return false;
        }
    }
    public synchronized void leave(String systemId){
        Seat seat = seatList.remove(systemId);
        if(seat!=null){
            if(seat.subscript>=0){
                Seat occupied =onStage[seat.subscript];
                occupied.occupied = false;
                occupied.systemId(null);
                this.pendingQueue.add(occupied);
            }
            if(houseSeat.asPlayer(systemId)){
                houseSeat.occupied = false;
                houseSeat.systemId(null);
                houseSeat.balance(this.betLineListener.onHouse(0));
                this.pendingQueue.offer(houseSeat);
            }
        }
    }
    public synchronized void onBalance(String systemId){
        Seat seat = seatList.get(systemId);
        if(seat!=null){
            seat.balance(this.betLineListener.balance(systemId));
            this.pendingQueue.offer(new CashInBalance(systemId,seat.subscript,seat.balance(),"balance","baccarat"));
        }
    }
    public synchronized boolean onAction(String systemId){
        if(currentCheckPoint.onTurn(systemId)){
            return this.currentCheckPoint.check();
        }else{
            return false;
        }
    }
    public synchronized boolean onDeal(){
        return _onDeal();
    }
    private boolean _onDeal(){
        if(onTurn!=ON_WAGER){
            return false;
        }
        onTurn = ON_DEAL;
        int r1 = playerHand.deal(deck.draw(),deck.draw());
        int r2 = bankerHand.deal(deck.draw(),deck.draw());
        this.pendingQueue.offer(deck.setup());
        if(r1>=8||r2>=8||(r1==r2)){ //natural or tie
            this.onTurn = ON_PAYOUT;
            playerHand.standing = true;
            bankerHand.standing = true;
        }
        else{
            playerHand.standing = playerHand.rank()>=6;//no third card on standing
            bankerHand.standing = bankerHand.rank()>=7;//no third card on standing
            if(playerHand.standing&&bankerHand.standing){
                onTurn = ON_PAYOUT;
            }
            else {
                if(!playerHand.standing){
                    tQueue.offer(new PlayerTurn(this));
                }
                if(!bankerHand.standing){
                    tQueue.offer(new BankerTurn(this));
                }
            }
        }
        this.pendingQueue.offer(playerHand);
        this.pendingQueue.offer(bankerHand);
        this.tQueue.offer(new PayoutTurn(this));
        _nextTurn();
        return true;
    }
    private boolean thirdCardOnPlayer(){
        if(onTurn!=ON_DEAL){
            return false;
        }
        boolean suc = playerHand.thirdCard(deck.draw());
        if(bankerHand.standing){
            onTurn = ON_PAYOUT;
        }
        _nextTurn();
        return suc;
    }
   private boolean thirdCardOnBanker(){
        if(onTurn!=ON_DEAL||(!playerHand.standing)){
            return false;
        }
        this.onTurn = ON_PAYOUT;
        int playerThirdCard = playerHand.checkThirdCard();
        int bankerRank = bankerHand.rank();
        boolean suc;
        if(bankerRank<3){ // draw any of 3th of banker
            suc = bankerHand.thirdCard(deck.draw());
        }
        else if(bankerRank==3&&playerThirdCard!=8){ // draw 3th except 8 of 3th of player
            suc = bankerHand.thirdCard(deck.draw());
        }
        else if(bankerRank==4&&(playerThirdCard==-1||playerThirdCard>1&&playerThirdCard<8)){ //draw 3th on 2 ,3, 4, 5, 6, 7 of 3th of player
            suc = bankerHand.thirdCard(deck.draw());
        }
        else if(bankerRank==5&&(playerThirdCard==-1||playerThirdCard>3&&playerThirdCard<8)){ //draw 3th on 4 ,5 ,6 ,7 of 3th of player
            suc = bankerHand.thirdCard(deck.draw());
        }
        else if(bankerRank==6&&(playerThirdCard==6||playerThirdCard==7)){ //draw 3th on 6 ,7 of 3th of player
            suc = bankerHand.thirdCard(deck.draw());
        }
        else{
            bankerHand.standing = true;
            this.pendingQueue.offer(bankerHand);
            suc =false;
        }
        _nextTurn();
        return suc;
    }
    private boolean onPayout(){
        if(onTurn!=ON_PAYOUT){
            return false;
        }
        int b = bankerHand.rank();
        int p = playerHand.rank();
        if(b>p){
            double _p = bankerHand.payout();
            if(_p>0){
                this.betLineListener.onStatistics("totalPayoutBanker",_p,3);
            }
            roundResult.push("B"+b);
        }
        else if(b<p){
            double _p = playerHand.payout();
            if(_p>0){
                this.betLineListener.onStatistics("totalPayoutPlayer",_p,4);
            }
            roundResult.push("P"+p);
        }
        else{//push player and banker wagers on tie
            roundResult.push("T"+b);
            double _p = baccaratTieBetLine.payout();
            if(_p>0){
                this.betLineListener.onStatistics("totalPayoutTie",_p,5);
            }
            playerHand.odd = 1;
            _p = playerHand.payout();
            if(_p>0){
                this.betLineListener.onStatistics("totalPayoutPlayer",_p,4);
            }
            playerHand.odd = 2;
            bankerHand.odd  = 1;
            _p = bankerHand.payout();
            if(_p>0){
                this.betLineListener.onStatistics("totalPayoutBanker",_p,3);
            }
            bankerHand.odd = 2*0.95;
        }
        bankerHand.clear();
        playerHand.clear();
        baccaratTieBetLine.clear();
        betLineList.clear();
        this.pendingQueue.offer(playerHand);
        this.pendingQueue.offer(bankerHand);
        _round();
        return true;
    }
    private void robotOnWager(){
        if(this.onTurn!=ON_WAGER||this.tournamentEnabled){
            return;
        }
        BetLine wg = new BetLine(LocalDateTime.now().getSecond()%3+1,0,0,LocalDateTime.now().getSecond()%11,systemId,minWager*(1+LocalDateTime.now().getSecond()%10));
        _onWager(wg);
    }
    private void _onWager(BetLine bet){
        if(bet.stub()==PLAYER_LINE_ID){
            playerHand.wager(bet);
            bet.balance(playerHand.balance());
            this.betLineListener.onStatistics("totalWagerPlayer",bet.wager(),1);
        }
        else if(bet.stub()==BANKER_LINE_ID){
            bankerHand.wager(bet);
            bet.balance(bankerHand.balance());
            this.betLineListener.onStatistics("totalWagerBanker",bet.wager(),0);
        }
        else{
            baccaratTieBetLine.wager(bet);
            bet.balance(baccaratTieBetLine.balance());
            this.betLineListener.onStatistics("totalWagerTie",bet.wager(),2);
        }
        betLineList.add(bet);
        bet.label("baccarat");
        this.pendingQueue.offer(bet);
        if(bet.systemId()!=null){
            Seat seat = this.seatList.get(bet.systemId());
            seat.balance(this.betLineListener.balance(bet.systemId()));
            this.pendingQueue.offer(new CashInBalance(bet.systemId(),seat.subscript,seat.balance(),"balance","baccarat"));
        }
        if(houseSeat.asPlayer()){
            this.betLineListener.onPayout(houseSeat.systemId(),bet.wager());
            this.seatList.get(houseSeat.systemId()).balance(this.betLineListener.balance(houseSeat.systemId()));
            this.houseSeat.balance(this.betLineListener.balance(houseSeat.systemId()));
        }else{
            this.houseSeat.balance(this.betLineListener.onHouse(bet.wager()));
        }
        this.pendingQueue.offer(new CashInBalance(houseSeat.systemId(),-1,this.houseSeat.balance(),"dealer","baccarat"));
    }
    public synchronized void onWager(CommandResponse response){
        if(onTurn!=ON_WAGER||houseSeat.asPlayer(response.owner())){
            response.code(BetLine.OFF_WAGER);
            response.message("please put wager on wager turn.");
            return;
        }
        if(this.betLineListener.onWager(response.owner(),response.entryCost())){
            BetLine wg = new BetLine(response.stub(),response.x,response.y,response.subscript,response.owner(),response.entryCost());
            _onWager(wg);
            response.code(BetLine.ON_WAGER);
            response.message("wagered");
        }else{
            response.code(BetLine.NO_FUND);
            response.message("please cash in more in game");
        }
    }
    public synchronized void onDealer(Response response){
        if(onTurn!=ON_WAGER||houseSeat.occupied){
            response.code(BetLine.OFF_WAGER);
            response.message("Please try on wager turn.");
        }
        if(this.betLineListener.onWager(response.owner(),this.dealerSeatFee)){
            houseSeat.systemId(response.owner());
            houseSeat.occupied = true;
            this.houseSeat.balance(this.betLineListener.balance(response.owner()));
            Seat seat = this.seatList.get(response.owner());
            seat.balance(this.betLineListener.balance(response.owner()));
            if(seat.subscript>=0){
                Seat oss = onStage[seat.subscript];
                oss.occupied = false;
                oss.systemId(null);
                pendingQueue.offer(oss);
                seat.subscript = -1;
            }
            this.pendingQueue.offer(houseSeat);
            response.code(BetLine.ON_WAGER);
            response.message("You are playing as the dealer.");

        }else{
            response.code(BetLine.NO_FUND);
            response.message("Please cash in more in game.");
        }
    }
    public synchronized void offDealer(Response response){
        if(onTurn==ON_WAGER&&houseSeat.asPlayer()&&response.owner().equals(houseSeat.systemId())){
            houseSeat.occupied =false;
            houseSeat.systemId(null);
            this.houseSeat.balance(this.betLineListener.onHouse(0));
            Seat seat = this.seatList.get(response.owner());
            seat.balance(this.betLineListener.balance(response.owner()));
            this.pendingQueue.offer(houseSeat);
            response.code(BetLine.ON_WAGER);
            response.message("You left the dealer seat.");
        }
        else{
            response.code(BetLine.OFF_WAGER);
            response.message("You cannot leave the dealer seat.");
        }
    }

    @Override
    public void start() throws Exception {
        this.deck = new Deck(deckSize);
        this.countOnRanks = new CountOnRank[10];
        for(int i=0;i<10;i++){
            countOnRanks[i]=new CountOnRank(i+1,this.label);
        }
        this.deck.label("baccarat");
        this.betLineList = new CopyOnWriteArrayList<>();
        this.balance = this.betLineListener.onHouse(0);
        this.playerHand = new BaccaratPlayerHand(this);
        this.bankerHand = new BaccaratBankerHand(this);
        this.baccaratTieBetLine = new BaccaratTieBetLine(this);
        this.houseSeat = new Seat();
        this.houseSeat.balance(this.betLineListener.onHouse(0));
        this.houseSeat.name("dealerSeat");
        this.houseSeat.label("baccarat");
        this.onStage = new Seat[11];
        for(int i=0;i<11;i++){
            this.onStage[i]=new Seat(i);
            this.onStage[i].name("seat");
            this.onStage[i].label("baccarat");
        }
        this._round();
    }

    @Override
    public void shutdown() throws Exception {

    }
    public static class WagerTurn extends BaccaratTurn {
        public WagerTurn(Baccarat baccarat){
            this.baccarat = baccarat;
            this.duration = Game.CHECK_POINT_INTERVAL*4;
            this.event = "Wager";
            this.name = "turn";
            this.label = "baccarat";
        }

        @Override
        public void reset() {
            if(SystemUtil.timeout(timestamp)) {
                if(!baccarat.onDeal()){
                    this.timestamp = SystemUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(duration));
                }
            }
            else{
                //per wager on each check point
                baccarat.robotOnWager();
            }
        }
        public boolean check(){
            return this.baccarat.onDeal();
        }
    }
    public static class BankerTurn extends BaccaratTurn {
        public BankerTurn(Baccarat baccarat){
            this.baccarat = baccarat;
            this.duration = Game.CHECK_POINT_INTERVAL*2;
            this.event = "Banker";
            this.name = "turn";
            this.label = "baccarat";
        }

        @Override
        public void reset() {
            if(SystemUtil.timeout(timestamp)){
                baccarat.thirdCardOnBanker();
            }
        }
        public boolean check(){
            return baccarat.thirdCardOnBanker();
        }
    }
    public static class PlayerTurn extends BaccaratTurn {
        public PlayerTurn(Baccarat baccarat){
            this.baccarat = baccarat;
            this.duration = Game.CHECK_POINT_INTERVAL*2;
            this.event = "Player";
            this.name = "turn";
            this.label = "baccarat";
        }

        @Override
        public void reset() {
            if(SystemUtil.timeout(timestamp)) {
                baccarat.thirdCardOnPlayer();
            }
        }
        public boolean check(){
            return baccarat.thirdCardOnPlayer();
        }
    }
    public static class PayoutTurn extends BaccaratTurn {
        public PayoutTurn(Baccarat baccarat){
            this.baccarat = baccarat;
            this.duration = Game.CHECK_POINT_INTERVAL*2;
            this.event = "Payout";
            this.name = "turn";
            this.label = "baccarat";
        }

        @Override
        public void reset() {
            if(SystemUtil.timeout(timestamp)) {
                baccarat.onPayout();
            }
        }
        public boolean check(){
            return baccarat.onPayout();
        }
    }
}
