using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GameEngineCluster.Model;
public class Register : MonoBehaviour {

    public NetworkManager api;
    
	void Start () {
		
	}
	
	void Update () {
		
	}
    public async void OnMouseDown(){
        User user = new User();
        await api.Register(user);
    }
}
