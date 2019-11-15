using System;
using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

using UnityEngine;

using GameEngineCluster;
using GameEngineCluster.Model;

public class NetworkManager : MonoBehaviour{
    
    public string GEC_HOST;
    
    private GecHttpClient _ghc;
    private GecWebSocket _gsc;
    
    private Connection connection;
    private Presence presence;
    
    public event GecHandler OnException;
    
    void Awake(){
         _ghc = new GecHttpClient(GEC_HOST);
         DontDestroyOnLoad(this.gameObject);
    }
    
    void Start(){
        
    }

    void Update(){
          
    }
    
    public  async Task<bool> Index(){
        try{
            string jstr = await _ghc.GetJson(this,"/user/index",new Header[]{new Header("Tarantula-tag","index/lobby")});
            ParseLobbyList(jstr);
            return true;
        }catch(Exception ex){
            OnException?.Invoke(ex);
            return false;
        }
    }
    public  async Task<bool> Register(User user){
        try{
            Header[] headers = new Header[]{
                new Header("Tarantula-tag","index/user"),
                new Header("Tarantula-magic-key",user.login),
                new Header("Tarantula-action","onRegister")
            };
            string json = JsonConvert.SerializeObject(user);
            Debug.Log(json);
            string jstr = await _ghc.PostJson(this,"/user/action",headers,json);
            Debug.Log(jstr);
            //ParseJson(jstr);
            return true;
        }catch(Exception ex){
            OnException?.Invoke(ex);
            return false;
        }
    }
    public  async Task<bool> Login(User user){
        try{
            Header[] headers = new Header[]{
                new Header("Tarantula-tag","index/user"),
                new Header("Tarantula-magic-key",user.login),
                new Header("Tarantula-action","onLogin")
            };
            string json = JsonConvert.SerializeObject(user);
            string jstr = await _ghc.PostJson(this,"/user/action",headers,json);
            return ParseLogin(jstr);            
        }catch(Exception ex){
            OnException?.Invoke(ex);
            return false;
        }
    }
    public  async Task<bool> Profile(){
        try{
            Header[] headers = new Header[]{
                new Header("Tarantula-tag","presence/profile"),
                new Header("Tarantula-token",presence.token),
            };
            string jstr = await _ghc.GetJson(this,"/service/action",headers);
            Debug.Log(jstr);
            //ParseProfile(jstr);
            return true;
        }catch(Exception ex){
            OnException?.Invoke(ex);
            return false;
        }
    }
    public  async Task<bool> Device(Device device){
        try{
            Header[] headers = new Header[]{
                new Header("Tarantula-tag","index/user"),
                new Header("Tarantula-magic-key",device.deviceId),
                new Header("Tarantula-action","onReset")
            };
            string json = JsonConvert.SerializeObject(device);
            string jstr = await _ghc.PostJson(this,"/user/action",headers,json);
            return ParseLogin(jstr);
        }catch(Exception ex){
            OnException?.Invoke(ex);
            return false;
        }
    }
    public  async Task<bool> ArenaList(){
        try{
            Header[] headers = new Header[]{
                new Header("Tarantula-tag","robotquest-service/live"),
                new Header("Tarantula-token",presence.token),
                new Header("Tarantula-action","onList")
            };
            //string json = JsonConvert.SerializeObject(device);
            //Debug.Log(json);
            string jstr = await _ghc.GetJson(this,"/service/action",headers);
            Debug.Log(jstr);
            //ParseLogin(jstr);
            return true;
        }catch(Exception ex){
            OnException?.Invoke(ex);
            return false;
        }
    }
    public async Task<bool> OnNotification(string label){
        try{
            //AddMessageListener(label,callback);
            Streaming ms = new Streaming();
            ms.action="onStart";
            ms.streaming=true;
            ms.label=label;
            Payload dt = new Payload();
            dt.command="onStart";
            ms.data=dt;
            string json = JsonConvert.SerializeObject(ms);
            Debug.Log(json);
            return await _gsc.Send(json);
        }catch(Exception ex){
            Debug.Log(ex);
            return false;
        }
    }
    public async Task<bool> Send(Header[] headers,string json){
        //JSONObject ms = new JSONObject(JSONObject.Type.OBJECT);
        //ms.AddField("action",target.action);
        //ms.AddField("streaming",target.streaming);
        //ms.AddField("path",target.path);
        //ms.AddField("tag",target.tag);
        //ms.AddField("data",target.data); 
        return await _gsc.Send("");
    }
    public async Task<bool> OnWebSocket(){
        try{
            _gsc = new GecWebSocket(connection,presence,"tarantula-service");     
            bool connected = await _gsc.Connect();
            if(connected){
                Streaming ms = new Streaming();
                ms.action="onStart";
                ms.streaming=true;
                ms.label="perfect-notification";
                Payload dt = new Payload();
                dt.command="onStart";
                ms.data=dt;
                string json = JsonConvert.SerializeObject(ms,new JsonSerializerSettings { NullValueHandling = NullValueHandling.Ignore });
                Debug.Log(json);
                await _gsc.Send(json);
                for(;;){
                    string rev = await _gsc.Receive();
                    Debug.Log(rev);
                }
            }
            
        }catch(Exception ex){
            OnException?.Invoke(ex);
        }
        return true;
    }
    private bool ParseLogin(string json){
        JObject jo = JObject.Parse(json);
        bool suc = (bool)jo.SelectToken("successful");
        if(!suc){
            return suc;
        }
        JToken tk = jo.SelectToken("presence");
        presence = tk.ToObject<Presence>();
        //Debug.Log(presence.systemId);
        //Debug.Log(presence.token);
        //Debug.Log(presence.login);
        connection = jo.SelectToken("connection").ToObject<Connection>();
        return true;
    }
    private void ParseLobbyList(string json){
        JObject jo = JObject.Parse(json);
        bool suc = (bool)jo.SelectToken("successful");
        if(!suc){
            return;
        }
        JArray tk = (JArray)jo.SelectToken("lobbyList");
        for(int i=0;i<tk.Count;i++){
            Descriptor1 desc = tk[i].SelectToken("descriptor").ToObject<Descriptor1>();
            Debug.Log("Desc->"+desc.typeId+"/"+desc.name+"/"+desc.tag);
            JArray ta = (JArray)tk[i].SelectToken("applications");
            if(ta.Count>0){
                for(int j=0;j<ta.Count;j++){
                    Descriptor1 app = ta[j].ToObject<Descriptor1>();
                    Debug.Log("App->"+app.typeId+"/"+app.name+"/"+desc.tag);
                }
            }
        }    
    }
}
