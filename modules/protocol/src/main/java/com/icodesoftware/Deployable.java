package com.icodesoftware;

public interface Deployable extends Recoverable{
    default String filter(){return null;}
}
