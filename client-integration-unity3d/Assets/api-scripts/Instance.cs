using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Instance  {
    
    public JSONObject data;
    public string applicationId;
    public string instanceId;
    public string path = "/application/instance";
    public string action;
    public bool streaming = false;
    public Instance(string appid,string insid,string action,JSONObject data){
        this.applicationId = appid;
        this.instanceId = insid;
        this.action = action;
        this.data = data;
    }
}
