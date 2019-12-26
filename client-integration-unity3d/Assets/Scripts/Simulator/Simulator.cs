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
    public View view;
    private TextMeshProUGUI timer;
    
    void Awake(){
        Integration.OnMessage += _OnMessage; 
    }
    
    void Start(){
        INS = Integration.Instance;
        GameObject gp = GameObject.Find("/UI/BackTimer"); 
        timer = gp.GetComponentInChildren<TextMeshProUGUI>();
        timer.SetText("00:00");
        GameObject ap = GameObject.Find("/UI/Arena"); 
        TextMeshProUGUI azt = ap.GetComponentInChildren<TextMeshProUGUI>();
        azt.SetText(INS.arena);
        Integration.OnException += (ex,msg,code)=>{
            Debug.Log(ex);
            Debug.Log(msg);
            Debug.Log(code);
            azt.SetText(msg);
            //INS.OnTicket(this);    
        };
        //setup robot
        view.OnBoard(INS.robotList);
    }
    void _OnMessage(InboundMessage msg){
        if(msg.instanceId!=null&&msg.instanceId.Equals(INS.game.gameId)){
            if(msg.query!=null&&msg.query.Equals("onMessage")){
                //Debug.Log(msg.payload);
                JObject jo = JObject.Parse(msg.payload);
                Payload pv = jo.ToObject<Payload>();
                view.OnMove(pv);
            }
            else if(msg.query!=null&&msg.query.Equals("onTimer")){
                JObject jo = JObject.Parse(msg.payload);
                int m = (int)jo.SelectToken("m");
                int s = (int)jo.SelectToken("s");
                timer.SetText(m+":"+s);
            }
            else if(msg.query!=null&&msg.query.Equals("onMove")){
                //Debug.Log(msg.payload);
                JObject jo = JObject.Parse(msg.payload);
                Vector3 pv = new Vector3();
                pv.x=(float)jo.SelectToken("x")*Screen.width;
                pv.y=(float)jo.SelectToken("y")*Screen.height;
                pv.z=0;
                float speed = (float)jo.SelectToken("f");
                string questId = (string)jo.SelectToken("n");
                view.OnMove(questId,pv,speed);
            }
            else if(msg.query!=null&&msg.query.Equals("onRemove")){
                //Debug.Log(msg.payload);
                JObject jo = JObject.Parse(msg.payload);
                string questId = (string)jo.SelectToken("n");
                view.OnRemove(questId);
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
            else{
                Debug.Log("Unknow query->"+msg.query);
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
    async void Update(){
        if (Input.GetMouseButtonDown(0)) {
             Vector3 target = Input.mousePosition;
             float x = (target.x/Screen.width);
             float y = (target.y/Screen.height);
             Payload payload = new Payload();
             payload.command = "onMessage";
             payload.headers = new Header[5];
             payload.headers[0]=new Header("x",x.ToString());
             payload.headers[1]=new Header("y",y.ToString());
             payload.headers[2]=new Header("z",target.z.ToString());
             payload.headers[3]=new Header("f","10");
             payload.headers[4]=new Header("n",(string)INS.robotList[INS.seatIndex].SelectToken("questId"));
             await INS.OnMove(payload);//publish move destination
             //Debug.Log("SEND ["+suc+"]");
        }   
    }
    public void OnQuest(string json){
        //Debug.Log(json);
        JObject jo = JObject.Parse(json);
        Vector3 mp = new Vector3();
        mp.x = ((float)jo.SelectToken("x"))*Screen.width;
        mp.y = ((float)jo.SelectToken("y"))*Screen.height;
        mp.z = 0;//float.Parse(pv.headers[2].value);
        string _name = (string)jo.SelectToken("n");
        int _ix = (int)jo.SelectToken("i");
        view.OnView(_name,mp,_ix);
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
    public  async void OnSeat1(){
        Payload payload = new Payload();
        payload.headers = new Header[]{new Header("accessId","a"),new Header("c","5")};
        await INS.OnQuest(payload);       
    }
    public  async void OnSeat2(){
        Payload payload = new Payload();
        payload.headers = new Header[]{new Header("accessId","b"),new Header("f","10")};
        await INS.OnQuest(payload);
    }
    public  async void OnSeat3(){
        Payload payload = new Payload();
        payload.headers = new Header[]{new Header("accessId","c"),new Header("f","2")};
        await INS.OnQuest(payload);
    }
    public async  void OnSeat4(){
        Payload payload = new Payload();
        payload.headers = new Header[]{new Header("accessId","d")};
        await INS.OnQuest(payload);
    }
}
