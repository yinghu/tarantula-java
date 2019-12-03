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
         gec.OnInboundMessage += (msg)=>{
             if(msg.label!=null){
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
        gec.Close();            
    }
   
    void Update(){
        if (Input.GetMouseButtonDown(0)){
            Vector3 mouse = Input.mousePosition;
            spin.OnMove(mouse);
        }
    }
    public async Task<bool> StartDemo(){
        if(!started){
            bool suc = await gec.Index(this);
            User device = new User();
            device.login = "root";
            device.nickname = "xnbbbv";
            device.password = "root";
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
                        Debug.Log(game.gameId);
                    });
                    if(suc){
                        spin.OnSpin(suc);
                        started = true;
                        //await gec.OnStreaming();  
                        
                    }
                    else{
                        Debug.Log("ops->"+gec.message);    
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
        bool suc = await gec.Profile(this,gec.presence.systemId);
        Debug.Log(gec.profile.nickname);
        await gec.Level(this);
        await gec.XP(this,"presence","LoginCount");
        await gec.LeaderBoard(this,"presence","LoginCount","T");
        User u = new User();
        u.login = "abc";
        u.nickname = "nvnv";
        OutboundMessage<User> om = new OutboundMessage<User>();
        om.instanceId = game.gameId;
        om.query = "onMessage";
        om.label = "game";
        om.payload = u;
        await gec.SendOnUDP(om);
    }
    public async void notification(){
        onNotification = !onNotification;
        bool suc = await gec.OnNotification("perfect-notification",onNotification);
        //Debug.Log(gec.profile.nickname);
        Payload cmd = new Payload();
        cmd.command = "abc";
        //cmd.headers = new Header[]{new Header()}
        await gec.SendOnService("robotquest-service/live",cmd);
    }
    public async void joo(){
        //onNotification = !onNotification;
        //bool suc = await gec.OnNotification("perfect-notification",onNotification);
        //Debug.Log(gec.profile.nickname);
        Payload cmd = new Payload();
        cmd.command = "abc";
        cmd.headers = new Header[]{new Header("a","1"),new Header("b","2")};
        await gec.SendOnInstance(game.applicationId,game.instanceId,cmd);
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
