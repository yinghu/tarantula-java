package com.icodesoftware;

public interface OnView extends Configurable {

    String LABEL ="LVT";
    String INVALID_VIEW_ID = "invalid.request";

    String viewId();
    void viewId(String viewId);

    String moduleContext();
    void moduleContext(String moduleContext);

    String moduleResourceFile();
    void moduleResourceFile(String moduleResourceFile);

}
