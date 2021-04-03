package com.icodesoftware.service;

public interface RNG {

    void onIdle();

    int onNext(int bound);

    int[] onNextList(int bound,int size);

    <T extends Object> T[] shuffle(T[] deck);

    int[] shuffle(int[] deck);
}
