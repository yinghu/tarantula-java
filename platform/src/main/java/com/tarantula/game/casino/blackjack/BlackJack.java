package com.tarantula.game.casino.blackjack;
import com.tarantula.Response;
import com.tarantula.Serviceable;
import com.tarantula.game.Game;
import com.tarantula.game.GameRecoverableRegistry;
import com.tarantula.game.casino.*;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yinghu lu on 11/28/2018.
 */
public class BlackJack extends Game implements Serviceable {

    public static final int MAX_HANDS = 3; //TWO SPLITS

    public static final int ON_WAGER = 1;
    public static final int ON_DEAL = 2;
    public static final int ON_HIT = 3;
    public static final int ON_PAYOUT = 4;

    public Deck deck;



    public BlackJackSeat[] seatList = new BlackJackSeat[]{new BlackJackSeat(0,true),new BlackJackSeat(1),new BlackJackSeat(2),new BlackJackSeat(3),new BlackJackSeat(4),new BlackJackSeat(5)};

    public boolean splittable;

    public int onTurn;
    public int round;
    public int counter;
    public int cutter;

    public Card holeCard = Card.BJ;


    public ConcurrentHashMap<String,Seat> indexing = new ConcurrentHashMap();

    public BlackJackTurn blackJackTurn;
    //1 OnWager --> wagering->dealing
    //max 5 OnPlayer --> hit, stand ,etc --> standing ( numbers of the players )
    //1 OnDealer --> soft 17 --> payout --reset to OnWager

    public List<BlackJackSeat> onSeat = new ArrayList<>();


    public int[] dealerRankList;

    public double payout;

    public BlackJack(){
        this.vertex = "Blackjack";
        this.label = "blackjack";
    }
    public synchronized BlackJack setup(){
        /**
        BlackJack bj = new BlackJack();
        bj.tournamentEnabled = this.tournamentEnabled;
        bj.deck = this.deck;
        bj.instanceId(this.instanceId);
        bj.entryCost(this.entryCost);
        bj.dealerSeatFee = this.dealerSeatFee;
        bj.splittable = this.splittable;
        bj.minWager = this.minWager;
        bj.maxWager = this.maxWager;
        bj.round = this.round;
        bj.onTurn = this.onTurn;
        bj.counter = this.deck.index;
        bj.cutter = this.deck.cutter;
        bj.deckSize = this.deckSize;
        bj.blackJackTurn = (BlackJackTurn) this.currentCheckPoint;
        bj.name(this.name);
        bj.betLineList = this.betLineList;
        bj.onTournament = this.onTournament;
        bj.onStatistics = this.onStatistics;
        bj.dealerRankList = this.dealerRankList;**/
        return this;
    }

    public synchronized void onShuffle(){
        this.deck._shuffle();
    }
    public void _round(){
        onTurn = ON_WAGER;
        round++;
        this.betLineListener.onStatistics("totalRounds",1,1);
        tQueue.clear();
        this.currentCheckPoint = new WagerTurn(this);
        this.currentCheckPoint.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(currentCheckPoint.duration())));

    }
    public synchronized void onDealerSeat(Response response){
        if(seatList[0].asPlayer()||this.onTurn!=ON_WAGER){
            response.code(BetLine.OFF_WAGER);
            response.message("Join as dealer on wager turn!");
            return;
        }
        BlackJackSeat ds = seatList[0];
        if(this.betLineListener.onWager(response.owner(),dealerSeatFee)){
            Seat onTable = this.indexing.get(response.owner());
            if(onTable.subscript>0) {//release seat
                seatList[onTable.subscript].occupied = false;
                this.pendingQueue.offer(new Seat(response.owner(), onTable.subscript, false, 0, "seat", "blackjack"));
            }
            ds.occupied = true;
            ds.systemId(response.owner());
            ds.balance(this.betLineListener.balance(response.owner()));
            this.betLineListener.onHouse(dealerSeatFee);
            onTable.subscript=0;
            this.pendingQueue.offer(new Seat(ds.systemId(),0,true,ds.balance(),"dealerSeat","blackjack"));
            response.code(BetLine.ON_WAGER);
            response.message("Joined to play as the dealer");
        }else{
            response.code(BetLine.NO_FUND);
            response.message("Not enough balance!");
        }
    }
    public synchronized void offDealerSeat(Response response){
        if(seatList[0].asPlayer(response.owner())){
            seatList[0].occupied = false;
            seatList[0].systemId(null);
            seatList[0].balance(this.betLineListener.onHouse(0));
            this.pendingQueue.offer(new Seat(seatList[0].systemId(),0,false,seatList[0].balance(),"dealerSeat","blackjack"));
            Seat onTable = indexing.get(response.owner());
            onTable.subscript = -1;//no player seat
            onTable.balance(this.betLineListener.balance(response.owner()));
            this.pendingQueue.offer(new CashInBalance(onTable.systemId(),onTable.subscript,onTable.balance(),"balance","blackjack"));
            response.code(BetLine.ON_WAGER);
            response.message("Player left dealer seat.");
        }
        else{
            response.code(BetLine.OFF_WAGER);
            response.message("Player not on dealer seat.");
        }
    }
    public synchronized void onJoin(String systemId){
        Seat onTable = new Seat();
        onTable.subscript = -1;
        onTable.systemId(systemId);
        onTable.balance(this.betLineListener.balance(systemId));
        indexing.put(systemId,onTable);
        pendingQueue.offer(onTable);
        this.pendingQueue.offer(new CashInBalance(systemId,onTable.subscript,this.betLineListener.balance(systemId),"balance","blackjack"));
    }
    public synchronized boolean onSeat(int index,String systemId){
        if(seatList[index].asPlayer()||seatList[0].asPlayer(systemId)){
            return false;
        }
        Seat onTable = indexing.get(systemId);
        if(onTable.subscript==-1){
            onTable.subscript = index;
            seatList[index].occupied = true;
            seatList[index].systemId(systemId);
            indexing.get(systemId).subscript = index;
            this.pendingQueue.offer(new Seat(systemId,index,true,onTable.balance(),"seat","blackjack"));
            return true;
        }else {
            BlackJackSeat fs = seatList[onTable.subscript];
            fs.occupied = false;
            this.pendingQueue.offer(new Seat(systemId, onTable.subscript, false, 0, "seat", "blackjack"));
            seatList[index].occupied = true;
            seatList[index].systemId(systemId);
            onTable.subscript = index;
            this.pendingQueue.offer(new Seat(systemId, index, true, onTable.balance(), "seat", "blackjack"));
            return true;
        }
    }
    public synchronized boolean offSeat(String systemId){
        if(onTurn!=ON_WAGER){
            return false;
        }
        Seat ot = indexing.remove(systemId);
        if(ot!=null){
            if(ot.subscript>0){
                seatList[ot.subscript].occupied = false;
                this.pendingQueue.offer(new Seat(systemId,ot.subscript,false,0,"seat","blackjack"));
            }
            else if(ot.subscript==0){
                seatList[0].occupied = false;
                seatList[0].balance(this.betLineListener.onHouse(0));
                this.pendingQueue.offer(new Seat(systemId(),0,false,seatList[0].balance(),"dealerSeat","blackjack"));
            }
        }
        return true;
    }
    private void kickOffFromDealerSeat(){
        BlackJackSeat rm = seatList[0];
        Seat ot = indexing.get(rm.systemId());
        ot.subscript=-1;
        this.pendingQueue.offer(new CashInBalance(ot.systemId(),ot.subscript,this.betLineListener.balance(ot.systemId()),"balance","blackjack"));
        rm.occupied = false;
        rm.systemId(null);
        rm.balance(this.betLineListener.onHouse(0));
        this.pendingQueue.offer(new Seat(null,0,false,seatList[0].balance(),"dealerSeat","blackjack"));
    }
    public synchronized void robotOnWager(){
        if(this.onTurn!=ON_WAGER||this.tournamentEnabled){
            return;
        }
        BetLine wg = new BetLine(100,0,0,LocalDateTime.now().getSecond()%5+1,systemId,this.minWager*(LocalDateTime.now().getSecond()%10+1));
        wg.label("blackjack");
        _onWager(wg);
    }
    private void _onWager(BetLine bet){
        seatList[bet.subscript].wagered=true;
        seatList[bet.subscript].wager(seatList[bet.subscript].wager()+bet.wager());
        if(seatList[0].asPlayer()){//occupied plus systemId !=null
            this.betLineListener.onPayout(seatList[0].systemId(),bet.wager());
            seatList[0].balance(this.betLineListener.balance(seatList[0].systemId()));
        }else{
            seatList[0].balance(this.betLineListener.onHouse(bet.wager()));
        }
        seatList[bet.subscript].wagerList.add(bet);
        this.betLineListener.onStatistics("totalWager",bet.wager(),2);
        this.pendingQueue.offer(new CashInBalance(seatList[0].systemId(),-1,seatList[0].balance(),"dealer","blackjack"));
        bet.balance(seatList[bet.subscript].wager());
        this.pendingQueue.offer(bet);
        betLineList.add(bet);
        if(bet.systemId()!=null){
            Seat onTable = indexing.get(bet.systemId());
            onTable.balance(this.betLineListener.balance(bet.systemId()));
            pendingQueue.offer(new CashInBalance(bet.systemId(),onTable.subscript,onTable.balance(),"balance","blackjack"));
            onTable.wagered = true;
        }
    }
    public synchronized boolean onWager(double wager,String systemId,int stub,float x, float y){
        if(this.onTurn!=ON_WAGER||(stub<=0||stub>5)||seatList[0].asPlayer(systemId)){
            return false;
        }
        //wager always is on the seat picked by player
        if((!seatList[stub].bank)&&this.betLineListener.onWager(systemId,wager)){
            BetLine wg = new BetLine(100,x,y,stub,systemId,wager);
            wg.label("blackjack");
            _onWager(wg);
            return true;
        }
        else{
            return false;
        }
    }

    public synchronized void onBalance(String systemId){
        Seat onTable = indexing.get(systemId);
        if(onTable!=null){
            onTable.balance(this.betLineListener.balance(systemId));
            this.pendingQueue.offer(new CashInBalance(systemId,onTable.subscript,onTable.balance(),"balance","blackjack"));
        }
    }
    public synchronized boolean onDeal(){
        if(onTurn!=ON_WAGER){
            return false;
        }
        int hds = 0;
        for(int i=1;i<6;i++){
            BlackJackSeat position = seatList[i];
            if(position.wagered){
                position.hands=new BlackJackHand[1];
                position.hands[0]=new BlackJackHand(0);
                position.hands[0].deal(deck.draw(),deck.draw());
                position.hands[0].standing = (position.hands[0].rank()==21);//standing on blackjack
                position.hands[0].natural = (position.hands[0].rank()==21);
                position.hands[0].wager(position.wager());
                position.dealing = true;
                if(!position.hands[0].standing){
                    if(position.occupied){
                        this.tQueue.offer(new ActionTurn(this,position.systemId(),i));
                    }else{
                        this.tQueue.offer(new AutoTurn(this,i));
                    }
                }
                else{
                    betLineListener.onStatistics("totalBlackjack",1,0);
                }
                this.onTurn = ON_DEAL;
                this.pendingQueue.offer(deck);
                this.pendingQueue.offer(position);
                hds++;
            }
        }
        if(this.onTurn==ON_DEAL){
            holeCard = deck.draw();
            seatList[0].hands = new BlackJackHand[1];
            seatList[0].hands[0] = new BlackJackHand(0);
            seatList[0].hands[0].deal(deck.draw(),Card.BJ);
            seatList[0].dealing = true;
            this.tQueue.offer(new DealerFaceUpTurn(this));
            this.tQueue.offer(new DealerTurn(this));
            this.tQueue.offer(new PayoutTurn(this));
            this.pendingQueue.offer(deck);
            this.pendingQueue.offer(seatList[0]);
            _nextTurn();
            hds++;
            this.betLineListener.onStatistics("totalHands",hds,4);
        }
        return onTurn == ON_DEAL;
    }
    public synchronized boolean onHit(String systemId,int subscript){
        int ix = this.indexing.get(systemId).subscript;
        if((ix==-1)||(!this.currentCheckPoint.onTurn(systemId))){
            return false;
        }
        boolean suc = false;
        if(seatList[ix].dealing&&(!seatList[ix].hands[subscript].standing)){
            int pts = seatList[ix].hands[subscript].hit(deck.draw());
            if(pts>=21){
                seatList[ix].hands[subscript].standing = true;
                _nextTurn();
            }
            suc = true;
            this.pendingQueue.offer(deck);
            this.pendingQueue.offer(seatList[ix]);
        }
        return suc;
    }
    public synchronized boolean onStand(String systemId,int subscript) {
        int ix = this.indexing.get(systemId).subscript;
        if((ix==-1)||(!this.currentCheckPoint.onTurn(systemId))){
            return false;
        }
        boolean suc = false;
        if (seatList[ix].dealing) {
            suc = seatList[ix].hands[subscript].stand();
            this.pendingQueue.offer(seatList[ix]);
            _nextTurn();
        }
        return suc;
    }
    public synchronized void onStandAll(int idx) {
        boolean suc = false;
        if (seatList[idx].dealing) {
            for(BlackJackHand h : seatList[idx].hands) {
                if(!h.standing){
                    _robotTurn(h);
                    suc = true;
                }
            }
        }
        if(suc){
            this.pendingQueue.offer(seatList[idx]);
            _nextTurn();
        }
    }
    private void _robotTurn(BlackJackHand hand){
        if(hand.rank()<17){ //do action if less than 17
            if(hand.rank()<=11){
                do{
                    hand.hit(deck.draw());
                }while (hand.rank()<17);
            }
            else{
                BlackJackHand dealer = seatList[0].hands[0];
                if(dealer.rank()>6){
                    do{
                        hand.hit(deck.draw());
                    }while (hand.rank()<17);
                }
            }
        }
        hand.stand();
    }
    public synchronized boolean onDoubleDown(String systemId,int subscript){
        int ix = this.indexing.get(systemId).subscript;
        if((ix==-1)||(!this.currentCheckPoint.onTurn(systemId))){
            return false;
        }
        boolean suc = false;
        if(seatList[ix].dealing&&(!seatList[ix].hands[subscript].standing)){
            BlackJackHand _h = seatList[ix].hands[subscript];
            if(this.betLineListener.onWager(systemId,_h.wager())){
                seatList[ix].hands[subscript].hit(deck.draw());
                seatList[ix].hands[subscript].standing = true;
                seatList[ix].wager(seatList[ix].wager()+_h.wager());
                if(seatList[0].asPlayer()){
                    this.betLineListener.onPayout(seatList[0].systemId(),_h.wager());
                    seatList[0].balance(this.betLineListener.balance(seatList[0].systemId()));
                }else{
                    this.seatList[0].balance(this.betLineListener.onHouse(_h.wager()));
                }
                BetLine wg = new BetLine(100,0,0,stub,systemId,_h.wager());
                seatList[stub].wagerList.add(wg);//add payout
                wg.label("blackjack");
                this.pendingQueue.offer(new CashInBalance(systemId,-1,this.betLineListener.balance(systemId),"balance","blackjack"));
                this.betLineListener.onStatistics("totalWager",_h.wager(),2);
                _h.wager(_h.wager()*2);
                _nextTurn();
                this.pendingQueue.offer(deck);
                this.pendingQueue.offer(seatList[0]);//dealer update
                this.pendingQueue.offer(seatList[ix]);//seat update
                suc = true;
            }
        }
        return suc;
    }
    public synchronized boolean onSplit(String systemId,int subscript){
        int ix = this.indexing.get(systemId).subscript;
        if((!splittable)||(ix==-1)||(!this.currentCheckPoint.onTurn(systemId))){
            return false;
        }
        boolean suc = false;
        if(seatList[ix].hands[subscript].splittable()&&seatList[ix].dealing&&seatList[ix].hands.length<MAX_HANDS){
            BlackJackHand _h = seatList[ix].hands[subscript];
            if(this.betLineListener.onWager(systemId,_h.wager())){
                BlackJackHand[] hs = seatList[ix].hands[subscript].split();
                seatList[ix].hands[subscript]=hs[0];
                hs[0].hit(deck.draw());
                hs[0].wager(_h.wager());
                BlackJackHand[] xlist = Arrays.copyOf(seatList[ix].hands,seatList[ix].hands.length+1);
                xlist[xlist.length-1]=hs[1];
                hs[1].hit(deck.draw());
                hs[1].wager(_h.wager());
                seatList[ix].wager(seatList[ix].wager()+_h.wager());
                seatList[ix].hands = xlist;
                suc = true;
            }
        }
        return suc;
    }
    public synchronized boolean onFaceUp(){
        boolean rt = _onFaceUp();
        this.pendingQueue.offer(seatList[0]);
        return rt;
    }
    public boolean _onFaceUp(){
        if(onTurn==ON_DEAL){
            BlackJackHand dealer = seatList[0].hands[0];
            if(dealer.swap(this.holeCard)>=17){
                onTurn = ON_PAYOUT;
            }
            else{
                onTurn = ON_HIT;
            }
            _nextTurn();
        }
        return onTurn == ON_PAYOUT;
    }
    public synchronized boolean onSoft17(){
        boolean rt = _onSoft17();
        this.pendingQueue.offer(seatList[0]);
        return rt;
    }
    public boolean _onSoft17(){
        if(onTurn==ON_HIT){
            BlackJackHand dealer = seatList[0].hands[0];
            if(dealer.hit(deck.draw())>=17){
                onTurn = ON_PAYOUT;
            }
            this.pendingQueue.offer(deck);
        }
        return onTurn == ON_HIT;
    }
    public synchronized void onDealerTurn(){
        boolean hit = this.onTurn==ON_HIT;
        do{
            if(hit){
                hit = this._onSoft17();
                this.pendingQueue.offer(seatList[0]);
            }
        }while (hit);
        seatList[0].hands[0].stand();
        for(int i=0;i<8;i++){
            if(i<7){
                dealerRankList[i]=dealerRankList[i+1];
            }
            else{
                dealerRankList[i]=seatList[0].hands[0].rank();
            }
        }
        this.pendingQueue.offer(seatList[0]);
        this._nextTurn();
    }
    public synchronized void onPayout(){
        if(this.onTurn==ON_PAYOUT){
            BlackJackHand dealer = seatList[0].hands[0];
            this.payout();
            dealer.hand=null;
            seatList[0].dealing = false;
            seatList[0].wager(0);
            this.betLineList.clear();
            this.pendingQueue.offer(seatList[0]);
            this.pendingQueue.offer(new CashInBalance(seatList[0].systemId(),-1,seatList[0].balance(),"dealer","blackjack"));
            this._round();
        }
    }
    private void payout(){
        BlackJackHand dealer = seatList[0].hands[0];
        double _payTotal = 0;
        if(dealer.rank()<=21&&(!dealer.natural)){ //dealer points
            for(int i=1;i<6;i++){
                BlackJackSeat position = seatList[i];
                if(position.dealing){
                    for(BlackJackHand h: position.hands){
                        double _pay = 0;
                        if(h.rank()<=21&&h.rank()>dealer.rank()){
                            _pay = position.payout(h.natural?3:2);
                        }
                        else if(h.rank()==dealer.rank()){
                            _pay = position.payout(1);;//push
                        }
                        if(_pay>0){
                            _payTotal +=_pay;
                            if(seatList[0].asPlayer()){
                                if(this.betLineListener.onWager(seatList[0].systemId(),_pay)){
                                    seatList[0].balance(this.betLineListener.balance(seatList[0].systemId()));
                                }
                                else{
                                    //kick off from sealer seat
                                    kickOffFromDealerSeat();
                                }
                            }else{
                                seatList[0].balance(this.betLineListener.onHouse((-1)*_pay));
                            }
                        }
                    }
                    position.hands=null;
                    position.wager(0);
                    position.wagered = false;
                    position.dealing = false;
                    position.wagerList.clear();
                    this.pendingQueue.offer(position);
                }
            }

        }
        else if(dealer.rank()>21){//dealer bust
            for(int i=1;i<6;i++){
                BlackJackSeat position = seatList[i];
                if(position.dealing){
                    for(BlackJackHand h: position.hands){
                        double _pay = 0;
                        if(h.rank()<=21){
                            _pay= position.payout(h.natural?3:2);
                        }
                        if(_pay>0){
                            _payTotal +=_pay;
                            if(seatList[0].asPlayer()){
                                if(this.betLineListener.onWager(seatList[0].systemId(),_pay)){
                                    seatList[0].balance(this.betLineListener.balance(seatList[0].systemId()));
                                }
                                else{
                                    //kick off from sealer seat
                                    kickOffFromDealerSeat();
                                }
                            }else{
                                seatList[0].balance(this.betLineListener.onHouse((-1)*_pay));
                            }
                        }
                    }
                    position.hands=null;
                    position.wager(0);
                    position.wagered = false;
                    position.dealing = false;
                    position.wagerList.clear();
                    this.pendingQueue.offer(position);
                }
            }
        }
        else if(dealer.natural){//dealer black jack
            betLineListener.onStatistics("totalBlackjack",1,0);
            for(int i=1;i<6;i++){
                BlackJackSeat position = seatList[i];
                if(position.dealing){
                    for(BlackJackHand h: position.hands){
                        double _pay = 0;
                        if(h.natural){
                            _pay=position.payout(1);;//push
                        }
                        if(_pay>0){
                            _payTotal +=_pay;
                            if(seatList[0].asPlayer()){
                                if(this.betLineListener.onWager(seatList[0].systemId(),_pay)){
                                    seatList[0].balance(this.betLineListener.balance(seatList[0].systemId()));
                                }
                                else{
                                    //kick off from sealer seat
                                    kickOffFromDealerSeat();
                                }
                            }else{
                                seatList[0].balance(this.betLineListener.onHouse((-1)*_pay));
                            }
                        }
                    }
                    position.hands=null;
                    position.wager(0);
                    position.wagered = false;
                    position.dealing = false;
                    position.wagerList.clear();
                    this.pendingQueue.offer(position);
                }
            }
        }
        if(_payTotal>0){
            betLineListener.onStatistics("totalPayout",_payTotal,3);
        }
    }

    @Override
    public int getFactoryId() {
        return GameRecoverableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GameRecoverableRegistry.BLACKJACK_CID;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",this.round);
        this.properties.put("2",this.onTurn);
        this.properties.put("3",this.holeCard.sequence);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.round = ((Number)properties.get("1")).intValue();
        this.onTurn = ((Number)properties.get("2")).intValue();
        this.holeCard.sequence = ((Number)properties.get("3")).intValue();
    }
    @Override
    public String toString(){
        return "Blackjack["+oid+"/"+round+"/"+onTurn+"/"+holeCard.sequence+"]";
    }
    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
    @Override
    public void start() throws Exception {
        this.seatList[0].balance(this.betLineListener.onHouse(0));
        this.deck = new Deck(deckSize);
        this.deck.label("blackjack");
        this.betLineList = new CopyOnWriteArrayList<>();
        this.dealerRankList = new int[8];
        for(BlackJackSeat bjs : seatList){
            bjs.blackJack = this;
            this.onSeat.add(bjs);
        }
        this._round();
    }

    @Override
    public void shutdown() throws Exception {

    }

    public static class WagerTurn extends BlackJackTurn{
        public WagerTurn(BlackJack blackJack){
            this.blackJack = blackJack;
            this.duration = BlackJack.CHECK_POINT_INTERVAL*4;
            this.event = "Wager";
            this.name = "turn";
            this.label = "blackjack";
        }

        @Override
        public void reset() {
            if(SystemUtil.timeout(timestamp)) {
                if(!blackJack.onDeal()){
                    this.timestamp = SystemUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(duration));
                }
            }
            else{
                //per wager on each check point
                blackJack.robotOnWager();
            }
        }
    }
    public static class DealerFaceUpTurn extends BlackJackTurn {

        public DealerFaceUpTurn(BlackJack blackJack){
            this.blackJack = blackJack;
            this.duration = BlackJack.CHECK_POINT_INTERVAL;
            this.event = "Face Up";
            this.name = "turn";
            this.label = "blackjack";
        }

        @Override
        public void reset() {
            //duration = duration-delta;
            if(SystemUtil.timeout(timestamp)) {
                this.blackJack.onFaceUp();
            }
        }
    }
    public static class DealerTurn extends BlackJackTurn {

        public DealerTurn(BlackJack blackJack){
            this.blackJack = blackJack;
            this.duration = BlackJack.CHECK_POINT_INTERVAL;
            this.event = "Dealer";
            this.name = "turn";
            this.label = "blackjack";
        }

        @Override
        public void reset() {
            //duration = duration-delta;
            if(SystemUtil.timeout(timestamp)) {
                this.blackJack.onDealerTurn();
            }
        }
    }
    public static class ActionTurn extends BlackJackTurn {


        public ActionTurn(BlackJack blackJack,String systemId,int seat){
            this.blackJack = blackJack;
            this.systemId = systemId;
            this.duration = BlackJack.CHECK_POINT_INTERVAL*2;
            this.event = "Player";
            this.name = "turn";
            this.label = "blackjack";
            this.seat = seat;
        }

        @Override
        public void reset() {
            //duration = duration - delta;
            if(SystemUtil.timeout(timestamp)){
                this.blackJack.onStandAll(seat);
            }
        }
    }
    public static class PayoutTurn extends BlackJackTurn {

        public PayoutTurn(BlackJack blackJack){
            this.blackJack = blackJack;
            this.duration = BlackJack.CHECK_POINT_INTERVAL*2;
            this.event = "Payout";
            this.name = "turn";
            this.label = "blackjack";
        }

        @Override
        public void reset() {
            //duration = duration -delta;
            if(SystemUtil.timeout(timestamp)){
                blackJack.onPayout();
            }
        }
    }
    public static class AutoTurn extends BlackJackTurn {


        public AutoTurn(BlackJack blackJack,int seat){
            this.blackJack = blackJack;
            this.duration = BlackJack.CHECK_POINT_INTERVAL/2;
            this.event = "Robot";
            this.name = "turn";
            this.label = "blackjack";
            this.seat = seat;
        }

        @Override
        public void reset() {
            //duration = duration - delta;
            if(SystemUtil.timeout(timestamp)){
                this.blackJack.onStandAll(seat);
            }
        }
    }
}
