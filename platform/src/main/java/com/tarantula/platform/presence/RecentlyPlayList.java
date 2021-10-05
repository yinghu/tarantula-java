package com.tarantula.platform.presence;

import com.icodesoftware.util.FIFOBuffer;
import com.icodesoftware.util.RecoverableObject;

public class RecentlyPlayList extends RecoverableObject {

    private FIFOBuffer<String> playListIndex;

    public RecentlyPlayList(){

    }
}
