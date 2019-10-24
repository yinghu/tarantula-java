using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Message {
    public string label;
    public string instanceId;
    public string query;
    public JSONObject data;
    public Message(string label,JSONObject data){
        this.label = label;
        this.data = data;
    }
	public Message(string label,string instanceId,string query,JSONObject data){
        this.label = label;
        this.instanceId = instanceId;
        this.query = query;
        this.data = data;
    }
}
