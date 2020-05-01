
using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using System.Net;
using System.Net.Sockets;
using System.Net.WebSockets;
using System.Text;
using System;
using System.Security.Cryptography.X509Certificates;
using UnityEngine;
using UnityEngine.Networking;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Tarantula.Networking{
   
   public delegate void ExceptionHandler(Exception ex,string message,ErrorCode errorCode);
   public delegate void WebSocketHandler();    
   public delegate void OnRoomEvent(RoomState state);  
    
   public enum ErrorCode{
       EC_INDEX,
       EC_DEDICATED,
       EC_DEVICE,
       EC_LOGOUT,
       EC_ONPLAY,
       EC_WS_RECEIVE,
       EC_INBOUND_MSG_PARSE,
       EC_CLOSE,
       EC_WEB_GET,
       EC_WEB_SET
   }
   public enum RoomState{
       WAITING= 0,
       PENDING_JOIN=1,
       INITIALIZING=2,
       STARTING=3,
       OVERTIME=4,
       ENDING=5
   }     
   [CreateAssetMenu(fileName = "GameEngineCluster", menuName = "Scripts/GameEngineCluster", order = 1)]    
   public class GameEngineCluster : ScriptableObject{
      
        public event ExceptionHandler OnException;
        public event WebSocketHandler OnWebSocket;
        
        public event OnRoomEvent OnUpdating;
       
       
        public string host;
        public bool dedicated;
        public string accessKey;
        
        private GecHttpClient _ghc;
        private GecWebSocket _gwc;
        
        private bool _liveWc;
       
        private Dictionary<string,Lobby> _lobbyList;
        private static JsonSerializerSettings JSON_SETTING = new JsonSerializerSettings{NullValueHandling = NullValueHandling.Ignore};
        
        public Presence presence {set;get;}
        public bool online {set;get; }
      
        public string message {set;get;}
        
        public Lobby lobby(string typeId){ return _lobbyList[typeId];}  
        
        public Stub stub{set;get;}
        public Room room{set;get;}
        public Timer timer{set;get;}
        
        private static GameEngineCluster _INSTANCE;
        private string deviceId;
        
        [RuntimeInitializeOnLoadMethod]
        private static void _Init(){
            _INSTANCE = Resources.Load<GameEngineCluster>("GameEngineCluster");
            _INSTANCE.Bootstrap();    
        }
        public static GameEngineCluster Instance{
            get{return _INSTANCE;}
        }
        void OnEnable(){
            //_HOST = GEC_HOST;
            deviceId = SystemInfo.deviceUniqueIdentifier;
            Debug.Log("GEC OPEN->"+host);
        }
        async void OnDestroy(){
            Debug.Log("GEC CLOSE->"+host);
            await _INSTANCE.OnClose();
        }
        private void Bootstrap(){
            _ghc = new GecHttpClient(host);  
            _lobbyList = new Dictionary<string,Lobby>();
            _liveWc = false;
            room = new Room();
            timer = new Timer();
            OnWebSocket += _OnWebSocketMessage;
            Debug.Log("Starting GameEngineCluster cluster on ["+host+"]");
        }  
        private async void _OnWebSocketMessage(){
            Debug.Log("Listen on WEB SOCKET");
            await OnWebSocketMessage();
        }
      
        public  async Task<bool> OnIndex(MonoBehaviour caller){
            try{
                string jstr = await _ghc.GetJson(caller,"/user/index",new Header[]{new Header("Tarantula-tag","index/lobby")});
                return ParseIndex(jstr);
            }catch(Exception ex){
                OnException?.Invoke(ex,ex.Message,ErrorCode.EC_INDEX);
                return false;
            }
        }
        public  async Task<bool> OnGameRegistered(MonoBehaviour caller,Connection conn){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-host",conn.host),
                    new Header("Tarantula-port",""+conn.port),
                    new Header("Tarantula-server-id",deviceId),
                    new Header("Tarantula-access-key",accessKey),
                    new Header("Tarantula-type-id",conn.type),
                    new Header("Tarantula-action","onRegistered")
                };
                string jstr = await _ghc.GetJson(caller,"/dedicated/action",headers);
                Debug.Log(jstr);
                return ParseHeader(jstr);            
            }catch(Exception ex){
                OnException?.Invoke(ex,ex.Message,ErrorCode.EC_DEDICATED);
                return false;
            }
        }
        public  async Task<bool> OnGameStarted(MonoBehaviour caller,Action<string> callback){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-server-id",deviceId),
                    new Header("Tarantula-access-key",accessKey),
                    new Header("Tarantula-action","onStarted")
                };
                //string json = JsonConvert.SerializeObject(conn,JSON_SETTING);
                string jstr = await _ghc.GetJson(caller,"/dedicated/action",headers);
                callback(jstr);
                return true;//           
            }catch(Exception ex){
                OnException?.Invoke(ex,ex.Message,ErrorCode.EC_DEDICATED);
                return false;
            }
        }
        public  async Task<bool> OnGameUpdated(MonoBehaviour caller,Action<string> callback){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-server-id",deviceId),
                    new Header("Tarantula-access-key",accessKey),
                    new Header("Tarantula-action","onUpdated")
                };
                string json = JsonConvert.SerializeObject(room,JSON_SETTING);
                string jstr = await _ghc.PostJson(caller,"/dedicated/action",headers,json);
                callback(jstr);
                return true;//           
            }catch(Exception ex){
                OnException?.Invoke(ex,ex.Message,ErrorCode.EC_DEDICATED);
                return false;
            }
        }
        public  async Task<bool> OnGameEnded(MonoBehaviour caller,Action<string> callback){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-server-id",deviceId),
                    new Header("Tarantula-access-key",accessKey),
                    new Header("Tarantula-action","onEnded")
                };
                string json = JsonConvert.SerializeObject(room,JSON_SETTING);
                string jstr = await _ghc.PostJson(caller,"/dedicated/action",headers,json);
                callback(jstr);
                return true;//           
            }catch(Exception ex){
                OnException?.Invoke(ex,ex.Message,ErrorCode.EC_DEDICATED);
                return false;
            }
        }
       public  async Task<bool> OnTicket(MonoBehaviour caller){
            try{
                if(_liveWc){
                    return false;
                }
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","presence/lobby"),
                    new Header("Tarantula-token",presence.token),
                    new Header("Tarantula-action","onTicket")
                };
                string jstr = await _ghc.GetJson(caller,"/service/action",headers);
                Debug.Log(jstr);
                return await ParseTicket(jstr);            
            }catch(Exception ex){
                OnException?.Invoke(ex,ex.Message,ErrorCode.EC_LOGOUT);
                return false;
            }
        }
       public  async Task<bool> Logout(MonoBehaviour caller){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","presence/lobby"),
                    new Header("Tarantula-token",presence.token),
                    new Header("Tarantula-action","onAbsence")
                };
                string jstr = await _ghc.GetJson(caller,"/service/action",headers);
                if(ParseLogout(jstr)){
                    await OnClose();
                    return true;
                }else{
                    return false;
                }           
            }catch(Exception ex){
                OnException?.Invoke(ex,ex.Message,ErrorCode.EC_LOGOUT);
                return false;
            }
        }
       
        public  async Task<bool> OnPlay(MonoBehaviour caller){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","game/mmk"),
                    new Header("Tarantula-token",presence.token),
                    new Header("Tarantula-action","onPlay")
                };
                string jstr = await _ghc.GetJson(caller,"/service/action",headers);
                return await ParseGameObject(jstr);
            }catch(Exception ex){
                OnException?.Invoke(ex,ex.Message,ErrorCode.EC_ONPLAY);
                return false;
            }
        }
        public  async Task<bool> OnLeave(MonoBehaviour caller){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag",stub.tag),
                    new Header("Tarantula-token",presence.token),
                    new Header("Tarantula-action","onLeave")
                };
                string jstr = await _ghc.GetJson(caller,"/service/action",headers);
                return ParseHeader(jstr);
            }catch(Exception ex){
                OnException?.Invoke(ex,ex.Message,ErrorCode.EC_ONPLAY);
                return false;
            }
        }
        public  async Task<bool> OnSet<T>(MonoBehaviour caller,string key,T jo){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","game/data"),
                    new Header("Tarantula-token",presence.token),
                    new Header("Tarantula-action","onSet"),
                    new Header("Tarantula-name",key),
                };
                string json = JsonConvert.SerializeObject(jo,JSON_SETTING);
                string jstr = await _ghc.PostJson(caller,"/service/action",headers,json);
                return ParseHeader(jstr);
            }catch(Exception ex){
                OnException?.Invoke(ex,ex.Message,ErrorCode.EC_WEB_SET);
                return false;
            }
        }
        public  async Task<bool> OnGet(MonoBehaviour caller,string key,Action<string> callback){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","game/data"),
                    new Header("Tarantula-token",presence.token),
                    new Header("Tarantula-action","onGet"),
                    new Header("Tarantula-name",key),
                };
                string jstr = await _ghc.GetJson(caller,"/service/action",headers);
                callback(jstr);
                return true;
            }catch(Exception ex){
                OnException?.Invoke(ex,ex.Message,ErrorCode.EC_WEB_GET);
                return false;
            }
        }
        public  async Task<bool> OnDevice(MonoBehaviour caller){
            try{
                Device device = new Device();
                device.deviceId = deviceId;
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","index/user"),
                    new Header("Tarantula-magic-key",deviceId),
                    new Header("Tarantula-action","onDevice")
                };
                string json = JsonConvert.SerializeObject(device,JSON_SETTING);
                string jstr = await _ghc.PostJson(caller,"/user/action",headers,json);
                return await ParseLogin(jstr);
            }catch(Exception ex){
                OnException?.Invoke(ex,ex.Message,ErrorCode.EC_DEVICE);
                return false;
            }
        }
        private async Task<bool> OnWebSocketMessage(){
            string msg ="{}";
            try{
                //do receive loop
                while(_liveWc){
                    msg = await _gwc.Receive();
                    ParseInboundMessage(msg);
                }
                return false;
            }catch(Exception ex){
                _liveWc = false;
                OnException?.Invoke(ex,msg,ErrorCode.EC_WS_RECEIVE);
                return _liveWc;
            }   
        }
        private async Task<bool> OnClose(){
            try{
                bool suc = false;
                if(_liveWc){
                    _liveWc = false;
                    suc = await _gwc.Close();
                }
                online = false;
                return suc;
            }catch(Exception ex){
                OnException?.Invoke(ex,ex.Message,ErrorCode.EC_CLOSE);
                online = false;
                return false;
            }
        }
        private void ParseInboundMessage(string msg){
            //format [label]#[instanceId]?[query]{json payload}
            try{
                InboundMessage im = new InboundMessage();
                int idx1 = msg.IndexOf('#');
                if(idx1>0){
                    im.label = msg.Substring(0,idx1);
                }
                int idx2 = msg.IndexOf('?');
                int idx3 = msg.IndexOf('{');
                if(idx2>0&idx3>0){
                    int idx = idx1>0?idx1+1:0;
                    im.instanceId = msg.Substring(idx,idx2-idx);
                    im.query = msg.Substring(idx2+1,idx3-idx2-1);
                }
                if(idx2<0&idx3>0){
                    int idx = idx1>0?idx1+1:0;
                    im.instanceId = msg.Substring(idx,idx3-idx);
                }
                im.payload = msg.Substring(idx3);
                if(im.query!=null&&im.query.Equals("onStart")){
                    Debug.Log("START=>>>"+im.payload);
                    JObject jo = JObject.Parse(im.payload);
                    room.arena = (string)jo.SelectToken("arena");
                    room.state = (RoomState)((int)jo.SelectToken("state"));
                    room.duration = (int)jo.SelectToken("duration");
                    room.overtime = (int)jo.SelectToken("overtime");
                    room.totalJoined = (int)jo.SelectToken("totalJoined");
                    if(jo.ContainsKey("connection")){
                        Connection conn = jo.SelectToken("connection").ToObject<Connection>();
                        room.connection = conn;
                    }
                    else{
                        Connection conn = new Connection();
                        conn.offline = true;
                        conn.host = "127.0.0.1";
                        conn.port = 15937;
                        room.connection = conn;
                    }
                    OnUpdating?.Invoke(room.state);
                }
                else if(im.query!=null&&im.query.Equals("onTimer")){
                    //Debug.Log("Timer=>>>"+im.payload);
                    JObject jo = JObject.Parse(im.payload);
                    timer.m = (int)jo.SelectToken("m");
                    timer.s = (int)jo.SelectToken("s");
                    room.state = (RoomState)((int)jo.SelectToken("state"));
                    room.totalJoined = (int)jo.SelectToken("totalJoined");
                    if(room.state==RoomState.INITIALIZING){
                        room.started=true;    
                    }
                    OnUpdating?.Invoke(room.state);
                }
                else if(im.query!=null&&im.query.Equals("onOvertime")){
                    Debug.Log("Overtime=>>>"+im.payload);
                    JObject jo = JObject.Parse(im.payload);
                    timer.m = (int)jo.SelectToken("m");
                    timer.s = (int)jo.SelectToken("s");
                    room.state = (RoomState)((int)jo.SelectToken("state"));
                    OnUpdating?.Invoke(room.state);
                }
                else if(im.query!=null&&im.query.Equals("onEnd")){
                    Debug.Log("END=>>>"+im.payload);
                    OnUpdating?.Invoke(RoomState.ENDING);
                }
            }catch(Exception ex){
                OnException?.Invoke(ex,msg,ErrorCode.EC_INBOUND_MSG_PARSE);    
            }   
        }
        private async Task<bool> ParseGameObject(string json){
            Debug.Log(json);
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                message = (string)jo.SelectToken("message");
                return suc;
            }
            stub = jo.SelectToken("stub").ToObject<Stub>();
            //streaming on websocket
            Streaming strm = new Streaming();
            strm.action = "onStream";
            strm.path = "/service/action";
            strm.streaming = true;
            strm.tag = stub.tag;
            strm.instanceId = stub.roomId;
            Payload p = new Payload();
            p.command = strm.action;
            strm.data = p;
            string jstrm = JsonConvert.SerializeObject(strm,JSON_SETTING);
            suc = await _gwc.Send(jstrm);
            return suc;
        }  
        private bool ParseIndex(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                message = (string)jo.SelectToken("message");
                return suc;
            }
            _lobbyList.Clear();
            JArray tk = (JArray)jo.SelectToken("lobbyList");
            for(int i=0;i<tk.Count;i++){
                Lobby lb = tk[i].ToObject<Lobby>();
                _lobbyList.Add(lb.descriptor.typeId,lb);
                Debug.Log(lb.descriptor.typeId);
            }
            return true;
        }
        private bool ParseHeader(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                message = (string)jo.SelectToken("message");
                return suc;
            }
            return true;
        }
        private async Task<bool> ParseTicket(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                message = (string)jo.SelectToken("message");
                return suc;
            }
            JToken tk = jo.SelectToken("presence");
            Presence _presence = tk.ToObject<Presence>();
            _presence.login = presence.login;
            if(jo.ContainsKey("connection")){//login sesseion websocket
                Connection connection = jo.SelectToken("connection").ToObject<Connection>();
                _gwc = new GecWebSocket(connection,_presence);
                suc = await _gwc.Connect();
                _liveWc = suc;
                OnWebSocket?.Invoke();
            }
            else{
                suc = false; //retry on caller again
            }
            return suc;
        }
        private async Task<bool> ParseLogin(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                message = (string)jo.SelectToken("message");
                return suc;
            }
            JToken tk = jo.SelectToken("presence");
            presence = tk.ToObject<Presence>();
            if(jo.ContainsKey("connection")){//login sesseion websocket
                Connection connection = jo.SelectToken("connection").ToObject<Connection>();
                _gwc = new GecWebSocket(connection,presence);
                suc = await _gwc.Connect();
                _liveWc = suc;
                OnWebSocket?.Invoke();
            }
            online = suc;
            return suc;
       }
       private bool ParseLogout(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                message = (string)jo.SelectToken("message");
                return suc;
            }
            return true;
        }  
   } 
   public class GecWebSocket{
        private ClientWebSocket _websocket;
        private string _url;
        private StringBuilder sb = new StringBuilder(4096);
        private byte[] data = new byte[1024];
        public GecWebSocket(Connection connection,Presence presence){
            _url = connection.protocol+"://"+connection.host+":"+connection.port+"/"+connection.path+"?accessKey="+presence.ticket+"&stub="+presence.stub+"&systemId="+presence.login;
            _websocket = new ClientWebSocket();
            _websocket.Options.AddSubProtocol(connection.subProtocol);
            _websocket.Options.SetRequestHeader("Origin",connection.host);
        }
        public async Task<bool> Connect(){
            await _websocket.ConnectAsync(new Uri(_url),CancellationToken.None);
            return _websocket.State == WebSocketState.Open;
        }
        public async Task<bool> Close(){
            await _websocket.CloseAsync(WebSocketCloseStatus.NormalClosure, string.Empty, CancellationToken.None);
            return _websocket.State == WebSocketState.Closed;
        }
        public async Task<bool> Send(string json){
            ArraySegment<Byte> om = new ArraySegment<Byte>(Encoding.UTF8.GetBytes(json.ToString()));     
            await _websocket.SendAsync(om, WebSocketMessageType.Text, true, CancellationToken.None); 
            return true;
        }
        public async Task<string> Receive(){
            sb.Clear();
            WebSocketReceiveResult wrs;
            do{
                ArraySegment<Byte> rbuff = new ArraySegment<Byte>(data);  
                wrs = await _websocket.ReceiveAsync(rbuff,CancellationToken.None);
                //check erro here 
                if(wrs.CloseStatus!=null){
                    sb.Append("error{'successful':false,'message':'"+wrs.CloseStatus+"'}");
                    break;
                }
                sb.Append(Encoding.UTF8.GetString(rbuff.Array,0,wrs.Count));
            }while(!wrs.EndOfMessage);    
            return sb.ToString();
        }
   } 
   public class GecHttpClient{
        
        
        private string GEC_HOST = "localhost:8090";
       
        public GecHttpClient(string host){
            GEC_HOST = host;      
        }
        
        public async Task<string> GetJson(MonoBehaviour caller,string path,Header[] headers){
            using(UnityWebRequest www = new UnityWebRequest(GEC_HOST+path,"GET")){
                www.downloadHandler = (DownloadHandler)new DownloadHandlerBuffer();
                www.certificateHandler = new KeyValidator(); 
                foreach(Header h in headers){
                    www.SetRequestHeader(h.name,h.value);    
                }
                www.SetRequestHeader("Accept","application/json");
                var tcs = new TaskCompletionSource<string>();
                caller.StartCoroutine(SendWebRequest(www,tcs));    
                return await tcs.Task;
            }
        }
        public async Task<string> PostJson(MonoBehaviour caller,string path,Header[] headers,string json){
            using(UnityWebRequest www = new UnityWebRequest(GEC_HOST+path,"POST")){
                byte[] payload = Encoding.UTF8.GetBytes(json.ToString());
                www.downloadHandler = (DownloadHandler)new DownloadHandlerBuffer();
                www.certificateHandler = new KeyValidator(); 
                www.uploadHandler = (UploadHandler)new UploadHandlerRaw(payload);
                foreach(Header h in headers){
                    www.SetRequestHeader(h.name,h.value);    
                }
                www.SetRequestHeader("Accept","application/json");
                www.SetRequestHeader("Content-type", "application/x-www-form-urlencoded");
                www.SetRequestHeader("Tarantula-payload-size",""+payload.Length);
                var tcs = new TaskCompletionSource<string>();
                caller.StartCoroutine(SendWebRequest(www,tcs));    
                return await tcs.Task;
            }
        }  
        private static IEnumerator SendWebRequest(UnityWebRequest request, TaskCompletionSource<string> tcs){
			yield return request.SendWebRequest();
			if(request.isNetworkError || request.isHttpError) {
                tcs.SetResult("{'successful':false,'message':'"+request.error+"'}"); 
            }
            else{
                tcs.SetResult(request.downloadHandler.text);        
            }
		}
    }
    public class KeyValidator : CertificateHandler{

        protected override bool ValidateCertificate(byte[] certificateData){
            //put key validation here
            /** uncomment this block to valid the certificate
            X509Certificate2 cert = new X509Certificate2(certificateData);
            **/
            return true;
        }
    }
    public class InboundMessage{
        public string label;
        public string instanceId;
        public string query;
        public string payload;
    }
    public class Header{
        public string name { get; set; }
        public string value { get; set; }
        public Header(string name,string value){
            this.name = name;
            this.value = value;
        }
    }
    public class Payload{
        public string command;
        public Header[] headers;
    }
    
    public class Streaming{
        public string path;
        public string action;
        public string label;
        public string applicationId;
        public string instanceId;
        public string tag;
        public bool streaming;
        public bool oneWay;
        public Payload data;
    }
    public class Descriptor{
        public bool singleton { get; set; }
        public string type { get; set; }
        public string typeId { get; set; }
        public string category { get; set; }
        public int capacity { get; set; }
        public string name { get; set; }
        public string description { get; set; }
        public string applicationId { get; set; }
        public string instanceId { get; set; }
        public int accessMode { get; set; }
        public double entryCost { get; set; }
        public bool tournamentEnabled { get; set; }
        public string tag { get; set; }
        public string responseLabel { get; set; }
    }
    public class Lobby{
        public Descriptor descriptor { get; set; }
        public List<Descriptor> applications { get; set; }
    }
    public class Device{
        public string deviceId { get; set; }
    }
    public class Connection{
        public bool offline{get;set;}
        public string path { get; set; }
        public string protocol { get; set; }
        public string subProtocol { get; set; }
        public string host { get; set; }
        public string type { get; set; }
        public string serverId { get; set; }
        public bool secured { get; set; }
        public int port { get; set; }
    }
    public class Room{
        public bool started{set;get;}
        public RoomState state{set;get;}
        public int totalJoined{set;get;}
        public Connection connection{get;set;}
        public string zone{get;set;}
        public string arena{get;set;}
        public int capacity{get;set;}
        public int duration{get;set;}
        public int overtime{get;set;}
        public Stub[] playerList{get;set;}
    }
    public class Timer{
        public int m{set;get;}
        public int s{set;get;}
    }
    public class Stub{
        public int rank{set;get;}
        public int seat{set;get;}
        public string owner{set;get;}
        public string roomId{set;get;}
        public string tag{set;get;}
    }
    public class Presence{
        public string systemId { get; set; }
        public int stub { get; set; }
        public string token { get; set; }
        public string ticket { get; set; }
        public string balance { get; set; }
        public string login { get; set; }
    }
}
