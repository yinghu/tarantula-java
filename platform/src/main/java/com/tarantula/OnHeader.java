package com.tarantula;

import java.util.Map;

/**
 * Updated by yinghu lu on 10/8/2018
 */
public interface OnHeader {

    void header(String header,String value);
    String header(String header);

    Map<String,String> header();
}
