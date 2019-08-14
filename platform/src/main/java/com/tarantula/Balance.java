package com.tarantula;


public interface Balance extends Recoverable{

    double balance();
    boolean transact(double delta);
}
