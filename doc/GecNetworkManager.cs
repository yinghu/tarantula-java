using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Threading.Tasks;
using System;

using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

using PerfectDay.GameEngineCluster;

public class GecNetworkManager : MonoBehaviour{
    
    public string GEC_HOST;
    private GecHttpClient _ghc;
    private Presence presence;
    private Connection connection;
    public string message { get; set; }
    
    private static GecNetworkManager instance;

	public static GecNetworkManager Instance{
		get
		{
			if (instance == null)
			{
				instance = FindObjectOfType<GecNetworkManager>();
				if (instance == null)
				{
					var obj = new GameObject
					{
						name = typeof(GecNetworkManager).Name
					};
					instance = obj.AddComponent<GecNetworkManager>();
				}
			}

			return instance;
		}
	}

    
    
    void Awake(){
         _ghc = new GecHttpClient(GEC_HOST);
         DontDestroyOnLoad(this.gameObject);
    }
    void Start(){
           
    }

   
    void Update()
    {
        
    }
    
    public  async Task<bool> Index(){
        try{
            string jstr = await _ghc.GetJson(this,"/user/index",new Header[]{new Header("Tarantula-tag","index/lobby")});
            return ParseLobbyList(jstr);
        }catch(Exception ex){
            Debug.Log(ex);
            this.message = "unexpected error";
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
            ParseLogin(jstr);
            return true;
        }catch(Exception ex){
            Debug.Log(ex);
            this.message = "unexpected error";
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
            Debug.Log(ex);
            this.message = "unexpected error";
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
            string jstr = await _ghc.PostJson(this,"/user/action",headers,json);
            return ParseRegistration(jstr);
        }catch(Exception ex){
            Debug.Log(ex);
            this.message = "unexpected error";
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
            string jstr = await _ghc.PostJson(this,"/service/action",headers,"{}");
            return ParseArenaList(jstr);
        }catch(Exception ex){
            Debug.Log(ex);
            this.message = "unexpected error";
            return false;
        }
    }
    private bool ParseRegistration(string json){
        JObject jo = JObject.Parse(json);
        bool suc = (bool)jo.SelectToken("successful");
        message = (string)jo.SelectToken("message");
        return suc;
    }
    private bool ParseLogin(string json){
        //Debug.Log(json);
        JObject jo = JObject.Parse(json);
        bool suc = (bool)jo.SelectToken("successful");
        if(!suc){
            message = (string)jo.SelectToken("message");
            return suc;
        }
        JToken tk = jo.SelectToken("presence");
        presence = tk.ToObject<Presence>();
        tk = jo.SelectToken("connection");
        connection = tk.ToObject<Connection>();
        return true;   
    }
    private bool ParseArenaList(string json){
        Debug.Log(json);
        JObject jo = JObject.Parse(json);
        bool suc = (bool)jo.SelectToken("successful");
        if(!suc){
            message = (string)jo.SelectToken("message");
            return suc;
        }
        JToken tk = jo.SelectToken("gameObject.arenaZone");
        ArenaZone az = tk.ToObject<ArenaZone>();
        Debug.Log(az.name);
        Debug.Log(az.list[0].name);
        return true;
    }
    private bool ParseLobbyList(string json){
        //Debug.Log(json);
        JObject jo = JObject.Parse(json);
        bool suc = (bool)jo.SelectToken("successful");
        if(!suc){
            message = (string)jo.SelectToken("message");
            return suc;
        }
        JArray tk = (JArray)jo.SelectToken("lobbyList");
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
        }
        return true;
    }
}
