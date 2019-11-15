using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Notification : MonoBehaviour {
    
    public NetworkManager api;
    
	private TextMesh tb;
   
	void Start () {
		tb = GetComponentInChildren<TextMesh>();
    }
	

	void Update () {   
	}
    public void OnNotification(){
        //api.OnNotification("perfect-notification",(jm)=>{
            //tb.text = jm.ToString();
        //});
    }
}
