using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Application {
    public JSONObject data;
    public string tag;
    public string path = "/service/action";
    public string action;
    public bool streaming = false;
    public Application(string tag,string action,JSONObject data){
        this.tag = tag;
        this.action = action;
        this.data = data;
    }
	
}
