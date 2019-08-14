package com.tarantula;

/**
 * Developer: YINGHU LU
 * Date Updated : 5/3/2018
 * Time: 10:21 PM
 * Session time out callback
 */
public interface SessionTimeoutListener {
    void onIdle(Session session);
    void onTimeout(Session session);
}
