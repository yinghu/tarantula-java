
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Demo : MonoBehaviour {
    public TARA_API api;
    public Balance balance;
    public Logger logger;
    Descriptor desc;
    bool joined = false;
    JSONObject game;
    TextMesh tb;
    int fn;
    long lastSeq;
    void Start () {
	   tb = GetComponentInChildren<TextMesh>();	
       lastSeq = -1;
    }
	
	void Update () {
        fn++;
	}
    public void OnLobby(Descriptor descriptor){
        desc = descriptor; 
        tb.text = desc.Name();   
    }
    public void OnMouseDown(){
        if(desc!=null&&(!joined)){
            api.Play(desc,(g)=>{
                joined = true;
                game = g;   
                balance.OnBalance();
            },(m)=>{
                   Debug.Log(m.data.ToString());      
                   string lb = m.data.GetField("label").str;
                   if(lb.Equals("timer")){
                        //OnTimer(m.data);
                   }
                   else if(lb.Equals("update")){
                       Debug.Log(m.data.ToString());
                   }
                   else{
                        OnNotice(m.data);      
                   }
            });
        }
        else if(desc!=null&&joined){
            JSONObject jn = new JSONObject(JSONObject.Type.OBJECT);
            jn.AddField("command","onLeave");
            Instance ins = new Instance(desc.ApplicationId(),game.GetField("instanceId").str,"onLeave",jn);
            api.Request(ins,(m)=>{
                balance.OnBalance();
                tb.text = desc.Name();
                joined = false;
                api.StopUdp();
            });
        }
    }
    public void Sync(string cmd){
        if(joined){
            JSONObject jn = new JSONObject(JSONObject.Type.OBJECT);
            jn.AddField("command",cmd);
            Instance ins = new Instance(desc.ApplicationId(),game.GetField("instanceId").str,cmd,jn);
            fn=0;
            api.Send(ins);
            JSONObject jp = new JSONObject(JSONObject.Type.OBJECT);
            jp.AddField("x",10);
            jp.AddField("y",10);
            jp.AddField("z",10);
            jp.AddField("label","update");
            api.Forward(game.GetField("label").str,game.GetField("instanceId").str,jp);
        }
    }
    void OnTimer(JSONObject m){
        long seq = (long)m.GetField("sequence").n;
        if(seq>lastSeq){
            tb.text = m.GetField("hh").n+":"+m.GetField("mm").n+":"+m.GetField("ss").n+":"+m.GetField("ms").n;
            lastSeq = seq;
        } 
        else{
            Debug.Log("Missed->"+seq);
        }
    }
    void OnNotice(JSONObject m){
        string mg = "Label->"+m.GetField("label").str+",Size->"+m.ToString().Length+",Timed->"+(fn*(1000/60));
        logger.Log(mg);
        fn=0;
    }
}
