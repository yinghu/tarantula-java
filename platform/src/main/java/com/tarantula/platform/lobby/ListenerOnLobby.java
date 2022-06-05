package com.tarantula.platform.lobby;

import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;

public class ListenerOnLobby {
    public final Descriptor lobby;
    public final Configurable.Listener<LobbyItem> listener;

    public ListenerOnLobby(Descriptor lobby,Configurable.Listener<LobbyItem> listener){
        this.lobby = lobby;
        this.listener = listener;
    }
}
