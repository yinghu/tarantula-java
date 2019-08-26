package com.tarantula;

/**
 * Updated by yinghu lu on 8/26/2019.
 */
public interface OnView extends Recoverable {

    String viewId();
    void viewId(String viewId);

    String flag();
    void flag(String flag);
    String category();
    void category(String category);
    String icon();
    void icon(String icon);

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
