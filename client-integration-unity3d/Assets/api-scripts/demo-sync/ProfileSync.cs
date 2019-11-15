using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ProfileSync : MonoBehaviour {

    public NetworkManager api;
    public Logger logger;
	public string protocol;
    private int fn;
    void Start () {
	}
	
	// Update is called once per frame
	void Update () {
		fn++;
	}
    /**
    public void OnMouseDown(){
        if(protocol.Equals("ws")){//web socket request
            api.AddMessageListener("profile",(m)=>{
                string ms = "Profile over web scoket size->"+m.data.ToString().Length+", Timed->"+(fn*(1000/60));
                logger.Log(ms);
                api.RemoveMessageListener("profile");
            });               
            
            //request profile data
            JSONObject jp = new JSONObject(JSONObject.Type.OBJECT);
            jp.AddField("command","onProfile");
            JSONObject arr = new JSONObject(JSONObject.Type.ARRAY);
            JSONObject hd = new JSONObject(JSONObject.Type.OBJECT);
            hd.AddField("name","systemId");
            hd.AddField("value",api.SystemId());
            arr.Add(hd);
            jp.AddField("headers",arr);
            Application app = new Application("presence/profile","onProfile",jp);
            fn=0;
            api.Send(app);
        }else if(protocol.Equals("http")){// http request
            JSONObject jp = new JSONObject(JSONObject.Type.OBJECT);
            jp.AddField("command","onProfile");
            JSONObject arr = new JSONObject(JSONObject.Type.ARRAY);
            JSONObject hd = new JSONObject(JSONObject.Type.OBJECT);
            hd.AddField("name","systemId");
            hd.AddField("value",api.SystemId());
            arr.Add(hd);
            jp.AddField("headers",arr);
            Application app = new Application("presence/profile","onProfile",jp);
            fn=0;
            api.Request(app,(m)=>{
                string ms = "Profile Over Http size->"+m.ToString().Length+", Timed->"+(fn*(1000/60));
                logger.Log(ms);        
            });
        }     
    }**/
}
