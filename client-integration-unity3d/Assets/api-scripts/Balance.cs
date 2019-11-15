using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Balance : MonoBehaviour {

    public NetworkManager api;
	
	void Start () {
		GetComponentInChildren<TextMesh>().text="-------";
	}
	
	void Update () {
		
	}
    public void OnBalance(){
        ///api.Balance((resp)=>{
            //JSONObject jmx = new JSONObject(resp);
            //Debug.Log(resp);
            //GetComponentInChildren<TextMesh>().text = resp.GetField("presence").GetField("balance").str;      
        //});
    }
    public void ClearBalance(){
        GetComponentInChildren<TextMesh>().text="-------";
    }
}
