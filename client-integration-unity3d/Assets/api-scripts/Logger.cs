using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Logger : MonoBehaviour {

	private TextMesh tb;
	void Start () {
		tb = GetComponentInChildren<TextMesh>();
	}
	
	// Update is called once per frame
	void Update () {
		
	}
    
    public void Log(Message message){
        tb.text = message.data.GetField("message").str;
    }
    public void Log(string message){
        tb.text = message;
    }
    public void Error(string message){
        Debug.Log(message);
    }
}
