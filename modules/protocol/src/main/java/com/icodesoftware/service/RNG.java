package com.icodesoftware.service;

/**
 * Updated by yinghu lu on 12/11/2020
 */
public interface RNG {

    void onIdle();

    int onNext(int bound);

    int[] onNextList(int bound,int size);

    <T extends Object> T[] shuffle(T[] deck);

    int[] shuffle(int[] deck);
}
