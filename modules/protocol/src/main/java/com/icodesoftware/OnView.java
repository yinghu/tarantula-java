package com.icodesoftware;

/**
 * Updated by yinghu lu on 7/11/2020
 */
public interface OnView extends Configurable {

    String LABEL ="LVT";
    String INVALID_VIEW_ID = "invalid.request";

    String viewId();
    void viewId(String viewId);

    String flag();
    void flag(String flag);

    String moduleContext();
    void moduleContext(String moduleContext);

    String moduleResourceFile();
    void moduleResourceFile(String moduleResourceFile);

}
