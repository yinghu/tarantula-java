package com.icodesoftware.util;

import com.icodesoftware.service.RNG;

import java.security.SecureRandom;

/**
 * updated by yinghu lu on 12/11/2020.
 */
public class JvmRNG extends SecureRandom implements RNG{

    private long m = 1;
    private long r = 0;

    public JvmRNG(){
    }

    public synchronized int onNext(int bound){
        return _nextInt(bound);
    }
    public synchronized void onNextList(int bound,int[] stops){
        for(int i=0;i<stops.length;i++){
            stops[i]=_nextInt(bound);
        }
    }
    public synchronized int[] onNextList(int bound,int size){
        int[] rlist = new int[size];
        for(int i=0;i<size;i++){
            rlist[i]=_nextInt(bound);
        }
        return rlist;
    }
    public synchronized void onIdle(){
        next(32);
    }
    private int _nextInt(int n) {
        while (true) {
            if (m < 0x80000000L) {
                m <<= 32;
                r <<= 32;
                r += (long)next(32) - Integer.MIN_VALUE;
            }
            long q = m / n;
            if (r < n * q) {
                int x = (int)(r % n);
                m = q;
                r /= n;
                return x;
            }
            m -= n * q;
            r -= n * q;
        }
    }
    public synchronized <T extends Object> T[] shuffle(T[] deck){
        int size = deck.length-1;
        for (int i=size;i>0;i--) {
            int _rx = this._nextInt(i+1);
            T tmp = deck[_rx];
            deck[_rx] = deck[i];
            deck[i] = tmp;
        }
        return deck;
    }
    public int[] shuffle(int[] deck){
        int size = deck.length-1;
        for (int i=size;i>0;i--) {
            int _rx = this._nextInt(i+1);
            int tmp = deck[_rx];
            deck[_rx] = deck[i];
            deck[i] = tmp;
        }
        return deck;
    }
}
