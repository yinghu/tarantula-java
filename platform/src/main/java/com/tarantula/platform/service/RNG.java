package com.tarantula.platform.service;

/**
 * Updated by yinghu lu on 10/7/2018.
 */
public interface RNG{

    void onIdle();

    int onNext(int bound);

    int[] onNextList(int bound,int size);

    <T extends Object> T[] shuffle(T[] deck);

    int[] shuffle(int[] deck);
}
