package com.icodesoftware;

public interface Filterable extends Recoverable{
    default String filter(){return "";}
    default void filter(String filter){}
}
