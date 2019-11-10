using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class DemoSync : MonoBehaviour {

	public TARA_API api;
    public Balance balance;
    public Notification notification; 
    
    public Demo[] demoList;
    
    public string deviceId;
    
    public void OnMouseDown(){
        OnLobby();    
    }
    
	void Start () {
	   api.Reset(deviceId,(b)=>{
            if(b){
                notification.OnNotification();     
            }
            else{
                Debug.Log(deviceId);
            }
        });   	
	}
	
	void Update () {
		
	}
    public void OnLobby(){
        int i = 0;
        api.OnLobby("demo",(desp)=>{
            if(!desp.Category().Equals("lobby")){
                Debug.Log(desp.ApplicationId()+"/"+desp.Category());
                demoList[i++].OnLobby(desp);
            }    
        });
    }
}
