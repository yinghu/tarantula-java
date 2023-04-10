package com.icodesoftware.protocol;

public interface GameServiceProvider {

    void updateStatistics(String system,String name,double delta);
    void updateExperience(String system,double delta);
}
