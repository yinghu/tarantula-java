using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Tarantula.Networking;
public class Spin : MonoBehaviour
{
    public float speed = 3.0f;
    public GameObject menu;
    bool _active;
    void Start()
    {
        _active = false;
        menu.SetActive(false);
    }

   
    void Update(){
        if(_active){
            transform.Rotate(10,speed,10);
        }    
    }
    public async void OnMouseDown(){
        _active = true;
        GameEngineCluster gec = Integration.Instance.gec;
        gec.OnException += (ex)=>{Debug.Log(ex);};
        Device device = new Device();
        device.deviceId = "abc123ggggggggggg";
        bool suc = await gec.Device(this,device);
        if(suc){
            Debug.Log(gec.presence.systemId);
            Debug.Log(gec.presence.ticket);
            Debug.Log(gec.presence.token);
            //GameObject ui = GameObject.Find("/UI/Menu");
            menu.SetActive(true);
            suc = await gec.OnWebSocket();
            //to do success
            //_active = false;
        }
        else{
            Debug.Log("opps=>"+gec.message);
        }
    }
}
