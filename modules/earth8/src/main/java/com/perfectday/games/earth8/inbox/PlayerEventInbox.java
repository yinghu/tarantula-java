package com.perfectday.games.earth8.inbox;

import com.icodesoftware.OnAccess;
import com.icodesoftware.protocol.OnInbox;

import java.util.List;

public class PlayerEventInbox implements OnInbox {

    private String name;
    private String category;

    private List<OnAccess> content;

    public PlayerEventInbox(String name,String category,List<OnAccess> content){
        this.name = name;
        this.category = category;
        this.content = content;
    }
    @Override
    public String name() {
        return name;
    }

    @Override
    public String category() {
        return category;
    }

    @Override
    public List<OnAccess> content() {
        return content;
    }
}
