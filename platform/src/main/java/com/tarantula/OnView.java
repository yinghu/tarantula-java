package com.tarantula;

/**
 * Updated by yinghu lu on 7/11/2020
 */
public interface OnView extends Recoverable {

    String LABEL ="LVT";

    String viewId();
    void viewId(String viewId);

    String flag();
    void flag(String flag);

    String contentBaseUrl();
    void contentBaseUrl(String contentBaseUrl);


    String moduleFile();
    void moduleFile(String moduleFile);

    String moduleName();
    void moduleName(String moduleName);

    String moduleResourceFile();
    void moduleResourceFile(String moduleResourceFile);

    interface Listener{
        void onView(OnView onView);
    }
}
