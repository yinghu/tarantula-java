package com.tarantula.platform.service.deployment;


import java.util.Comparator;

/**
 * Updated by yinghu lu on 10/9/2018.
 */
public class LobbyComparator implements Comparator<LobbyConfiguration> {

    public int compare(LobbyConfiguration o1, LobbyConfiguration o2) {
        int diff = o1.descriptor.deployPriority()-o2.descriptor.deployPriority();
        if(diff>0){
            return -1;
        }
        else if(diff<0){
            return 1;
        }
        else{
            return 0;
        }
    }
}
