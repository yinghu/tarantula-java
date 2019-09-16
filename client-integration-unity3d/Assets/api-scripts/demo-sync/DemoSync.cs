using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class DemoSync : MonoBehaviour {

	public TARA_API api;
    
    public Demo[] demoList;
    
    
	void Start () {
	   	
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
