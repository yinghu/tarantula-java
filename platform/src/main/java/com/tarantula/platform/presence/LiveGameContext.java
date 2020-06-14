package com.tarantula.platform.presence;

import java.util.ArrayList;


public class LiveGameContext {

    private ArrayList<String> gameIndex = new ArrayList<>();

    public LiveGameContext(){

    }
    public synchronized void addGameIndex(String name){
        boolean adding = true;
        for(String g : gameIndex){
            if(g.equals(name)){
                adding = false;
                break;
            }
        }
        if(adding){
            gameIndex.add(name);
        }
    }
    public synchronized void removeGameIndex(String name){
        ArrayList<String> _tmp = new ArrayList<>();
        gameIndex.forEach((g)->{
            if(!g.equals(name)){
                _tmp.add(g);
            }
        });
        gameIndex.clear();
        gameIndex.addAll(_tmp);
    }
    public synchronized LiveGame onIndex(int page){
        if(gameIndex.size()==0){
            return null;
        }
        if(page<gameIndex.size()){
            return new LiveGame(page,gameIndex.get(page));
        }
        else{
            return new LiveGame(0,gameIndex.get(0));
        }
    }
}
