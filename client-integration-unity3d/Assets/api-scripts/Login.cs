using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Login : MonoBehaviour {

    public TARA_API api;
    public Balance balance;
    public Notification notification;
    public DemoSync demoSync;
    public string deviceId;
    
	void Start () {	
	}
	

	void Update () {
		
	}
    public void OnMouseDown(){
        api.Reset(deviceId,(b)=>{
            if(b){
                api.Presence((a)=>{
                    balance.OnBalance();
                    notification.OnNotification();
                    demoSync.OnLobby();
                });
            }
            else{
                Debug.Log(deviceId);
            }
        });
    }
}
