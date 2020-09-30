package com.tarantula;


import com.icodesoftware.Recoverable;

public interface Balance extends Recoverable {

    double balance();
    boolean transact(double delta);
}
