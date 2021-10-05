package com.tarantula.platform.presence;

import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.util.FIFOBuffer;
import com.icodesoftware.util.RecoverableObject;

public class RecentlyPlayList extends RecoverableObject {

    private FIFOBuffer<String> playListIndex;

    public RecentlyPlayList(){

    }

    public interface Listener extends Configurable.Listener {
        void onPlay(String systemId, Descriptor lobby);
    }
}