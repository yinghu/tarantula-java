using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Lobby{

    private JSONObject _data;
    public Lobby(JSONObject data){
        this._data = data;
    }
    public Descriptor Descriptor(){
        return new Descriptor(_data.GetField("descriptor"));
    }
    
	public void OnLobby(Action<Descriptor> callback){
        JSONObject jo = _data.GetField("applications");
        for(int i=0;i<jo.list.Count;i++){
            callback(new Descriptor(jo.list[i]));
        }
    }
}
