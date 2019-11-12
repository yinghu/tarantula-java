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
    
    private Presence presence;
    public event GecHandler OnException;
    
    void Start(){
        _ghc = new GecHttpClient(GEC_HOST);
    }

    void Update(){}
    
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
            Debug.Log(json);
            string jstr = await _ghc.PostJson(this,"/user/action",headers,json);
            Debug.Log(jstr);
            ParseLogin(jstr);
            return true;
        }catch(Exception ex){
            OnException?.Invoke(ex);
            return false;
        }
    }
    public  async Task<bool> Profile(string systemId){
        try{
            Header[] headers = new Header[]{
                new Header("Tarantula-tag","presence/profile"),
                new Header("Tarantula-token",presence.token),
                new Header("Tarantula-action","onProfile")
            };
            Payload p = new Payload();
            p.command = "onProfile";
            p.headers = new Header[]{new Header("systemId",presence.systemId),new Header("stub",presence.stub.ToString())};
            string json = JsonConvert.SerializeObject(p);
            Debug.Log(json);
            string jstr = await _ghc.PostJson(this,"/service/action",headers,json);
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
            Debug.Log(json);
            string jstr = await _ghc.PostJson(this,"/user/action",headers,json);
            Debug.Log(jstr);
            ParseLogin(jstr);
            return true;
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
            string jstr = await _ghc.PostJson(this,"/service/action",headers,"{}");
            Debug.Log(jstr);
            //ParseLogin(jstr);
            return true;
        }catch(Exception ex){
            OnException?.Invoke(ex);
            return false;
        }
    }
    private void ParseLogin(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                return;
            }
            JToken tk = jo.SelectToken("presence");
            presence = tk.ToObject<Presence>();
            Debug.Log(presence.systemId);
            Debug.Log(presence.token);
            Debug.Log(presence.login);
            /**
            for(int i=0;i<tk.Count;i++){
                Descriptor desc = tk[i].SelectToken("descriptor").ToObject<Descriptor>();
                Debug.Log("Desc->"+desc.typeId+"/"+desc.name+"/"+desc.tag);
                JArray ta = (JArray)tk[i].SelectToken("applications");
                if(ta.Count>0){
                    for(int j=0;j<ta.Count;j++){
                        Descriptor app = ta[j].ToObject<Descriptor>();
                        Debug.Log("App->"+app.typeId+"/"+app.name+"/"+desc.tag);
                    }
                }
            }**/   
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
