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
            device.deviceId = "abc123ggggggggggg";
            suc = await gec.Device(this,device);
            if(suc){
                suc = await gec.OnLobby(this,"robotquest");
                if(suc){
                    Descriptor g = gec.gameList()[0];
                    suc = await gec.OnPlay(this,g,(gs)=>{
                        
                        Debug.Log(gs);
                    });
                }
            }
            spin.OnSpin(suc);
            started = true;
            return suc;
        }
        else{
            started = false;
            spin.OnSpin(false);
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
            JToken tk = jo.SelectToken("gameObject");
            //for(int i=0;i<tk.Count;i++){
                //Descriptor gm = tk[i].ToObject<Descriptor>();
                //_gameList.Add(gm);
            //}
            return true;
    } 

}
