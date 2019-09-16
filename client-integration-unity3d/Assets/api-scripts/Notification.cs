using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Notification : MonoBehaviour {
    
    public TARA_API api;
    
	private TextMesh tb;
   
	void Start () {
		tb = GetComponentInChildren<TextMesh>();
    }
	

	void Update () {   
	}
    public void OnNotification(){
        api.OnNotification("presence/notice",(jm)=>{
            tb.text = jm.ToString();
        });
    }
}
