using System.Collections;
using System.Collections.Generic;
using System.Collections.Concurrent;
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
   
   public delegate void ExceptionHandler(Exception ex);
    
   public class GameEngineCluster{
      
        public event ExceptionHandler OnException;

        private GecHttpClient _ghc;
        private GecWebSocket _gwc;
        private GecUdpSocket _guc;
        private bool _liveWc;
        private bool _liveUc;
        private Dictionary<string,Lobby> _lobbyList;
        private List<Descriptor> _gameList;
        private ConcurrentDictionary<string,Action<Message>> _onMessage;
        private static JsonSerializerSettings JSON_SETTING = new JsonSerializerSettings{NullValueHandling = NullValueHandling.Ignore};
        
        public Presence presence {set;get;}
        public Profile profile {set;get;}
        public string message {set;get;}
        
        public Lobby lobby(string typeId){ return _lobbyList[typeId];}  
        public List<Descriptor> gameList(){ return _gameList;}
        
        public  GameEngineCluster(string host){
            _ghc = new GecHttpClient(host);  
            _lobbyList = new Dictionary<string,Lobby>();
            _gameList = new List<Descriptor>();
            _onMessage = new ConcurrentDictionary<string,Action<Message>>();
            _onMessage["debug"] = (msg)=>{Debug.Log(msg.payload);};
            _liveWc = false;
            _liveUc = false;
        }  
      
        public  async Task<bool> Index(MonoBehaviour caller){
            try{
                string jstr = await _ghc.GetJson(caller,"/user/index",new Header[]{new Header("Tarantula-tag","index/lobby")});
                return ParseIndex(jstr);
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        public  async Task<bool> Register(MonoBehaviour caller,User user){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","index/user"),
                    new Header("Tarantula-magic-key",user.login),
                    new Header("Tarantula-action","onRegister")
                };
                string json = JsonConvert.SerializeObject(user,JSON_SETTING);
                string jstr = await _ghc.PostJson(caller,"/user/action",headers,json);
                return ParseRegister(jstr);            
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
                string json = JsonConvert.SerializeObject(user,JSON_SETTING);
                string jstr = await _ghc.PostJson(caller,"/user/action",headers,json);
                return await ParseLogin(jstr);           
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
                string json = JsonConvert.SerializeObject(device,JSON_SETTING);
                string jstr = await _ghc.PostJson(caller,"/user/action",headers,json);
                return await ParseLogin(jstr);
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
                string json = JsonConvert.SerializeObject(strm,JSON_SETTING);
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
                string json = JsonConvert.SerializeObject(p,JSON_SETTING);
                string jstr = await _ghc.PostJson(caller,"/service/action",headers,json);
                return ParseLobby(jstr);
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }   
        }
        public async Task<bool> OnPlay(MonoBehaviour caller,string joinTag,Descriptor game,Action<JObject> callback){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag",joinTag),
                    new Header("Tarantula-token",presence.token),
                    new Header("Tarantula-action","onPlay")
                };
                Payload p = new Payload();
                p.command = "onPlay";
                p.headers = new Header[]{new Header("applicationId",game.applicationId),new Header("accessMode","2")};
                string json = JsonConvert.SerializeObject(p,JSON_SETTING);
                string jstr = await _ghc.PostJson(caller,"/service/action",headers,json);
                //Processing join response 
                return await ParseGameObject(jstr,game,callback);
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
                string json = JsonConvert.SerializeObject(payload,JSON_SETTING);
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
                string json = JsonConvert.SerializeObject(payload,JSON_SETTING);
                string jstr = await _ghc.PostJson(caller,"/application/instance",headers,json);
                callback(jstr);
                return true;
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }        
        }
        public void RegisterMessageListener(string register,Action<Message> listener){
            _onMessage[register]= listener;
        }
        public async Task<bool> OnMessage(){
            try{
                //do receive loop
                if(_liveUc){
                    while(_liveUc){
                        string msg = await _guc.Receive();
                        ParseInboundMessage(msg);
                    }    
                }
                else{
                    while(_liveWc){
                        string msg = await _gwc.Receive();
                        ParseInboundMessage(msg);
                    }
                }
                return false;
            }catch(Exception ex){
                _liveWc = false;
                OnException?.Invoke(ex);
                return _liveWc;
            }   
        }
        public async Task<bool> Close(){
            try{
                bool suc = false;
                if(_liveWc){
                    _liveWc = false;
                    suc = await _gwc.Close();
                }
                if(_liveUc){
                    _liveUc = false;
                    _guc.Close();
                }
                return suc;
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        private void ParseInboundMessage(string msg){
            //format [label]#[instanceId]?[query]{json payload}
            Message im = new Message();
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
            _onMessage["debug"](im);    
        }
        private async Task<bool> ParseGameObject(string json,Descriptor game,Action<JObject> callback){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                message = (string)jo.SelectToken("message");
                return suc;
            }
            string tid = (string)jo.SelectToken("instanceId");
            string ticket = (string)jo.SelectToken("ticket");
            game.instanceId = tid;
            if(jo.ContainsKey("connection")){
                //streaming on udp game session 
                _guc = new GecUdpSocket(jo.SelectToken("connection").ToObject<Connection>(),presence);
                suc = await _guc.Init(tid,ticket);
                _liveUc = true;
            }
            else{
                //streaming on websocket
                Streaming strm = new Streaming();
                strm.action = "onStream";
                strm.path = "/application/instance";
                strm.streaming = true;
                strm.applicationId = game.applicationId;
                strm.instanceId = game.instanceId;
                Payload p = new Payload();
                p.command = strm.action;
                strm.data = p;
                string jstrm = JsonConvert.SerializeObject(strm,JSON_SETTING);
                suc = await _gwc.Send(jstrm);
            }
            if(suc){callback(jo);}
            return suc;
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
        private bool ParseRegister(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                message = (string)jo.SelectToken("message");
                return suc;
            }
            return true;
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
            }
            return suc;
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
       private UdpClient _udpClient;
       private IPEndPoint endPoint;
       private Connection _connection;
       private Presence _presence;
       public GecUdpSocket(Connection connection,Presence presence){
            IPAddress[] ips = Dns.GetHostAddresses(connection.host);
            endPoint = new IPEndPoint(ips[0],connection.port); 
            _connection = connection;
            _presence = presence;
       }
       public async Task<bool> Init(string instanceId,string ticket){
            _udpClient = new UdpClient(_connection.host,_connection.port);
            OnJoin payload = new OnJoin();
            payload.command= "onJoin";
            payload.systemId= _presence.login;
            payload.instanceId= instanceId;
            payload.stub= _presence.stub;
            payload.ticket= ticket;
            string json = JsonConvert.SerializeObject(payload);
            string mex = "rt#"+instanceId+"?onJoin"+json;
            byte[] onjoin = Encoding.UTF8.GetBytes(mex);
            int bst = await _udpClient.SendAsync(onjoin,onjoin.Length);
            Debug.Log(bst+"<>"+onjoin.Length+"///"+_connection.host+":"+_connection.port);
            return bst==onjoin.Length;
       }
       public void Close(){
           _udpClient.Close();
           _udpClient.Dispose();
       }
       public async Task<bool> Send(string json){
           byte[] payload = Encoding.UTF8.GetBytes(json.ToString());
           int bytes = await _udpClient.SendAsync(payload,payload.Length); 
           return bytes>0;    
       }
       public async Task<string> Receive(){
           UdpReceiveResult ret = await _udpClient.ReceiveAsync();
           return Encoding.UTF8.GetString(ret.Buffer);
       }
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
            ArraySegment<Byte> rbuff = new ArraySegment<Byte>(new byte[4096]);
            WebSocketReceiveResult wrs = await _websocket.ReceiveAsync(rbuff,CancellationToken.None);
            //check erro here 
            if(wrs.CloseStatus!=null){
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
    public class Message{
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
    public class OnJoin{
        public string command;
        public string systemId;
        public string ticket;
        public string instanceId;
        public int stub;
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
