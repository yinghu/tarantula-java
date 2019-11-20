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
         DontDestroyOnLoad(this.gameObject);
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
            Device device = new Device();
            device.deviceId = "highttt";
            suc = await gec.Device(this,device);
            if(suc){
                suc = await gec.OnLobby(this,"robotquest");
                if(suc){
                    game = gec.gameList()[0];
                    suc = await gec.OnPlay(this,game,(gs)=>{
                        Debug.Log(gs);
                        ParseGame(gs);
                    });
                    if(suc){
                        suc = await gec.OnStreaming(game);
                    }else{
                        Debug.Log("ops->"+gec.message);
                    }
                }
                else{
                    Debug.Log("ops->"+gec.message);
                }
            }
            spin.OnSpin(suc);
            started = true;
            await gec.OnWebSocket((msg)=>{
                Debug.Log(msg);
            });
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
