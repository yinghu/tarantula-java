package com.tarantula;

/**
 * Updated by yinghu on 9/6/2019
 */
public interface Response extends Recoverable{

    int INSUFFICIENT_BALANCE = 1;
    int ACCESS_MODE_NOT_SUPPORTED = 3;
    int INSTANCE_FULL = 4;

    String command();
    void command(String command);

    int code();
    void code(int code);

    String message();
    void message(String message);

    boolean successful();
    void successful(boolean successful);

}
