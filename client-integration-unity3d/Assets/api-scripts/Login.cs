using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GameEngineCluster.Model;
public class Login : MonoBehaviour {

    public NetworkManager api;
    public Balance balance;
    public Notification notification;
    public DemoSync demoSync;
    public string deviceId;
    
	void Start () {	
	}
	

	void Update () {
		
	}
    public async void OnMouseDown(){
        Device device = new Device();
        device.deviceId="abc123";
        await api.Device(device);
    }
}
