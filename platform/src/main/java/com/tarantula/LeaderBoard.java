package com.tarantula;

/**
 * Updated by yinghu on 4/30/20
 */
public interface LeaderBoard extends Updatable{

    int size();
    String category(); //The category of statistics eg WonCount, LostCount

    void onBoard(String systemId,double value);

    void reset();
    void registerListener(Listener listener);

    default Board daily(){
        return null;
    }
    default Board weekly(){
        return null;
    }
    default Board total(){
        return null;
    }
    interface  Board extends Updatable{
        Entry[] list();
    }
    interface Entry extends Recoverable,Updatable{
        int rank();
        String category();
        String classifier();
        double value();
        Entry update(Entry entry);
    }
    interface Reset{
        boolean reset(LeaderBoard leaderBoard);
    }
    interface Listener{
        void onUpdated(Entry entry);
    }
}
