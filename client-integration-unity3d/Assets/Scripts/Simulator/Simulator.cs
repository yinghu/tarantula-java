using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using Tarantula.Networking;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
public class Simulator : MonoBehaviour
{
    private Integration INS;
    public Spin spin;
    void Start()
    {
        INS = Integration.Instance;
        Integration.OnMessage += _OnMessage;
        //Camera.main.transform.Rotate(0,0,180);
    }
    void _OnMessage(InboundMessage msg){
        if(msg.instanceId!=null&&msg.instanceId.Equals(INS.game.gameId)){
            if(msg.query!=null&&msg.query.Equals("onMessage")){
                JObject jo = JObject.Parse(msg.payload);
                Payload pv = jo.ToObject<Payload>();
                Vector3 mp = new Vector3();
                mp.x = float.Parse(pv.headers[0].value)*Screen.width;
                mp.y = float.Parse(pv.headers[1].value)*Screen.height;
                mp.z = float.Parse(pv.headers[2].value);
                spin.OnMove(mp);
            }    
        }
    }
    void OnDestroy(){
        Integration.OnMessage -= _OnMessage;
        Debug.Log("removed message handler");
    }
    // Update is called once per frame
    async void Update()
    {
        if (Input.GetMouseButtonDown(0)) {
             Vector3 target = Input.mousePosition;
             float x = (target.x/Screen.width);
             float y = (target.y/Screen.height);
             //Debug.Log(target);
             Payload payload = new Payload();
             payload.command = "onMessage";
             payload.headers = new Header[]{new Header("x",x.ToString()),new Header("y",y.ToString()),new Header("z",target.z.ToString())};
             await INS.OnMove(payload);//publish move destination
             
             //GameObject go = GameObject.Find("/View/Spin1");
             //if(go!=null){
                //go.name = "popo";
                //Spin spin = go.GetComponent<Spin>();
                //spin.OnSpin(false);
                //Debug.Log(go.name);
             //}
             //spin.OnMove(target);
        }   
    }
    
    public async void Back(){
        bool suc = await INS.OnLeave(this); 
        if(suc){
            SceneManager.LoadScene("Integration");
        }     
    }
    public void OnSeat1(){
        Camera.main.transform.Rotate(0,0,180,Space.Self);
    }
    public void OnSeat2(){
        Camera.main.transform.Rotate(0,0,180,Space.World);
    }
     public void OnSeat3(){
        Camera.main.transform.Rotate(0,0,180);
    }
}
