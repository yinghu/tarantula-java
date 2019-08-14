package com.tarantula.game.scc;

import com.tarantula.RNG;
import com.tarantula.Recoverable;
import com.tarantula.game.*;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.util.JvmRNG;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ShipCaptainCrew extends CheckPoint {

    public static int ON_SCC = 0;
    public static int ON_CARGO = 1;
    public static int MAX_ROLLS = 5;


    private RNG rnd = new JvmRNG();

    private int[] diceSet = new int[]{1,2,3,4,5,6};
    public boolean ship;
    public boolean captain;
    public boolean crew;
    public Cargo cargo = new Cargo();
    public int onTurn;
    private List<DiceSide> released = new ArrayList<>();
    private DiceSideComparator comparator = new DiceSideComparator();
    public DiceSide[] board; //30, 24, 18, 12
    public int dicePickRemaining;
    public int rollRemaining;
    public boolean roundOver;

    public GameComponentListener gameComponentListener;

    public ShipCaptainCrew(){
        this.vertex = "ShipCaptainCrew";
        this.broadcasting = false;
        this.duration = 4;
        //this.onTournament = new OnPotGame();
    }

    public boolean pick(int subscript){
        DiceSide d = board[subscript];
        if(dicePickRemaining>0&&(!d.released)){
            d.released = true;
            released.add(d);
            if(onTurn==ON_SCC&&dicePickRemaining==1){
                Collections.sort(released,comparator);
                for (DiceSide r : released) {
                    if(r.rank==6){
                        ship = true;
                    }
                    if(ship&&r.rank==5){
                        captain = true;
                    }
                    if(ship&&captain&&r.rank==4){
                        crew = true;
                    }
                }
                this.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(this.duration)));
            }
            else if(onTurn==ON_CARGO&&dicePickRemaining==1) {
                //SCORE
                Cargo _cargo = new Cargo();
                _cargo.dice1 = released.get(0).rank;
                _cargo.dice2 = released.get(1).rank;

                if((_cargo.dice1+_cargo.dice2)>(cargo.dice1+cargo.dice2)){
                    cargo = _cargo;
                    //this.onTournament.score(1000000*(cargo.dice1+cargo.dice2));
                    //potGameService.commit(this.onTournament);
                }
                this.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(this.duration)));
            }
            dicePickRemaining--;
            this.roundOver = rollRemaining==0&&dicePickRemaining==0;
            return rollRemaining>=0&&dicePickRemaining==0;
        }else{
            return false;
        }
    }
    public boolean check(){
        return SystemUtil.timeout(this.timestamp);
    }
    public void reset(){
        if(rollRemaining>0){
            shuffle();
        }
        this.gameComponentListener.onUpdated(this);
    }
    public void _reset(){
        rollRemaining = MAX_ROLLS;
        ship =false;
        captain = false;
        crew = false;
        cargo.dice1=0;
        cargo.dice2=0;
        onTurn = ON_SCC;
        roundOver = false;
        shuffle();
    }
    public void release(){
        for(DiceSide diceSide : board){
            diceSide.released = true;
        }
    }
    private void shuffle(){
        int size = 30;
        dicePickRemaining = 5;
        if(ship){
            size = 24;
            dicePickRemaining = 4;
        }
        if(captain){
            size = 18;
            dicePickRemaining = 3;
        }
        if(crew){
            size =12;
            dicePickRemaining = 2;
        }
        if(ship&&captain&&crew){
            onTurn = ON_CARGO;
        }
        else{
            onTurn = ON_SCC;
        }
        released.clear();
        board = new DiceSide[size];
        int ix = 0;
        for(int i=0;i<(dicePickRemaining);i++){
            _shuffle(diceSet);
            for(int j=0;j<6;j++){
                board[ix++]=new DiceSide(diceSet[j],false);
            }
        }
        for (int i=size-1;i>0;i--) {
            int _rx = rnd.onNext(i+1);
            DiceSide tmp = board[_rx];
            board[_rx] = board[i];
            board[i] = tmp;
        }
        rollRemaining--;
    }
    private void _shuffle(int[] alist){
        for (int i=alist.length-1;i>0;i--) {
            int _rx = rnd.onNext(i+1);
            int tmp = alist[_rx];
            alist[_rx] = alist[i];
            alist[i] = tmp;
        }
    }

    @Override
    public int getFactoryId() {
        return GameRecoverableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return GameRecoverableRegistry.SHIP_CAPTAIN_CREW_CID;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("balance",balance);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.balance = ((Number)properties.get("balance")).doubleValue();
    }
    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
}
