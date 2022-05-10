package com.icodesoftware;

public interface Response extends Recoverable {

    int INSUFFICIENT_BALANCE = 1;
    int ACCESS_MODE_NOT_SUPPORTED = 3;
    int INSTANCE_FULL = 4;

    String RESPONSE_SUCCESSFUL = "successful";
    String RESPONSE_MESSAGE = "message";
    String RESPONSE_CODE = "code";
    String RESPONSE_COMMAND = "command";



    String command();
    void command(String command);

    int code();
    void code(int code);

    String message();
    void message(String message);

    boolean successful();
    void successful(boolean successful);

}
