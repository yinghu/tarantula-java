package com.tarantula;

import java.util.List;

/**
 * Updated 4/24/2018 yinghu lu
 */
public interface PlayList extends Recoverable{

    String name();
    void name(String name);
    /**
     * size of the list
     * */
    int size();
    void size(int size);

    /**
     * open to link from member of the list if true
     * */
    boolean open();
    void open(boolean open);



    List<OnPlay> onPlay();
    boolean onPlay(OnPlay onPlay);


}
