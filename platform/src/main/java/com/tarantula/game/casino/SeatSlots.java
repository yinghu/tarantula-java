package com.tarantula.game.casino;

public class SeatSlots {
    private boolean[] occupied;
    private int max;
    public SeatSlots(int n){
        occupied = new boolean[n];
        for(int i=0;i<n;i++){
            occupied[i]=false;
        }
        max = n;
    }
    public synchronized int onSlot(){
        int slot = -1;
        for (int i=0;i<max;i++){
            if(!occupied[i]){
                occupied[i]=true;
                slot =  i;
                break;
            }
        }
        return slot;
    }
    public synchronized boolean offSlot(int slot){
        if(slot>=0&&slot<max){
            occupied[slot]=false;
            return true;
        }
        else{
            return false;
        }
    }
}
