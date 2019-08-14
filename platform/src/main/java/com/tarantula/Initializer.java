package com.tarantula;

/**
 * Created by yinghu lu on 4/27/2018.
 */
public interface Initializer {

    void setup(ApplicationContext context) throws Exception;

    Descriptor descriptor();

    void descriptor(Descriptor descriptor);

}
