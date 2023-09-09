package com.icodesoftware;

public interface OnSession extends OnApplication, Response {

    String LABEL = "onSession";

    String token();
    void token(String token);

    String login();
    void login(String login);

}
