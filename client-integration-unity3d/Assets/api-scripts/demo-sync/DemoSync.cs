using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GameEngineCluster.Model;
public class DemoSync : MonoBehaviour {

	public TARA_API api;
    public Balance balance;
    public Notification notification; 
    
    public Demo[] demoList;
    
    public string deviceId;
    public NetworkManager crt;
    public CRotate rtt;
    public async void OnMouseDown(){
        //OnLobby();   
        rtt._Update(true);
        Device dev = new Device();
        dev.deviceId = "abc12345";
        bool suc = await crt.Device(dev);
        Debug.Log("suc=>"+suc);
        await crt.ArenaList();
        rtt._Update(false);
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
