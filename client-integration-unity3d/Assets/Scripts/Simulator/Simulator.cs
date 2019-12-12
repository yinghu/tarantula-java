using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using Tarantula.Networking;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using TMPro;
public class Simulator : MonoBehaviour
{
    private Integration INS;
    public Spin spin;
    private TextMeshProUGUI timer;
    
    void Awake(){
        Integration.OnMessage += _OnMessage; 
    }
    
    void Start(){
        INS = Integration.Instance;
        GameObject gp = GameObject.Find("/UI/BackTimer"); 
        timer = gp.GetComponentInChildren<TextMeshProUGUI>();
        timer.SetText("00:00");
        //if(INS.seatIndex==1){
            //Camera.main.transform.Rotate(0,0,180);
        //}
        GameObject ap = GameObject.Find("/UI/Arena"); 
        TextMeshProUGUI azt = ap.GetComponentInChildren<TextMeshProUGUI>();
        azt.SetText(INS.arena);
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
                spin.OnMove(mp,float.Parse(pv.headers[3].value));
            }
            else if(msg.query!=null&&msg.query.Equals("onTimer")){
                JObject jo = JObject.Parse(msg.payload);
                int m = (int)jo.SelectToken("m");
                int s = (int)jo.SelectToken("s");
                timer.SetText(m+":"+s);
            }
            else if(msg.query!=null&&msg.query.Equals("onEnd")){
                SceneManager.LoadScene("Integration");
            }
            else if(msg.query!=null&&msg.query.Equals("onQuest")){
                OnQuest(msg.payload);    
            }
            else if(msg.query!=null&&msg.query.Equals("offQuest")){
                OffQuest(msg.payload);    
            }
        }
        else{
            Debug.Log("REV>"+msg.payload);
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
             payload.headers = new Header[]{new Header("x",x.ToString()),new Header("y",y.ToString()),new Header("z",target.z.ToString()),new Header("f","1.5")};
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
    public void OnQuest(string json){
        GameObject _view = GameObject.Find("/View");
        View view = _view.GetComponent<View>();
        Debug.Log(json);
        JObject jo = JObject.Parse(json);
        Vector3 mp = new Vector3();
        mp.x = ((float)jo.SelectToken("x"))*Screen.width;
        mp.y = ((float)jo.SelectToken("y"))*Screen.height;
        mp.z = 0;//float.Parse(pv.headers[2].value);
        string _name = (string)jo.SelectToken("n");
        view.OnView(_name,mp);
    }
    public void OffQuest(string json){
        Debug.Log(json);
    }
    public async void Back(){
        bool suc = await INS.OnLeave(this); 
        if(suc){
            SceneManager.LoadScene("Integration");
        }     
    }
    public void OnSeat1(){
        Payload payload = new Payload();
        payload.headers = new Header[]{new Header("x","abc")};
        INS.OnQuest(payload);     
        //Camera.main.transform.Rotate(0,0,180,Space.Self);
    }
    public void OnSeat2(){
        //Camera.main.transform.Rotate(0,0,180,Space.World);
    }
    public void OnSeat3(){
        //Camera.main.transform.Rotate(0,0,180);
    }
}
