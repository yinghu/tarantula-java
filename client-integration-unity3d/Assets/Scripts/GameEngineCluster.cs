using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using System.Net;
using System.Net.WebSockets;
using System.Text;
using System;
using System.Security.Cryptography.X509Certificates;
using UnityEngine;
using UnityEngine.Networking;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;


namespace Tarantula.Networking{
   
   public delegate void ExceptionHandler(Exception ex);
    
   public class GameEngineCluster{
      
        public event ExceptionHandler OnException;

        private GecHttpClient _ghc;
        private GecWebSocket _gwc;
        private GecUdpSocket _guc;
        private bool _live;
        private Dictionary<string,Lobby> _lobbyList;
        private List<Descriptor> _gameList;
        
        public Presence presence {set;get;}
        public Profile profile {set;get;}
        public string message {set;get;}
        
        public Lobby lobby(string typeId){ return _lobbyList[typeId];}  
        public List<Descriptor> gameList(){ return _gameList;}
        
        public  GameEngineCluster(string host){
            _ghc = new GecHttpClient(host);  
            _lobbyList = new Dictionary<string,Lobby>();
            _gameList = new List<Descriptor>();
            _live = false;
        }  
      
        public  async Task<bool> Index(MonoBehaviour caller){
            try{
                string jstr = await _ghc.GetJson(caller,"/user/index",new Header[]{new Header("Tarantula-tag","index/lobby")});
                Debug.Log(jstr);
                return ParseIndex(jstr);
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        public  async Task<bool> Login(MonoBehaviour caller,User user){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","index/user"),
                    new Header("Tarantula-magic-key",user.login),
                    new Header("Tarantula-action","onLogin")
                };
                string json = JsonConvert.SerializeObject(user);
                string jstr = await _ghc.PostJson(caller,"/user/action",headers,json);
                bool suc = ParseLogin(jstr);
                if(suc){
                    suc = await _gwc.Connect();
                    _live = suc;
                }
                return suc;            
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        public  async Task<bool> Profile(MonoBehaviour caller){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","presence/profile"),
                    new Header("Tarantula-token",presence.token),
                };
                string jstr = await _ghc.GetJson(caller,"/service/action",headers);
                return ParseProfile(jstr);
                //profile get over websocket 
                //Streaming strm = new Streaming();
                //strm.path = "/service/action";
                //strm.tag = "presence/profile";
                //strm.streaming = false;
                //string json = JsonConvert.SerializeObject(strm,new JsonSerializerSettings{NullValueHandling = NullValueHandling.Ignore});
                //Debug.Log(json);
                //await _gwc.Send(json);
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        public  async Task<bool> Device(MonoBehaviour caller,Device device){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","index/user"),
                    new Header("Tarantula-magic-key",device.deviceId),
                    new Header("Tarantula-action","onReset")
                };
                string json = JsonConvert.SerializeObject(device);
                string jstr = await _ghc.PostJson(caller,"/user/action",headers,json);
                bool suc = ParseLogin(jstr);
                if(suc){
                    suc = await _gwc.Connect();
                    _live = suc;
                }
                return suc;
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        public async Task<bool> OnNotification(string label,bool streaming){
            try{
                Streaming strm = new Streaming();
                strm.action = streaming?"onStart":"onStop";
                strm.label = label;
                strm.streaming = true;
                Payload p = new Payload();
                p.command = strm.action;
                strm.data = p;
                string json = JsonConvert.SerializeObject(strm,new JsonSerializerSettings{NullValueHandling = NullValueHandling.Ignore});
                Debug.Log(json);
                return await _gwc.Send(json);
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        public async Task<bool> OnStreaming(Descriptor instance){
            try{
                Streaming strm = new Streaming();
                strm.action = "onStream";
                strm.path = "/application/instance";
                strm.streaming = true;
                strm.applicationId = instance.applicationId;
                strm.instanceId = instance.instanceId;
                Payload p = new Payload();
                p.command = strm.action;
                strm.data = p;
                string json = JsonConvert.SerializeObject(strm,new JsonSerializerSettings{NullValueHandling = NullValueHandling.Ignore});
                Debug.Log(json);
                return await _gwc.Send(json);
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        public async Task<bool> OnLobby(MonoBehaviour caller,string typeId){
            try{
                if(!_lobbyList.ContainsKey(typeId)){
                    return false;
                }
                Lobby lb = _lobbyList[typeId];
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag",lb.descriptor.tag),
                    new Header("Tarantula-token",presence.token),
                    new Header("Tarantula-action","onLobby")
                };
                Payload p = new Payload();
                p.command = "onLobby";
                p.headers = new Header[]{new Header("typeId",typeId)};
                string json = JsonConvert.SerializeObject(p);
                string jstr = await _ghc.PostJson(caller,"/service/action",headers,json);
                return ParseLobby(jstr);
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }   
        }
        public async Task<bool> OnPlay(MonoBehaviour caller,string joinTag,Descriptor game,Action<string> callback){
            try{
                Header[] headers = new Header[]{
                    //new Header("Tarantula-tag","robotquest-service/live"),
                    new Header("Tarantula-tag",joinTag),
                    new Header("Tarantula-token",presence.token),
                    new Header("Tarantula-action","onPlay")
                };
                Payload p = new Payload();
                p.command = "onPlay";
                p.headers = new Header[]{new Header("applicationId",game.applicationId),new Header("accessMode","2")};
                string json = JsonConvert.SerializeObject(p);
                string jstr = await _ghc.PostJson(caller,"/service/action",headers,json);
                //check if UDP connection is available 
                bool suc = ParseGameObject(jstr);
                callback(jstr);
                return suc;
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
       public async Task<bool> OnService(MonoBehaviour caller,Descriptor service,Payload payload,Action<string> callback){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag",service.tag),
                    new Header("Tarantula-token",presence.token),
                    new Header("Tarantula-action",payload.command)
                };
                string json = JsonConvert.SerializeObject(payload);
                string jstr = await _ghc.PostJson(caller,"/service/action",headers,json);
                callback(jstr);
                return true;
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }        
        }
        public async Task<bool> OnInstance(MonoBehaviour caller,Descriptor instance,Payload payload,Action<string> callback){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-token",presence.token),
                    new Header("Tarantula-action",payload.command),
                    new Header("Tarantula-application-id",instance.applicationId),
                    new Header("Tarantula-instance-id",instance.instanceId)
                };
                string json = JsonConvert.SerializeObject(payload,new JsonSerializerSettings{NullValueHandling = NullValueHandling.Ignore});
                string jstr = await _ghc.PostJson(caller,"/application/instance",headers,json);
                callback(jstr);
                return true;
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }        
        }
        public async Task<bool> OnWebSocket(Action<string> callback){
            try{
                //do receive loop
                while(_live){
                    string msg = await _gwc.Receive();
                    //format [label]#[instanceId]?[query]{json payload}
                    //Debug.Log(">>"+msg);
                    callback(msg);
                    //processing msg;
                }
                
                return false;
            }catch(Exception ex){
                _live = false;
                OnException?.Invoke(ex);
                return _live;
            }   
        }
        public async Task<bool> Close(){
            try{
                _live = false;
                bool suc = await _gwc.Close();
                return suc;
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        private bool ParseGameObject(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                message = (string)jo.SelectToken("message");
                return suc;
            }
            //check connection/ticket and create udp connection 
            //JArray tk = (JArray)jo.SelectToken("gameList");
            //for(int i=0;i<tk.Count;i++){
                //Descriptor gm = tk[i].ToObject<Descriptor>();
                //_gameList.Add(gm);
            //}
            return true;
        } 
        private bool ParseLobby(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                message = (string)jo.SelectToken("message");
                return suc;
            }
            JArray tk = (JArray)jo.SelectToken("gameList");
            for(int i=0;i<tk.Count;i++){
                Descriptor gm = tk[i].ToObject<Descriptor>();
                _gameList.Add(gm);
            }
            return true;
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
            }
            return true;
        } 
        private bool ParseLogin(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                message = (string)jo.SelectToken("message");
                return suc;
            }
            JToken tk = jo.SelectToken("presence");
            presence = tk.ToObject<Presence>();
            Connection connection = jo.SelectToken("connection").ToObject<Connection>();
            _gwc = new GecWebSocket(connection,presence);
            return true;
        }
        private bool ParseProfile(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                message = (string)jo.SelectToken("message");
                return suc;
            }
            JToken tk = jo.SelectToken("profile");
            profile = tk.ToObject<Profile>();
            return true;
        }
   } 
   public class GecUdpSocket{
            
   }    
   public class GecWebSocket{
        private ClientWebSocket _websocket;
        private string _url;

        public GecWebSocket(Connection connection,Presence presence){
            _url = connection.protocol+"://"+connection.host+":"+connection.port+"/"+connection.path+"?accessKey="+presence.ticket+"&stub="+presence.stub+"&systemId="+presence.login;
            _websocket = new ClientWebSocket();
            _websocket.Options.AddSubProtocol(connection.subProtocol);
            _websocket.Options.SetRequestHeader("Origin",connection.host);
        }
        public async Task<bool> Connect(){
            await _websocket.ConnectAsync(new Uri(_url),new CancellationTokenSource(5000).Token);
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
            ArraySegment<Byte> rbuff = new ArraySegment<Byte>(new byte[4096]);
            WebSocketReceiveResult wrs = await _websocket.ReceiveAsync(rbuff,CancellationToken.None);
            //check erro here 
            if(wrs.CloseStatus!=null){
                //Debug.Log("Closed on ->"+wrs.CloseStatus);
                return "error{'successful':false,'message':'"+wrs.CloseStatus+"'}";
            }
            return Encoding.UTF8.GetString(rbuff.Array,0,wrs.Count);
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
        
        public Payload data;
    }
    public class Descriptor{
        public bool singleton { get; set; }
        public int deployCode { get; set; }
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
        public string entryCostAsString { get; set; }
        public bool tournamentEnabled { get; set; }
        public bool disabled { get; set; }
        public bool resetEnabled { get; set; }
        public int timerOnModule { get; set; }
        public int runtimeDuration { get; set; }
        public int runtimeDurationOnInstance { get; set; }
        public string tag { get; set; }
        public string icon { get; set; }
        public string viewId { get; set; }
        public string responseLabel { get; set; }
    }

    public class Lobby{
        public Descriptor descriptor { get; set; }
        public List<Descriptor> applications { get; set; }
    }
    public class Device{
        public string deviceId { get; set; }
    }
    public class User{
        public string login { get; set; }
        public string nickname { get; set; }
        public string emailAddress { get; set; }
        public string password { get; set; }
    }
    public class Profile{
        public string nickname { get; set; }
        public string avatar { get; set; }
    }
    public class Connection{
        public string command { get; set; }
        public int code { get; set; }
        public int timestamp { get; set; }
        public int sequence { get; set; }
        public bool successful { get; set; }
        public string path { get; set; }
        public string protocol { get; set; }
        public string subProtocol { get; set; }
        public string host { get; set; }
        public string type { get; set; }
        public string serverId { get; set; }
        public bool secured { get; set; }
        public int port { get; set; }
    }
    public class Presence{
        public bool successful { get; set; }
        public string systemId { get; set; }
        public int stub { get; set; }
        public string token { get; set; }
        public string ticket { get; set; }
        public string balance { get; set; }
        public string login { get; set; }
    }
}
