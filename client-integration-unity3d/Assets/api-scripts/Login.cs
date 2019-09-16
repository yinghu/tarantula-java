using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Login : MonoBehaviour {

    public TARA_API api;
    public Balance balance;
    public Notification notification;
    public DemoSync demoSync;
    public string login;
    public string password;
	
	void Start () {	
	}
	

	void Update () {
		
	}
    public void OnMouseDown(){
        api.Login(login,password,(b)=>{
            if(b){
                api.Presence((a)=>{
                    balance.OnBalance();
                    notification.OnNotification();
                    demoSync.OnLobby();
                });
            }
        });
    }
}
