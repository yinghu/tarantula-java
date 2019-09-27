using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.Net.Sockets;
using System.Net;
using System.Threading;

using UnityEngine;
using UnityEngine.Networking;

using WebSocketSharp;

public class TARA_API : MonoBehaviour {
    
    public string GEC_HOST;
    public Logger logger;
    private string token;
    private string systemId;
    private string login;
    private string ticket;
    private int stub;
    private WebSocket wc;
    
    private Dictionary<string,Action<Message>> mCallback = new Dictionary<string,Action<Message>>();
    private Dictionary<string,Lobby> lobbyMapping = new Dictionary<string,Lobby>();
    private Queue messageQueue;
    
    //udp 
    private Thread udpListener;
    private UdpClient udp;
    private IPEndPoint endPoint;
    private bool running;
	void Start () {	
        this.messageQueue = Queue.Synchronized(new Queue());
        Debug.Log(this.messageQueue.IsSynchronized ? "synchronized" : "not synchronized" );
        StartCoroutine(Index());  
	}
	void Update () {
        if(messageQueue.Count>0){
            Message m = (Message)messageQueue.Dequeue();
            if(mCallback.ContainsKey(m.label)){
                mCallback[m.label](m);        
            }else{
                //Debug.Log();
                logger.Error(m.label+" not existed");
            }
        }
	}
    IEnumerator Index(){
        using(UnityWebRequest www = new UnityWebRequest(GEC_HOST+"/user/index","GET")){
		    www.downloadHandler = (DownloadHandler) new DownloadHandlerBuffer();
            www.certificateHandler = new KeyValidator();       
            www.SetRequestHeader("Accept","application/json");
            www.SetRequestHeader("Tarantula-tag","index/lobby");
            yield return www.SendWebRequest();
            if (www.isNetworkError) {
                logger.Error(www.error);	
            }
            else{
                JSONObject jn = new JSONObject(www.downloadHandler.text);
                _Parse(jn.GetField("lobbyList"));
            }
        }
    }
    public void Register(string u,string p,string n,Action<JSONObject> callback){
        StartCoroutine(_Register(u,p,n,callback));    
    }
    IEnumerator _Register(string u,string p,string n,Action<JSONObject> callback){
        JSONObject j = new JSONObject(JSONObject.Type.OBJECT);
        j.AddField("login",u);
        j.AddField("password",p);
        j.AddField("nickname",n);
        byte[] payload = Encoding.UTF8.GetBytes(j.ToString());
        using(UnityWebRequest www = new UnityWebRequest(GEC_HOST+"/user/action","POST")){
		    www.certificateHandler = new KeyValidator();
            www.uploadHandler = (UploadHandler)new UploadHandlerRaw(payload);
            www.downloadHandler = (DownloadHandler)new DownloadHandlerBuffer();
            www.SetRequestHeader("Accept","application/json");
            www.SetRequestHeader("Content-type", "application/x-www-form-urlencoded");
            www.SetRequestHeader("Tarantula-tag","index/user");
            www.SetRequestHeader("Tarantula-magic-key",u);
            www.SetRequestHeader("Tarantula-action","onRegister");
            www.SetRequestHeader("Tarantula-payload-size",""+payload.Length);  
            yield return www.SendWebRequest();
            if (www.isNetworkError) {
                  logger.Error(www.error);	
            }
            else{
                callback(new JSONObject(www.downloadHandler.text));
            }
        }
    }
    public void Reset(string u,Action<bool> callback){
        StartCoroutine(_Reset(u,callback));    
    }
    IEnumerator _Reset(string u,Action<bool> callback){
        JSONObject j = new JSONObject(JSONObject.Type.OBJECT);
        j.AddField("deviceId",u);
        byte[] payload = Encoding.UTF8.GetBytes(j.ToString());
        using(UnityWebRequest www = new UnityWebRequest(GEC_HOST+"/user/action","POST")){
		    www.certificateHandler = new KeyValidator();
            www.uploadHandler = (UploadHandler)new UploadHandlerRaw(payload);
            www.downloadHandler = (DownloadHandler)new DownloadHandlerBuffer();
            www.SetRequestHeader("Accept","application/json");
            www.SetRequestHeader("Content-type", "application/x-www-form-urlencoded");
            www.SetRequestHeader("Tarantula-tag","index/user");
            www.SetRequestHeader("Tarantula-magic-key",u);
            www.SetRequestHeader("Tarantula-action","onReset");
            www.SetRequestHeader("Tarantula-payload-size",""+payload.Length);  
            yield return www.SendWebRequest();
            if (www.isNetworkError) {
                  logger.Error(www.error);	
            }
            else{
                Debug.Log(www.downloadHandler.text);
                JSONObject jn = new JSONObject(www.downloadHandler.text);
                if(jn.GetField("successful").b){
                    JSONObject pre = jn.GetField("presence");
                    token = pre.GetField("token").str;
                    systemId = pre.GetField("systemId").str;
                    ticket = pre.GetField("ticket").str;
                    stub = (int)pre.GetField("stub").n;
                    login = pre.GetField("login").str;
                    _Parse(jn.GetField("lobbyList"));
                    callback(true);
                }else{
                    callback(false);
                }
            }
        }
    }
    IEnumerator _Login(string u,string p,Action<bool> callback){
        JSONObject j = new JSONObject(JSONObject.Type.OBJECT);
        j.AddField("login",u);
        j.AddField("password",p);
        byte[] payload = Encoding.UTF8.GetBytes(j.ToString());
        using(UnityWebRequest www = new UnityWebRequest(GEC_HOST+"/user/action","POST")){
		    www.certificateHandler = new KeyValidator();
            www.uploadHandler = (UploadHandler)new UploadHandlerRaw(payload);
            www.downloadHandler = (DownloadHandler)new DownloadHandlerBuffer();
            www.SetRequestHeader("Accept","application/json");
            www.SetRequestHeader("Content-type", "application/x-www-form-urlencoded");
            www.SetRequestHeader("Tarantula-tag","index/user");
            www.SetRequestHeader("Tarantula-magic-key",u);
            www.SetRequestHeader("Tarantula-action","onLogin");
            www.SetRequestHeader("Tarantula-payload-size",""+payload.Length);  
            yield return www.SendWebRequest();
            if (www.isNetworkError) {
                  logger.Error(www.error);	
            }
            else{
                JSONObject jn = new JSONObject(www.downloadHandler.text);
                if(jn.GetField("successful").b){
                    JSONObject pre = jn.GetField("presence");
                    token = pre.GetField("token").str;
                    systemId = pre.GetField("systemId").str;
                    ticket = pre.GetField("ticket").str;
                    stub = (int)pre.GetField("stub").n;
                    login = pre.GetField("login").str;
                    _Parse(jn.GetField("lobbyList"));
                    callback(true);
                }else{
                    callback(false);
                }
            }
        }
    }
    public void Presence(Action<bool> callback){
        JSONObject jn = new JSONObject(JSONObject.Type.OBJECT);
        jn.AddField("command","onPresence");
        Application app = new Application("presence/lobby","onPresence",jn);
        Request(app,(jxn)=>{
            JSONObject jx = jxn.GetField("connection");
            string ws = jx.GetField("protocol").str+"://"+jx.GetField("host").str+":"+jx.GetField("port").n+"/"+jx.GetField("path").str+"?accessKey="+ticket+"&stub="+stub+"&systemId="+login;
            wc = new WebSocket(ws,"tarantula-service");
            wc.Origin = GEC_HOST;
            wc.OnMessage += (sender,e) =>{
                if(e.IsText){
                    int ix = e.Data.IndexOf("{");
                    string lb = e.Data.Substring(0,ix);
                    messageQueue.Enqueue(new Message(lb,new JSONObject(e.Data.Substring(ix))));
                }    
            };
            wc.OnClose += (sender,e)=>{
                logger.Log("ws closed");   
            };
            wc.OnOpen += (sender,e)=>{
                logger.Log("ws connected");
                callback(true);
            };
            wc.OnError += (sender,e)=>{
                Debug.Log(e.Exception);
                logger.Error("ws failed");
            };
            wc.Connect();
        });    
    }
    public void Balance(Action<JSONObject> callback){
        JSONObject jn = new JSONObject(JSONObject.Type.OBJECT);
        jn.AddField("command","onBalance");
        Application app = new Application("presence/lobby","onBalance",jn);
        Request(app,callback);
    }
    public void OnLobby(string typeId,Action<Descriptor> callback){
        Lobby lb = lobbyMapping[typeId];
        Descriptor a1 = lb.Descriptor();
        if(a1.Category().Equals("game")){//request on game lobby 
            JSONObject jn = new JSONObject(JSONObject.Type.OBJECT);
            jn.AddField("command","onLobby");
            jn.AddField("typeId",typeId);
            Application app = new Application(a1.Tag(),"onLobby",jn);
            Request(app,(resp)=>{
                JSONObject jx = resp.GetField("gameList");
                for(int i=0;i<jx.list.Count;i++){
                    if(i<3){
                        callback(new Descriptor(jx.list[i]));
                    }    
                }
            });
        }
        else if(a1.Category().Equals("service")){//preload loop
            lb.OnLobby(callback);
        }
    }
   
    public void Play(Descriptor descriptor,Action<JSONObject> callback,Action<Message> onstream){
        JSONObject jn = new JSONObject(JSONObject.Type.OBJECT);
        jn.AddField("command","onPlay");
        jn.AddField("accessMode",2);
        jn.AddField("applicationId",descriptor.ApplicationId());
        Application app = new Application("presence/lobby","onPlay",jn);
        Request(app,(game)=>{
            game.AddField("applicationId",descriptor.ApplicationId());
            callback(game);
            JSONObject conn;
            AddMessageListener(game.GetField("label").str,onstream);//web socket listener
            if((conn=game.GetField("gameObject").GetField("connection"))!=null){
                conn.AddField("ticket",game.GetField("gameObject").GetField("ticket").str);
                conn.AddField("instanceId",game.GetField("instanceId").str);
                AddMessageListener(game.GetField("label").str+"#"+game.GetField("instanceId").str,onstream);
                initUdp(conn);
            }else{//stream on websocket if udp not available 
                JSONObject ms = new JSONObject(JSONObject.Type.OBJECT);
                ms.AddField("action","onStream");
                ms.AddField("streaming",true);
                ms.AddField("path","/application/instance");
                ms.AddField("instanceId",game.GetField("instanceId").str);
                ms.AddField("applicationId",game.GetField("applicationId").str);
                JSONObject dt = new JSONObject(JSONObject.Type.OBJECT);
                dt.AddField("command","onStream");
                ms.AddField("data",dt);
                _Send(ms);
            }
        });    
    }
    public void Logout(Action<bool> callback){
        if(systemId==null){
            callback(false);
            return;
        }
        JSONObject jn = new JSONObject(JSONObject.Type.OBJECT);
        jn.AddField("command","onAbsence");
        wc.Close();
        Application app = new Application("presence/lobby","onAbsence",jn);
        Request(app,(jm)=>{
            if(jm.GetField("successful").b){
                callback(true);
                token = null;
                systemId = null;
                ticket = null;
                login = null;
                stub = -1;
            }
            else{
                callback(false);
            }
        });
    }
    public void Request(Application target,Action<JSONObject> callback){
        StartCoroutine(_Service(target,callback));
    }
    IEnumerator _Service(Application target,Action<JSONObject> callback){
        byte[] payload = Encoding.UTF8.GetBytes(target.data.ToString());
        using(UnityWebRequest www = new UnityWebRequest(GEC_HOST+"/service/action","POST")){
		    www.certificateHandler = new KeyValidator();
            www.uploadHandler = (UploadHandler)new UploadHandlerRaw(payload);
            www.downloadHandler = (DownloadHandler)new DownloadHandlerBuffer();            
            www.SetRequestHeader("Tarantula-tag",target.tag);
            www.SetRequestHeader("Tarantula-token",token);
            www.SetRequestHeader("Tarantula-action",target.action);
            www.SetRequestHeader("Accept","application/json");
            www.SetRequestHeader("Content-type", "application/x-www-form-urlencoded");
            www.SetRequestHeader("Tarantula-payload-size",""+payload.Length);  
            yield return www.SendWebRequest();
            if (www.isNetworkError) {
                  logger.Error(www.error);	
            }
            else{
                callback(new JSONObject(www.downloadHandler.text));
            }
        }
    }
    public void Request(Instance target,Action<JSONObject> callback){
        StartCoroutine(_Instance(target,callback));  
    }
    IEnumerator _Instance(Instance target,Action<JSONObject> callback){
        byte[] payload = Encoding.UTF8.GetBytes(target.data.ToString());
        using(UnityWebRequest www = new UnityWebRequest(GEC_HOST+"/application/instance","POST")){
		    www.certificateHandler = new KeyValidator();
            www.uploadHandler = (UploadHandler)new UploadHandlerRaw(payload);
            www.downloadHandler = (DownloadHandler)new DownloadHandlerBuffer();            
            www.SetRequestHeader("Tarantula-token",token);
            www.SetRequestHeader("Tarantula-action",target.action);
            www.SetRequestHeader("Tarantula-application-id",target.applicationId);
            www.SetRequestHeader("Tarantula-instance-id",target.instanceId);
            www.SetRequestHeader("Accept","application/json");
            www.SetRequestHeader("Content-type", "application/x-www-form-urlencoded");
            www.SetRequestHeader("Tarantula-payload-size",""+payload.Length);  
            yield return www.SendWebRequest();
            if (www.isNetworkError) {
                  logger.Error(www.error);	
            }
            else{
                callback(new JSONObject(www.downloadHandler.text));
            }
        }
    }
    public void OnNotification(string label,Action<Message> callback){
        AddMessageListener(label,callback);
        JSONObject ms = new JSONObject(JSONObject.Type.OBJECT);
        ms.AddField("action","onStart");
        ms.AddField("streaming",true);
        ms.AddField("label",label);
        JSONObject dt = new JSONObject(JSONObject.Type.OBJECT);
        dt.AddField("command","onStart");
        ms.AddField("data",dt);
        _Send(ms);   
    }
    public void AddMessageListener(string label,Action<Message> callback){
        if(mCallback.ContainsKey(label)){
            mCallback.Remove(label);
        }
        mCallback.Add(label,callback);    
    }
    public void RemoveMessageListener(string label){
        if(mCallback.ContainsKey(label)){
            mCallback.Remove(label);
        }   
    }
    public void Send(Application target){
        JSONObject ms = new JSONObject(JSONObject.Type.OBJECT);
        ms.AddField("action",target.action);
        ms.AddField("streaming",target.streaming);
        ms.AddField("path",target.path);
        ms.AddField("tag",target.tag);
        ms.AddField("data",target.data);    
        _Send(ms);
    }
    public void Send(Instance target){
        JSONObject ms = new JSONObject(JSONObject.Type.OBJECT);
        ms.AddField("action",target.action);
        ms.AddField("streaming",target.streaming);
        ms.AddField("path",target.path);
        ms.AddField("applicationId",target.applicationId);
        ms.AddField("instanceId",target.instanceId);
        ms.AddField("data",target.data);    
        _Send(ms);
    } 
    void _Send(JSONObject jms){
        wc.Send(jms.ToString());    
    }
    public string SystemId(){
        return this.systemId;
    }
    void _Parse(JSONObject jo){
        for(int i=0;i<jo.list.Count;i++){
            Lobby lbl = new Lobby(jo.list[i]);
            string tk = lbl.Descriptor().TypeId();
            if(lobbyMapping.ContainsKey(tk)){
                lobbyMapping.Remove(tk);
            }
            lobbyMapping.Add(tk,lbl);
        }          
    }
    public void Forward(string label,string instanceId,JSONObject data){
        if(!running){
           return; 
        }
        JSONObject payload = new JSONObject(JSONObject.Type.OBJECT);
        payload.AddField("command","onMessage");
        payload.AddField("label",label);
        payload.AddField("systemId",systemId);
        payload.AddField("instanceId",instanceId);
        payload.AddField("data",data);
        byte[] mf = Encoding.UTF8.GetBytes(payload.ToString());
        udp.Send(mf,mf.Length,endPoint);
    }
    public void StopUdp(){  
        if(!running){
            return;
        }
        try{
            running = false;
            udp.Close();
            udpListener.Interrupt();
            logger.Log("UDP SESSION ENDED");
        }catch(Exception ex){
            logger.Error("EXXX");
        }
    }
    void initUdp(JSONObject conn){
        //remote end point
        IPAddress[] ips = Dns.GetHostAddresses(conn.GetField("host").str);
        endPoint = new IPEndPoint(ips[0],(int)conn.GetField("port").n);
        //local receiver and sender
        udp = new UdpClient();
        udpListener = new Thread(()=>{
            Byte[] buff = new byte[0];
            while(running){
                try{ 
                    buff = udp.Receive(ref endPoint);
                    string json = Encoding.ASCII.GetString(buff);
                    int ix = json.IndexOf("{");
                    string lb = json.Substring(0,ix);
                    //Debug.Log(lb+"=>"+json.Substring(ix));
                    messageQueue.Enqueue(new Message(lb,new JSONObject(json.Substring(ix))));
                }catch(Exception ex){
                    running = false;
                    udp.Close();
                    Debug.Log(ex);
                }
            }
        });
        JSONObject payload = new JSONObject(JSONObject.Type.OBJECT);
        payload.AddField("command","onJoin");
        payload.AddField("systemId",login);
        payload.AddField("instanceId",conn.GetField("instanceId"));
        payload.AddField("stub",stub);
        payload.AddField("ticket",conn.GetField("ticket").str);
        Debug.Log(payload.ToString());
        byte[] onjoin = Encoding.UTF8.GetBytes(payload.ToString());
        udp.Send(onjoin,onjoin.Length,endPoint);
        running = true;
        udpListener.IsBackground = true;
        udpListener.Start();  
        logger.Log("UDP SESSION STARTED");
    }
}
