package com.tarantula.platform.playlist;


import com.tarantula.platform.ResponseHeader;

import java.util.Set;

/**
 * Updated by yinghu lu on 10/9/2018.
 */
public class PlayListContext extends ResponseHeader {
    public RecentPlayList recentPlayList;
    public BuddyList myPlayList;
    public Set<String> availableList;
}
