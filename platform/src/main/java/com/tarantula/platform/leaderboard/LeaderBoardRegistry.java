package com.tarantula.platform.leaderboard;


import com.tarantula.LeaderBoard;

import java.util.*;

/**
 * Updated by yinghu on 6/15/2018.
 */
public class LeaderBoardRegistry implements LeaderBoard.Registry{
    public String name;
    public int size;
    public Set<String> classifierList = new HashSet<>();
    public Set<String> categoryList = new HashSet<>();
    public String header;

    public LeaderBoardRegistry(String name,String header,int size){
        this.name = name;
        this.header = header;
        this.size = size;
    }

    @Override
    public String toString(){
        StringBuffer bf = new StringBuffer("["+name+"/"+size+"/");
        for(String c : classifierList){
            bf.append(c+"/");
        }
        bf.append("]");
        return bf.toString();
    }
}
