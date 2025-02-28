package com.icodesoftware;

public interface Initializer {

    void setup(ApplicationContext context) throws Exception;

    Descriptor descriptor();

}
