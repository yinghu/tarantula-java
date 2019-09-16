using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Message {
    public string label;
    public JSONObject data;
	public Message(string label,JSONObject data){
        this.label = label;
        this.data = data;
    }
}
