using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Descriptor {
    
    private JSONObject _data;
   
    public Descriptor(JSONObject data){
        this._data = data;
    }
    public string TypeId(){
        return _data.GetField("typeId").str;
    }
    public string Name(){
        return _data.GetField("name").str;
    }
    public string ApplicationId(){
        return _data.GetField("applicationId").str;
    }
    //public string SubtypeId(){
        //return _data.GetField("subtypeId").str;
    //}
    public string Category(){
        return _data.GetField("category").str;
    }
    public string Tag(){
        return _data.GetField("tag").str;
    }
    public string ResponseLabel(){
        return _data.GetField("responseLabel").str;
    }
    public bool Singleton(){
        return _data.GetField("singleton").b;
    }
}
