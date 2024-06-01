package com.tarantula.test;


import com.icodesoftware.DataStore;

import com.tarantula.platform.presence.PlayList;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;


public class PlayListTest extends DataStoreHook{


    @Test(groups = { "PlayList" })
    public void recentlyPlayListTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_play_list");
        long gameClusterId = serviceContext.distributionId();
        PlayList playList = new PlayList(10);
        playList.distributionId(gameClusterId);
        Assert.assertTrue(dataStore.createIfAbsent(playList,true));
        Assert.assertFalse(dataStore.createIfAbsent(playList,true));
        List<Long> list = playList.list();
        Assert.assertEquals(list.size(),10);
        list.forEach(id->{
            Assert.assertEquals(id,0);
        });
        for(int i =0;i<100;i++){
            playList.onList(serviceContext.distributionId());
            dataStore.update(playList);
        }
        dataStore.load(playList);
        list = playList.list();
        Assert.assertEquals(list.size(),10);
        list.forEach(id->{
            Assert.assertTrue(id>0);
        });
        for(int i=0;i<10;i++){
            PlayList friendList = new PlayList(10);
            friendList.distributionId(serviceContext.distributionId());
            Assert.assertTrue(dataStore.createIfAbsent(friendList,true));
            Assert.assertFalse(dataStore.createIfAbsent(friendList,true));
        }
        int[] ct ={0};
        dataStore.backup().forEach((k,v)->{
            ct[0]++;
            return true;
        });
        Assert.assertEquals(ct[0],11);
    }

}
