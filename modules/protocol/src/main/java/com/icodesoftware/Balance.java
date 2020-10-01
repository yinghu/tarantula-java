package com.icodesoftware;


public interface Balance extends Recoverable {

    double balance();
    boolean transact(double delta);
}
