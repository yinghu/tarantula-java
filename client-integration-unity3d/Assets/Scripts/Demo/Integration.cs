using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using UnityEngine;
using Tarantula.Networking;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

public class Integration : MonoBehaviour{
    
    public string GEC_HOST;
    public Spin spin;
    public GameEngineCluster gec;
    
    
    private static Integration instance;
    private bool onNotification;
    private bool started;
    private Descriptor game;

	public static Integration Instance{
		get
		{
			if (instance == null)
			{
				instance = FindObjectOfType<Integration>();
				if (instance == null)
				{
					GameObject obj = new GameObject
					{
						name = typeof(Integration).Name
					};
					instance = obj.AddComponent<Integration>();
				}
			}

			return instance;
		}
	}
    
    
    void Awake(){
         gec = new GameEngineCluster(GEC_HOST);
         gec.OnException += (ex)=>{
             Debug.Log(ex);
             spin.OnSpin(false);
             //gec.Close();
         };
         gec.OnWebSocket += _OnWebSocketMessage;
         gec.OnUDPSocket += _OnUDPSocketMessage;
         gec.OnMessage += (msg)=>{
             if(msg.label!=null&&msg.label.Equals("connection")){
                Debug.Log(msg.label);
                Debug.Log(msg.instanceId);
                Debug.Log(msg.query);
                Debug.Log(msg.payload);
             }
             else{
                 Debug.Log(msg.payload);
             }
         };
         DontDestroyOnLoad(this.gameObject);
    }
    async void _OnWebSocketMessage(bool suc){
        Debug.Log("web socket->"+suc);
        await gec.OnWebSocketMessage();
    }
    async void _OnUDPSocketMessage(bool suc){
        Debug.Log("udp socket->"+suc);
        await gec.OnUDPSocketMessage();
    }
    void Start(){
        onNotification = false;  
        started = false;
    }
    void OnDestroy(){
             
    }
   
    void Update()
    {
        
    }
    public async Task<bool> StartDemo(){
        if(!started){
            bool suc = await gec.Index(this);
            User device = new User();
            device.login = "xbbbbb";
            device.nickname = "xnbbbv";
            device.password = "abc123";
            suc = await gec.Login(this,device);
            if(!suc){
                Debug.Log(gec.message);
            }
            Debug.Log(suc);
            
            if(suc){
                suc = await gec.OnLobby(this,"robotquest");
                if(suc){
                    game = gec.gameList()[0];
                    suc = await gec.OnPlay(this,"robotquest-service/live",game,(gs)=>{
                        Debug.Log(game.instanceId);
                    });
                    if(suc){
                        spin.OnSpin(suc);
                        started = true;
                        //await gec.OnStreaming();  
                        
                    }
                }
                else{
                    Debug.Log("ops->"+gec.message);
                }
            }
            return suc;
        }
        else{
            started = false;
            spin.OnSpin(false);
            Payload p = new Payload();
            p.command = "onLeave";
            p.headers = new Header[]{new Header("test","miss")};
            await gec.OnInstance(this,game,p,(m)=>{Debug.Log(m);});
            await gec.Close();
            return false;
        }
    }
    
    public async void profile(){
        bool suc = await gec.Profile(this);
        Debug.Log(gec.profile.nickname);
    }
    public async void notification(){
        onNotification = !onNotification;
        bool suc = await gec.OnNotification("perfect-notification",onNotification);
        //Debug.Log(gec.profile.nickname);
    }
    private bool ParseGame(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                //message = (string)jo.SelectToken("message");
                return suc;
            }
            game.instanceId = (string)jo.SelectToken("instanceId");
            JToken tk = jo.SelectToken("gameObject");
            //for(int i=0;i<tk.Count;i++){
                //Descriptor gm = tk[i].ToObject<Descriptor>();
                //_gameList.Add(gm);
            //}
            return true;
    } 

}
